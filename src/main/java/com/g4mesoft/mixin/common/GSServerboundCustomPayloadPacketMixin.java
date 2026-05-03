package com.g4mesoft.mixin.common;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.packet.GSCustomPayload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class GSServerboundCustomPayloadPacketMixin {

	@Inject(
		method = "lambda$static$1",
		at = @At("HEAD")
	)
	private static void onReadPayload(ArrayList<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> codecs, CallbackInfo ci) {
		codecs.add(new CustomPacketPayload.TypeAndCodec<>(GSCustomPayload.ID, GSCustomPayload.CODEC));
	}
}
