package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.debug.GSDebug;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;

@Mixin(ClientConnection.class)
public class GSClientConnectionMixin {

	@Inject(method = "exceptionCaught", at = @At("HEAD"))
	public void onExceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo ci) {
		if (GSDebug.GS_DEBUG)
			throwable.printStackTrace();
	}
}
