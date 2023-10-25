package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.packet.GSCustomPayload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

@Mixin(CustomPayloadS2CPacket.class)
public abstract class GSCustomPayloadS2CPacketMixin {

	@Inject(
		method = "readPayload",
		cancellable = true,
		at = @At("HEAD")
	)
	private static void onReadPayload(Identifier identifier, PacketByteBuf buf, CallbackInfoReturnable<CustomPayload> cir) {
		if (GSCustomPayload.GS_IDENTIFIER.equals(identifier)) {
			cir.setReturnValue(new GSCustomPayload(buf));
			cir.cancel();
		}
	}
}
