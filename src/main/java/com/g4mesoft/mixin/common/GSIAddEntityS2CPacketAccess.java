package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.packet.s2c.play.AddEntityS2CPacket;

@Mixin(AddEntityS2CPacket.class)
public interface GSIAddEntityS2CPacketAccess {

	@Accessor("y")
	public void setY(double y);

}
