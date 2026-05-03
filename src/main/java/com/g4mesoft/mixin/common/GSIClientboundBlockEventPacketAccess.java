package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.world.level.block.Block;

@Mixin(ClientboundBlockEventPacket.class)
public interface GSIClientboundBlockEventPacketAccess {

	@Accessor("block")
	public Block getBlock2();
	
}
