package com.g4mesoft.mixin.common;

import java.util.BitSet;
import java.util.List;

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

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkHolder.PlayerProvider;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(ChunkHolder.class)
public abstract class GSChunkHolderMixin extends GenerationChunkHolder implements GSIChunkHolderAccess {

	@Shadow @Final private LevelHeightAccessor levelHeightAccessor;
	@Shadow @Final private ShortSet[] changedBlocksPerSection;

	@Shadow @Final private PlayerProvider playerProvider;
	
	@Shadow private boolean hasChangedSections;
	@Shadow @Final private BitSet blockChangedLightSectionFilter;
	@Shadow @Final private BitSet skyChangedLightSectionFilter;

	@Shadow protected abstract void broadcastBlockEntityIfNeeded(List<ServerPlayer> players, Level world, BlockPos pos, BlockState state);
	
	@Shadow protected abstract void broadcast(List<ServerPlayer> players, Packet<?> packet);
	
	@Shadow public abstract LevelChunk getTickingChunk();

	public GSChunkHolderMixin(ChunkPos pos) {
		super(pos);
	}
	
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
	private void onInit(CallbackInfo ci) {
		gs_blockEntityUpdatesBySection = new ShortSet[changedBlocksPerSection.length];
		gs_pendingBlockEntityUpdates = false;
	}
	
	@Inject(
		method = "broadcastChanges",
		at = @At("HEAD")
	)
	private void onBroadcastChanges(LevelChunk chunk, CallbackInfo ci) {
		gs_loopSectionIndex = 0;

		if (gs_pendingBlockEntityUpdates) {
			GSServerController.getInstance().sendPacketToAll(new GSFlushingBlockEntityUpdatesPacket(true), CORRECTED_PUSHING_VERSION);
		
			// Only gets executed if there are no normal block or light
			// updates that are marked for updates (where loops do not run).
			if (!hasChangedSections && skyChangedLightSectionFilter.isEmpty() && blockChangedLightSectionFilter.isEmpty()) {
				for (int s = 0; s < gs_blockEntityUpdatesBySection.length; s++)
					sendBlockEntityUpdates(chunk, s);
			}
		}
	}
	
	@Inject(
		method = "broadcastChanges",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				ordinal = 1,
				shift = Shift.AFTER,
				target = "Ljava/util/BitSet;clear()V"
			)
		),
		at = @At(
			value = "FIELD",
			ordinal = 1,
			shift = Shift.BEFORE,
			opcode = Opcodes.GETFIELD,
			target =
				"Lnet/minecraft/server/level/ChunkHolder;changedBlocksPerSection" +
				":[Lit/unimi/dsi/fastutil/shorts/ShortSet;"
		)
	)
	private void onFlushUpdatesBlockUpdateLoop(LevelChunk chunk, CallbackInfo ci) {
		if (gs_loopSectionIndex < gs_blockEntityUpdatesBySection.length)
			sendBlockEntityUpdates(chunk, gs_loopSectionIndex++);
	}
	
	@Inject(
		method = "broadcastChanges",
		at = @At("RETURN")
	)
	private void onFlushUpdatesReturn(CallbackInfo ci) {
		if (gs_pendingBlockEntityUpdates) {
			GSServerController.getInstance().sendPacketToAll(new GSFlushingBlockEntityUpdatesPacket(false), CORRECTED_PUSHING_VERSION);
			gs_pendingBlockEntityUpdates = false;
		}
	}
	
	@Unique
	private void sendBlockEntityUpdates(LevelChunk chunk, int sectionIndex) {
		ShortSet markedUpdates = gs_blockEntityUpdatesBySection[sectionIndex];

		if (markedUpdates != null) {
			int sectionCoord = this.levelHeightAccessor.getSectionYFromSectionIndex(sectionIndex);
			SectionPos sectionPos = SectionPos.of(chunk.getPos(), sectionCoord);
			
			List<ServerPlayer> players = playerProvider.getPlayers(this.pos, false);
			for (short coord : markedUpdates) {
				BlockPos pos = sectionPos.relativeToBlockPos(coord);
				BlockEntity blockEntity = chunk.getBlockEntity(pos);

				if (blockEntity != null) {
					Packet<?> packet;
					if (blockEntity instanceof PistonMovingBlockEntity) {
						broadcast(players, ClientboundBlockEntityDataPacket.create(blockEntity, BlockEntity::saveWithoutMetadata));
					} else {
						packet = blockEntity.getUpdatePacket();
						if (packet != null)
							broadcast(players, packet);
					}
				}
			}

			gs_blockEntityUpdatesBySection[sectionIndex] = null;
		}
	}
	
	@Override
	public void gs_updateBlockImmediately(Level world, BlockPos pos) {
		sendPacketToPlayersWatching(new ClientboundBlockUpdatePacket(world, pos.immutable()), false);
		gs_updateBlockEntityImmediately(world, pos);
	}
	
	@Override
	public void gs_updateBlockEntityImmediately(Level world, BlockPos pos) {
		List<ServerPlayer> players = playerProvider.getPlayers(this.pos, false);
		broadcastBlockEntityIfNeeded(players, world, pos, world.getBlockState(pos));
	}
	
	@Override
	public void gs_markBlockEntityUpdate(BlockPos blockPos) {
		LevelChunk worldChunk = this.getTickingChunk();
		if (worldChunk != null) {
			int sectionIndex = this.levelHeightAccessor.getSectionIndex(blockPos.getY());
			if (gs_blockEntityUpdatesBySection[sectionIndex] == null) {
				gs_pendingBlockEntityUpdates = true;
				gs_blockEntityUpdatesBySection[sectionIndex] = new ShortOpenHashSet();
			}

			gs_blockEntityUpdatesBySection[sectionIndex].add(SectionPos.sectionRelativePos(blockPos));
		}
	}
	
	@Override
	public void gs_sendToNearbyPlayers0(Packet<?> packet) {
		sendPacketToPlayersWatching(packet, false);
	}
	
	@Unique
	private void sendPacketToPlayersWatching(Packet<?> packet, boolean includeLazy) {
		broadcast(playerProvider.getPlayers(pos, includeLazy), packet);
	}
}
