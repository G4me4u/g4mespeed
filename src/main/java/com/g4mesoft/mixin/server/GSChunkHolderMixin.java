package com.g4mesoft.mixin.server;

import java.util.BitSet;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIChunkHolderAccess;

import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkHolder.LevelUpdateListener;
import net.minecraft.server.world.ChunkHolder.PlayersWatchingChunkProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;

@Mixin(ChunkHolder.class)
public abstract class GSChunkHolderMixin implements GSIChunkHolderAccess {

	@Shadow @Final private ShortSet[] blockUpdatesBySection;

	@Shadow private boolean pendingBlockUpdates;
	@Shadow @Final private BitSet blockLightUpdateBits;
	@Shadow @Final private BitSet skyLightUpdateBits;

	@Shadow protected abstract void sendPacketToPlayersWatching(Packet<?> packet, boolean boolean_1);

	@Shadow protected abstract void tryUpdateBlockEntityAt(World world, BlockPos blockPos, BlockState blockState);
	
	@Shadow public abstract WorldChunk getWorldChunk();
	
	private int loopSectionIndex;
	private ShortSet[] blockEntityUpdatesBySection;
	private boolean pendingBlockEntityUpdates;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(ChunkPos pos, int level, HeightLimitView world, LightingProvider lightingProvider,
	                    LevelUpdateListener levelUpdateListener, PlayersWatchingChunkProvider playersWatchingChunkProvider, CallbackInfo ci) {
		
		blockEntityUpdatesBySection = new ShortSet[blockUpdatesBySection.length];
		pendingBlockEntityUpdates = false;
	}
	
	@Inject(method = "flushUpdates", at = @At("HEAD"))
	private void onFlushUpdates(WorldChunk chunk, CallbackInfo ci) {
		loopSectionIndex = 0;
		
		if (!pendingBlockUpdates && skyLightUpdateBits.isEmpty() && blockLightUpdateBits.isEmpty()) {
			// Only gets executed if there are no normal block or light
			// updates that are marked for updates (where loops do not run).
			if (pendingBlockEntityUpdates) {
				for (int s = 0; s < blockEntityUpdatesBySection.length; s++)
					sendBlockEntityUpdates(chunk, s);
			}
		}
	}
	
	@Inject(method = "flushUpdates", slice = @Slice(
	        from = @At(value = "INVOKE", ordinal = 1, shift = Shift.AFTER, target = "Ljava/util/BitSet;clear()V")),
	        at = @At(value = "FIELD", ordinal = 1, shift = Shift.BEFORE, opcode = Opcodes.GETFIELD,
	        target = "Lnet/minecraft/server/world/ChunkHolder;blockUpdatesBySection:[Lit/unimi/dsi/fastutil/shorts/ShortSet;"))
	private void onFlushUpdatesBlockUpdateLoop(WorldChunk chunk, CallbackInfo ci) {
		if (loopSectionIndex < blockEntityUpdatesBySection.length)
			sendBlockEntityUpdates(chunk, loopSectionIndex++);
	}
	
	@Inject(method = "flushUpdates", at = @At("RETURN"))
	private void onFlushUpdatesReturn(CallbackInfo ci) {
		pendingBlockEntityUpdates = false;
	}
	
	private void sendBlockEntityUpdates(WorldChunk chunk, int sectionIndex) {
		ShortSet markedUpdates = blockEntityUpdatesBySection[sectionIndex];

		if (markedUpdates != null) {
			ChunkSectionPos sectionPos = ChunkSectionPos.from(chunk.getPos(), sectionIndex);

			for (short coord : markedUpdates) {
				BlockPos pos = sectionPos.unpackBlockPos(coord);
				BlockEntity blockEntity = chunk.getBlockEntity(pos);

				if (blockEntity != null) {
					Packet<?> packet;
					if (blockEntity instanceof PistonBlockEntity) {
						NbtCompound tag = blockEntity.writeNbt(new NbtCompound());
						sendPacketToPlayersWatching(new BlockEntityUpdateS2CPacket(pos, 0, tag), false);
					} else {
						packet = blockEntity.toUpdatePacket();
						if (packet != null)
							sendPacketToPlayersWatching(packet, false);
					}
				}
			}

			blockEntityUpdatesBySection[sectionIndex] = null;
		}
	}
	
	@Override
	public void updateBlockImmediately(World world, BlockPos pos) {
        sendPacketToPlayersWatching(new BlockUpdateS2CPacket(world, pos.toImmutable()), false);
        updateBlockEntityImmediately(world, pos);
	}
	
	@Override
	public void updateBlockEntityImmediately(World world, BlockPos pos) {
		tryUpdateBlockEntityAt(world, pos, world.getBlockState(pos));
	}
	
	@Override
	public void markBlockEntityUpdate(BlockPos blockPos) {
		WorldChunk worldChunk = this.getWorldChunk();
		if (worldChunk != null) {
			int sectionIndex = ChunkSectionPos.getSectionCoord(blockPos.getY());
			if (blockEntityUpdatesBySection[sectionIndex] == null) {
				pendingBlockEntityUpdates = true;
				blockEntityUpdatesBySection[sectionIndex] = new ShortArraySet();
			}

			blockEntityUpdatesBySection[sectionIndex].add(ChunkSectionPos.packLocal(blockPos));
		}
	}
	
	@Override
	public void sendToNearbyPlayers0(Packet<?> packet) {
		sendPacketToPlayersWatching(packet, false);
	}
}
