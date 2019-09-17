package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIMinecraftClientAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.tps.GSITpsDependant;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.SoundManager;

@Mixin(MinecraftClient.class)
public class GSMinecraftClientMixin implements GSIMinecraftClientAccess, GSITpsDependant {

	@Shadow @Final private RenderTickCounter renderTickCounter;
	@Shadow private SoundManager soundManager;

	
	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(CallbackInfo ci) {
		GSControllerClient.getInstance().init((MinecraftClient)(Object)this);
		GSControllerClient.getInstance().getTpsModule().addTpsListener(this);
	}
	
	@Inject(method = "disconnect", at = @At("HEAD"))
	public void onDisconnect(CallbackInfo ci) {
		System.out.println("Disconnecting!");
		GSControllerClient.getInstance().onDisconnectServer();
	}

	@Override
	public void tpsChanged(float newTps, float oldTps) {
		((GSITpsDependant)renderTickCounter).tpsChanged(newTps, oldTps);

		if (soundManager != null)
			((GSITpsDependant)soundManager).tpsChanged(newTps, oldTps);
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		GSControllerClient.getInstance().tick();
	}

	@Override
	public RenderTickCounter getRenderTickCounter() {
		return renderTickCounter;
	}
}
