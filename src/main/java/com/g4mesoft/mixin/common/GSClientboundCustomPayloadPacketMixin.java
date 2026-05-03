package com.g4mesoft.mixin.common;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.packet.GSCustomPayload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class GSClientboundCustomPayloadPacketMixin {

	@Inject(
		method = "method_58270",
		at = @At("HEAD")
	)
	private static void onReadPayload(ArrayList<CustomPacketPayload.TypeAndCodec<? extends FriendlyByteBuf, ? extends CustomPacketPayload>> codecs, CallbackInfo ci) {
		codecs.add(new CustomPacketPayload.TypeAndCodec<>(GSCustomPayload.ID, GSCustomPayload.CODEC));
	}
}
