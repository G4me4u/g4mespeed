package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;

@Mixin(ClientConnection.class)
public class GSClientConnectionMixin {

	@Inject(method = "exceptionCaught", at = @At("HEAD"))
	public void onExceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo ci) {
		throwable.printStackTrace();
	}
}
