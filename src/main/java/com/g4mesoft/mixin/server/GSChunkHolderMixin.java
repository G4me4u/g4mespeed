package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.GSIChunkHolderAccess;

import net.minecraft.block.BlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ChunkHolder.class)
public abstract class GSChunkHolderMixin implements GSIChunkHolderAccess {

	@Shadow protected abstract void sendPacketToPlayersWatching(Packet<?> packet, boolean boolean_1);

	@Shadow protected abstract void tryUpdateBlockEntityAt(World world, BlockPos blockPos, BlockState blockState);
	
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
	public void sendToNearbyPlayers0(Packet<?> packet) {
		sendPacketToPlayersWatching(packet, false);
	}
}
