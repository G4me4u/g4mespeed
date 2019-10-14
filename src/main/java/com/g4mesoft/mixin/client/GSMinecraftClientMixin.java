package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIMinecraftClientAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.debug.GSDebug;
import com.g4mesoft.module.tps.GSITpsDependant;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.SoundManager;

@Mixin(MinecraftClient.class)
public class GSMinecraftClientMixin implements GSIMinecraftClientAccess {

	@Shadow @Final private RenderTickCounter renderTickCounter;
	@Shadow private SoundManager soundManager;
	@Shadow public ClientPlayerEntity player;
	
	@Inject(method = "init()V", at = @At("RETURN"))
	public void onInit(CallbackInfo ci) {
		GSControllerClient controllerClient = GSControllerClient.getInstance();
		controllerClient.init((MinecraftClient)(Object)this);

		GSTpsModule tpsModule = controllerClient.getTpsModule();
		tpsModule.addTpsListener((GSITpsDependant)renderTickCounter);
		tpsModule.addTpsListener((GSITpsDependant)soundManager);
	}
	
	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
	public void onDisconnect(Screen screen, CallbackInfo ci) {
		// Check if player is null. This ensures that we only
		// call disconnect when we're leaving a play-session.
		if (this.player != null)
			GSControllerClient.getInstance().onDisconnectServer();
	}
	
	@Inject(method = "stop", at = @At(value = "CONSTANT", args = "stringValue=Stopping!"))
	public void onClientClose(CallbackInfo ci) {
		GSControllerClient.getInstance().onClientClose();
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		GSDebug.onClientTick();
		GSControllerClient.getInstance().tick();
	}

	@Override
	public RenderTickCounter getRenderTickCounter() {
		return renderTickCounter;
	}
}
