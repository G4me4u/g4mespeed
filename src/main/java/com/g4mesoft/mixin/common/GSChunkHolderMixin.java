package com.g4mesoft.mixin.common;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.common.GSIChunkHolderAccess;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSFlushingBlockEntityUpdatesPacket;

import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;

@Mixin(ChunkHolder.class)
public abstract class GSChunkHolderMixin implements GSIChunkHolderAccess {

	@Shadow @Final private ShortSet[] blockUpdatesBySection;

	@Shadow private boolean pendingBlockUpdates;
	@Shadow private int blockLightUpdateBits;
	@Shadow private int skyLightUpdateBits;

	@Shadow protected abstract void sendPacketToPlayersWatching(Packet<?> packet, boolean boolean_1);

	@Shadow protected abstract void tryUpdateBlockEntityAt(World world, BlockPos blockPos, BlockState blockState);
	
	@Shadow public abstract WorldChunk getWorldChunk();
	
	@Unique
	private static final GSVersion CORRECTED_PUSHING_VERSION = new GSVersion(1, 2, 2);
	
	@Unique
	private int gs_loopSectionIndex;
	@Unique
	private ShortSet[] gs_blockEntityUpdatesBySection;
	@Unique
	private boolean gs_pendingBlockEntityUpdates;
	
	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(ChunkPos pos, int level, LightingProvider lightingProvider, ChunkHolder.LevelUpdateListener levelUpdateListener,
	                    ChunkHolder.PlayersWatchingChunkProvider playersWatchingChunkProvider, CallbackInfo ci) {
		
		gs_blockEntityUpdatesBySection = new ShortSet[blockUpdatesBySection.length];
		gs_pendingBlockEntityUpdates = false;
	}
	
	@Inject(
		method = "flushUpdates",
		at = @At("HEAD")
	)
	private void onFlushUpdates(WorldChunk chunk, CallbackInfo ci) {
		gs_loopSectionIndex = 0;
		
		if (gs_pendingBlockEntityUpdates) {
			GSServerController.getInstance().sendPacketToAll(new GSFlushingBlockEntityUpdatesPacket(true), CORRECTED_PUSHING_VERSION);

			// Only gets executed if there are no normal block or light
			// updates that are marked for updates (where loops do not run).
			if (!pendingBlockUpdates && skyLightUpdateBits == 0 && blockLightUpdateBits == 0) {
				for (int s = 0; s < gs_blockEntityUpdatesBySection.length; s++)
					sendBlockEntityUpdates(chunk, s);
			}
		}
	}
	
	@Inject(
		method = "flushUpdates",
		slice = @Slice(
			from = @At(
				value = "FIELD",
				shift = Shift.AFTER,
				opcode = Opcodes.PUTFIELD,
				target = "Lnet/minecraft/server/world/ChunkHolder;blockLightUpdateBits:I"
			)
		),
		at = @At(
			value = "FIELD",
			ordinal = 1,
			shift = Shift.BEFORE,
			opcode = Opcodes.GETFIELD,
			target =
				"Lnet/minecraft/server/world/ChunkHolder;blockUpdatesBySection" +
				":[Lit/unimi/dsi/fastutil/shorts/ShortSet;"
		)
	)
	private void onFlushUpdatesBlockUpdateLoop(WorldChunk chunk, CallbackInfo ci) {
		if (gs_loopSectionIndex < gs_blockEntityUpdatesBySection.length)
			sendBlockEntityUpdates(chunk, gs_loopSectionIndex++);
	}
	
	@Inject(
		method = "flushUpdates",
		at = @At("RETURN")
	)
	private void onFlushUpdatesReturn(CallbackInfo ci) {
		if (gs_pendingBlockEntityUpdates) {
			GSServerController.getInstance().sendPacketToAll(new GSFlushingBlockEntityUpdatesPacket(false), CORRECTED_PUSHING_VERSION);
			gs_pendingBlockEntityUpdates = false;
		}
	}
	
	@Unique
	private void sendBlockEntityUpdates(WorldChunk chunk, int sectionIndex) {
		ShortSet markedUpdates = gs_blockEntityUpdatesBySection[sectionIndex];

		if (markedUpdates != null) {
			ChunkSectionPos sectionPos = ChunkSectionPos.from(chunk.getPos(), sectionIndex);

			for (short coord : markedUpdates) {
				BlockPos pos = sectionPos.unpackBlockPos(coord);
				BlockEntity blockEntity = chunk.getBlockEntity(pos);

				if (blockEntity != null) {
					Packet<?> packet;
					if (blockEntity instanceof PistonBlockEntity) {
						CompoundTag tag = blockEntity.toTag(new CompoundTag());
						sendPacketToPlayersWatching(new BlockEntityUpdateS2CPacket(pos, 0, tag), false);
					} else {
						packet = blockEntity.toUpdatePacket();
						if (packet != null)
							sendPacketToPlayersWatching(packet, false);
					}
				}
			}

			gs_blockEntityUpdatesBySection[sectionIndex] = null;
		}
	}
	
	@Override
	public void gs_updateBlockImmediately(World world, BlockPos pos) {
		sendPacketToPlayersWatching(new BlockUpdateS2CPacket(world, pos.toImmutable()), false);
		gs_updateBlockEntityImmediately(world, pos);
	}
	
	@Override
	public void gs_updateBlockEntityImmediately(World world, BlockPos pos) {
		tryUpdateBlockEntityAt(world, pos, world.getBlockState(pos));
	}
	
	@Override
	public void gs_markBlockEntityUpdate(BlockPos blockPos) {
		WorldChunk worldChunk = this.getWorldChunk();
		if (worldChunk != null) {
			int sectionIndex = ChunkSectionPos.getSectionCoord(blockPos.getY());
			if (gs_blockEntityUpdatesBySection[sectionIndex] == null) {
				gs_pendingBlockEntityUpdates = true;
				gs_blockEntityUpdatesBySection[sectionIndex] = new ShortArraySet();
			}

			gs_blockEntityUpdatesBySection[sectionIndex].add(ChunkSectionPos.packLocal(blockPos));
		}
	}
	
	@Override
	public void gs_sendToNearbyPlayers0(Packet<?> packet) {
		sendPacketToPlayersWatching(packet, false);
	}
}
