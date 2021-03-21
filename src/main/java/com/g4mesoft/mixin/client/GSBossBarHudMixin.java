package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(BossBarHud.class)
public class GSBossBarHudMixin {

	@Shadow @Final private MinecraftClient client;
	
	@Inject(method = "render", at = @At("HEAD"))
	private void onRenderHead(MatrixStack matrixStack, CallbackInfo ci) {
		if (GSControllerClient.getInstance().getTpsModule().cTpsLabel.getValue() == GSTpsModule.TPS_LABEL_TOP_CENTER) {
			matrixStack.push();
			matrixStack.translate(0.0, client.textRenderer.fontHeight + 5, 0.0);
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void onRenderReturn(MatrixStack matrixStack, CallbackInfo ci) {
		if (GSControllerClient.getInstance().getTpsModule().cTpsLabel.getValue() == GSTpsModule.TPS_LABEL_TOP_CENTER)
			matrixStack.pop();
	}
}