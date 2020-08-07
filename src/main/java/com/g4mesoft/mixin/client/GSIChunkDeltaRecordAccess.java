package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;

@Mixin(ChunkDeltaUpdateS2CPacket.ChunkDeltaRecord.class)
public interface GSIChunkDeltaRecordAccess {

	@Accessor("state")
	public void setBlockState(BlockState state);
	
}
