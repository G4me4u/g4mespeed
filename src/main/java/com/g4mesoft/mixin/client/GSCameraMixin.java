package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.client.GSIMinecraftAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.compat.GSTweakerooCompat;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

@Mixin(Camera.class)
public class GSCameraMixin {

	@Shadow private Level level;
	@Shadow private Entity entity;
	@Shadow @Final private Minecraft minecraft;

	@Unique
	private GSTpsModule gs_tpsModule;

	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(CallbackInfo ci) {
		gs_tpsModule = GSClientController.getInstance().getTpsModule();
	}

	@ModifyReturnValue(
		method = "getCameraEntityPartialTicks",
		at = @At("RETURN")
	)
	private float onGetCameraEntityPartialTicksModifyPartialTick(float oldPartialTick) {
		if (minecraft.isPaused() || level.tickRateManager().isEntityFrozen(entity))
			return oldPartialTick;
		// Main player/spectating fixed movement player.
		if ((entity instanceof AbstractClientPlayer) && gs_tpsModule.isPlayerFixedMovement((AbstractClientPlayer)entity))
			return ((GSIMinecraftAccess)minecraft).gs_getFixedMovementTickDelta();
		// Tweakeroo freecam.
		if (gs_tpsModule.cTweakerooFreecamHack.get()) {
			GSTweakerooCompat tweakerooCompat = G4mespeedMod.getTweakerooCompat();
			if (tweakerooCompat.isCameraEntityRetreived() && tweakerooCompat.isCameraEntityInstance(entity) && gs_tpsModule.isMainPlayerFixedMovement())
				return ((GSIMinecraftAccess)minecraft).gs_getFixedMovementTickDelta();
		}
		return oldPartialTick;
	}
}
