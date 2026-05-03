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

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

/* Priority <1000, compatibility fix for Apoli/Origins */
@Mixin(value = GameRenderer.class, priority = 999)
public class GSGameRendererMixin {

	@Shadow @Final Minecraft minecraft;
	
	@ModifyArg(
		method = "renderLevel",
		index = 4,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/Camera;setup(" +
					"Lnet/minecraft/world/level/BlockGetter;" +
					"Lnet/minecraft/world/entity/Entity;" +
					"Z" +
					"Z" +
					"F" +
				")V"
		)
	)
	private float onRenderLevelModifyCameraSetupTickDelta(BlockGetter blockView, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float oldTickDelta) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		if (focusedEntity instanceof AbstractClientPlayer) {
			if (tpsModule.isPlayerFixedMovement(((AbstractClientPlayer)focusedEntity)))
				return oldTickDelta;
		}
		if (tpsModule.cTweakerooFreecamHack.get()) {
			GSTweakerooCompat tweakerooCompat = G4mespeedMod.getTweakerooCompat();
			if (tweakerooCompat.isCameraEntityRetreived() && tweakerooCompat.isCameraEntityInstance(focusedEntity) && tpsModule.isMainPlayerFixedMovement())
				return oldTickDelta;
		}
		
		return minecraft.isPaused() ? oldTickDelta : minecraft.getFrameTime();
	}
	
	@ModifyArg(
		method = "renderLevel",
		index = 0,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(" +
					"F" +
					"J" +
					"Z" +
					"Lnet/minecraft/client/Camera;" +
					"Lnet/minecraft/client/renderer/GameRenderer;" +
					"Lnet/minecraft/client/renderer/LightTexture;" +
					"Lorg/joml/Matrix4f;" +
					"Lorg/joml/Matrix4f;" +
				")V"
		)
	)
	private float onRenderLevelModifyLevelRendererTickDelta(float oldTickDelta) {
		return minecraft.isPaused() ? oldTickDelta : minecraft.getFrameTime();
	}
}
