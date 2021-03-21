package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.tps.GSTpsModule;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;

@Mixin(BossBarHud.class)
public class GSBossBarHudMixin {

	@Shadow @Final private MinecraftClient client;
	
	@Inject(method = "render", at = @At("HEAD"))
	private void onRenderHead(CallbackInfo ci) {
		if (GSControllerClient.getInstance().getTpsModule().cTpsLabel.getValue() == GSTpsModule.TPS_LABEL_TOP_CENTER) {
			RenderSystem.pushMatrix();
			RenderSystem.translatef(0.0f, client.textRenderer.fontHeight + 5.0f, 0.0f);
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void onRenderReturn(CallbackInfo ci) {
		if (GSControllerClient.getInstance().getTpsModule().cTpsLabel.getValue() == GSTpsModule.TPS_LABEL_TOP_CENTER)
			RenderSystem.popMatrix();
	}
}
