package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.Block;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;

@Mixin(BlockEventS2CPacket.class)
public interface GSIBlockEventS2CPacketAccess {

	@Accessor("block")
	public Block getBlock2();
	
}
