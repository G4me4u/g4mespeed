package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.compat.GSTweakerooCompat;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;

@Mixin(GameRenderer.class)
public class GSGameRendererMixin {

	@Shadow @Final private Minecraft minecraft;
	
	@Unique
	private float gs_getGlobalTickDelta(float oldTickDelta) {
		return minecraft.isPaused() ? oldTickDelta : minecraft.getPartialTick();
	}
	
	@ModifyArg(
		method = "render(IFJ)V",
		index = 0,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/render/GameRenderer;setupCamera(" +
					"F" +
					"I" +
				")V"
		)
	)
	private float modifyCameraUpdateTickDelta(float oldTickDelta) {
		Entity camera = minecraft.getCamera();
		
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		if (camera instanceof ClientPlayerEntity) {
			if (tpsModule.isPlayerFixedMovement(((ClientPlayerEntity)camera)))
				return oldTickDelta;
		}
		if (tpsModule.cTweakerooFreecamHack.get()) {
			GSTweakerooCompat tweakerooCompat = G4mespeedMod.getTweakerooCompat();
			if (tweakerooCompat.isCameraEntityRetreived() && tweakerooCompat.isCameraEntityInstance(camera) && tpsModule.isMainPlayerFixedMovement())
				return oldTickDelta;
		}
		
		return gs_getGlobalTickDelta(oldTickDelta);
	}
	
	@ModifyArg(
		method = "renderClouds",
		index = 0,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/render/world/WorldRenderer;renderClouds(" +
					"F" +
					"I" +
				")V"
		)
	)
	private float modifyRenderCloudsTickDelta(float oldTickDelta) {
		return gs_getGlobalTickDelta(oldTickDelta);
	}
}
