package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.state.BlockState;
import net.minecraft.network.packet.s2c.play.BlocksUpdateS2CPacket;

@Mixin(BlocksUpdateS2CPacket.BlockUpdate.class)
public interface GSIBlocksUpdateBlockUpdateAccess {

	@Accessor("state")
	public void setBlockState(BlockState state);
	
}
