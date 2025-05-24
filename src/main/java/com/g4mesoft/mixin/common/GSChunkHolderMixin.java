package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.common.GSIChunkHolderAccess;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSFlushingBlockEntityUpdatesPacket;

import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.ChunkHolder;
import net.minecraft.server.ChunkMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WorldChunk.BlockEntityCreationType;

@Mixin(ChunkHolder.class)
public abstract class GSChunkHolderMixin implements GSIChunkHolderAccess {

	@Shadow @Final private ChunkMap chunkMap;

	@Shadow protected abstract void sendPacket(Packet<?> packet);

	@Shadow protected abstract void sendBlockEntityUpdate(BlockEntity blockEntity);
	
	@Shadow public abstract WorldChunk getChunk();
	
	@Unique
	private static final GSVersion CORRECTED_PUSHING_VERSION = new GSVersion(1, 2, 2);
	
	@Unique
	private ShortSet[] gs_blockEntityUpdatesBySection;
	@Unique
	private boolean gs_pendingBlockEntityUpdates;
	
	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(CallbackInfo ci) {
		// Note: world height is 16 * 16 = 256
		gs_blockEntityUpdatesBySection = new ShortSet[16];
		gs_pendingBlockEntityUpdates = false;
	}
	
	@Inject(
		method = "sendChanges",
		at = @At("RETURN")
	)
	private void onFlushUpdatesReturn(CallbackInfo ci) {
		if (gs_pendingBlockEntityUpdates) {
			GSServerController.getInstance().sendPacketToAll(new GSFlushingBlockEntityUpdatesPacket(true), CORRECTED_PUSHING_VERSION);
			for (int s = 0; s < gs_blockEntityUpdatesBySection.length; s++)
				sendBlockEntityUpdates(getChunk(), s);
			GSServerController.getInstance().sendPacketToAll(new GSFlushingBlockEntityUpdatesPacket(false), CORRECTED_PUSHING_VERSION);
			gs_pendingBlockEntityUpdates = false;
		}
	}
	
	@Unique
	private void sendBlockEntityUpdates(WorldChunk chunk, int sectionIndex) {
		ShortSet markedUpdates = gs_blockEntityUpdatesBySection[sectionIndex];

		if (markedUpdates != null) {
			int sectionX = chunk.chunkX << 4;
			int sectionY = sectionIndex << 4;
			int sectionZ = chunk.chunkZ << 4;
			
			for (short coord : markedUpdates) {
				int x = ((coord      ) & 15) + sectionX;
				int y = ((coord >>> 4) & 15) + sectionY;
				int z = ((coord >>> 8) & 15) + sectionZ;
				BlockPos pos = new BlockPos(x, y, z);
				BlockEntity blockEntity = chunk.getBlockEntity(pos, BlockEntityCreationType.CHECK);

				if (blockEntity != null) {
					Packet<?> packet;
					if (blockEntity instanceof MovingBlockEntity) {
						NbtCompound tag = blockEntity.writeNbt(new NbtCompound());
						sendPacket(new BlockEntityUpdateS2CPacket(pos, 0, tag));
					} else {
						packet = blockEntity.createUpdatePacket();
						if (packet != null)
							sendPacket(packet);
					}
				}
			}

			gs_blockEntityUpdatesBySection[sectionIndex] = null;
		}
	}
	
	@Override
	public void gs_updateBlockImmediately(World world, BlockPos pos) {
		sendPacket(new BlockUpdateS2CPacket(world, pos.immutable()));
		gs_updateBlockEntityImmediately(world, pos);
	}
	
	@Override
	public void gs_updateBlockEntityImmediately(World world, BlockPos pos) {
		// Note: blockEntity == null is checked in the invocation.
		sendBlockEntityUpdate(world.getBlockEntity(pos));
	}
	
	@Override
	public void gs_markBlockEntityUpdate(BlockPos blockPos) {
		WorldChunk worldChunk = this.getChunk();
		int sectionIndex = blockPos.getY() >> 4;
		if (worldChunk != null && sectionIndex < gs_blockEntityUpdatesBySection.length) {
			if (gs_blockEntityUpdatesBySection[sectionIndex] == null) {
				if (!gs_pendingBlockEntityUpdates) {
					chunkMap.markDirty((ChunkHolder)(Object)this);
					gs_pendingBlockEntityUpdates = true;
				}
				gs_blockEntityUpdatesBySection[sectionIndex] = new ShortArraySet();
			}

			int relX = blockPos.getX() & 15;
			int relY = blockPos.getY() & 15;
			int relZ = blockPos.getZ() & 15;
			short coord = (short)(relX | (relY << 4) | (relZ << 8));
			gs_blockEntityUpdatesBySection[sectionIndex].add(coord);
		}
	}
	
	@Override
	public void gs_sendToNearbyPlayers0(Packet<?> packet) {
		sendPacket(packet);
	}
}
