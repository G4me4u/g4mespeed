package com.g4mesoft.mixin.common;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.packet.GSCustomPayload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;

@Mixin(CustomPayloadS2CPacket.class)
public abstract class GSCustomPayloadS2CPacketMixin {

	@Inject(
		method = "method_58270",
		at = @At("HEAD")
	)
	private static void onReadPayload(ArrayList<CustomPayload.Type<? extends PacketByteBuf, ? extends CustomPayload>> codecs, CallbackInfo ci) {
		codecs.add(new CustomPayload.Type<>(GSCustomPayload.ID, GSCustomPayload.CODEC));
	}
}
