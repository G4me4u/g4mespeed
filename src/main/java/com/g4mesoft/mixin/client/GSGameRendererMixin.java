package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.compat.GSTweakerooCompat;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;

/* Priority <1000, compatibility fix for Apoli/Origins */
@Mixin(value = GameRenderer.class, priority = 999)
public class GSGameRendererMixin {

	@Shadow @Final private MinecraftClient client;
	
	@ModifyArg(method = "renderWorld", index = 4, at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"))
	private float modifyCameraUpdateTickDelta(BlockView blockView, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float oldTickDelta) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		if (focusedEntity instanceof AbstractClientPlayerEntity) {
			if (tpsModule.isPlayerFixedMovement(((AbstractClientPlayerEntity)focusedEntity)))
				return oldTickDelta;
		}
		if (tpsModule.cTweakerooFreecamHack.getValue()) {
			GSTweakerooCompat tweakerooCompat = G4mespeedMod.getInstance().getTweakerooCompat();
			if (tweakerooCompat.isCameraEntityRetreived() && tweakerooCompat.isCameraEntityInstance(focusedEntity) && tpsModule.isMainPlayerFixedMovement())
				return oldTickDelta;
		}
		
		return client.isPaused() ? oldTickDelta : client.getTickDelta();
	}
	
	@ModifyArg(method = "renderWorld", index = 1, at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V"))
	private float modifyWorldRenderTickDelta(float oldTickDelta) {
		return client.isPaused() ? oldTickDelta : client.getTickDelta();
	}
}
