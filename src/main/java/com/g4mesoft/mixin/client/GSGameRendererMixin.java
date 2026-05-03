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

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;

/* Priority <1000, compatibility fix for Apoli/Origins */
@Mixin(value = GameRenderer.class, priority = 999)
public class GSGameRendererMixin {

	@Shadow @Final Minecraft minecraft;
	@Shadow @Final private Camera mainCamera;
	
	@ModifyArg(
		method = "update",
		index = 0,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/Camera;update(" +
					"Lnet/minecraft/client/DeltaTracker;" +
				")V"
		)
	)
	private DeltaTracker onUpdateCameraModifyCameraSetupTickDelta(DeltaTracker oldDeltaTracker) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		Entity focusedEntity = mainCamera.entity();
		if (focusedEntity instanceof AbstractClientPlayer) {
			if (tpsModule.isPlayerFixedMovement(((AbstractClientPlayer)focusedEntity)))
				return oldDeltaTracker;
		}
		if (tpsModule.cTweakerooFreecamHack.get()) {
			GSTweakerooCompat tweakerooCompat = G4mespeedMod.getTweakerooCompat();
			if (tweakerooCompat.isCameraEntityRetreived() && tweakerooCompat.isCameraEntityInstance(focusedEntity) && tpsModule.isMainPlayerFixedMovement())
				return oldDeltaTracker;
		}
		
		return minecraft.isPaused() ? oldDeltaTracker: minecraft.getDeltaTracker();
	}
	
	@ModifyArg(
		method = "renderLevel",
		index = 1,
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(" +
					"Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;" +
					"Lnet/minecraft/client/DeltaTracker;" +
					"Z" +
					"Lnet/minecraft/client/renderer/state/level/CameraRenderState;" +
					"Lorg/joml/Matrix4fc;" +
					"Lcom/mojang/blaze3d/buffers/GpuBufferSlice;" +
					"Lorg/joml/Vector4f;" +
					"Z" +
					"Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;" +
				")V"
		)
	)
	private DeltaTracker onRenderLevelModifyLevelRendererTickDelta(DeltaTracker oldDeltaTracker) {
		return minecraft.isPaused() ? oldDeltaTracker : minecraft.getDeltaTracker();
	}
}
