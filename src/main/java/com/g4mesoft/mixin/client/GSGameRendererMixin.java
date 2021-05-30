package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;

@Mixin(GameRenderer.class)
public class GSGameRendererMixin {

	@Shadow @Final private MinecraftClient client;
	
	@ModifyArg(method = "renderWorld", index = 4, at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"))
	private float modifyCameraUpdateTickDelta(BlockView blockView, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float oldTickDelta) {
		if (focusedEntity instanceof AbstractClientPlayerEntity) {
			GSTpsModule tpsModule = GSControllerClient.getInstance().getTpsModule();
			if (tpsModule.isPlayerFixedMovement(((AbstractClientPlayerEntity)focusedEntity)))
				return oldTickDelta;
		}
		
		return client.isPaused() ? oldTickDelta : client.getTickDelta();
	}
	
	@ModifyArg(method = "renderWorld", index = 1, at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V"))
	private float modifyWorldRenderTickDelta(float oldTickDelta) {
		return client.isPaused() ? oldTickDelta : client.getTickDelta();
	}
}
