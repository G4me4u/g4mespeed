package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;

@Mixin(GameRenderer.class)
public class GSGameRendererMixin {

	@Shadow @Final private MinecraftClient client;
	
	@ModifyArg(method = "renderWorld", index = 1, at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V"))
	public float modifyTickDelta(float oldTickDelta) {
		return client.isPaused() ? oldTickDelta : client.getTickDelta();
	}
}
