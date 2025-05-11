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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;

@Mixin(GameRenderer.class)
public class GSGameRendererMixin {

	@Shadow @Final private MinecraftClient client;
	
	@Unique
	private float gs_getGlobalTickDelta(float oldTickDelta) {
		return client.isPaused() ? oldTickDelta : client.getTickDelta();
	}
	
	@ModifyArg(
		method = "renderCenter",
		index = 4,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/render/Camera;update(" +
					"Lnet/minecraft/world/BlockView;" +
					"Lnet/minecraft/entity/Entity;" +
					"ZZF" +
				")V"
		)
	)
	private float modifyCameraUpdateTickDelta(BlockView blockView, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float oldTickDelta) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		if (focusedEntity instanceof AbstractClientPlayerEntity) {
			if (tpsModule.isPlayerFixedMovement(((AbstractClientPlayerEntity)focusedEntity)))
				return oldTickDelta;
		}
		if (tpsModule.cTweakerooFreecamHack.get()) {
			GSTweakerooCompat tweakerooCompat = G4mespeedMod.getTweakerooCompat();
			if (tweakerooCompat.isCameraEntityRetreived() && tweakerooCompat.isCameraEntityInstance(focusedEntity) && tpsModule.isMainPlayerFixedMovement())
				return oldTickDelta;
		}
		
		return gs_getGlobalTickDelta(oldTickDelta);
	}
	
	@ModifyArg(
		method = "renderCenter",
		index = 1,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/particle/ParticleManager;renderParticles(" +
					"Lnet/minecraft/client/render/Camera;" +
					"F" +
				")V"
		)
	)
	private float modifyParticleRenderTickDelta(float oldTickDelta) {
		return gs_getGlobalTickDelta(oldTickDelta);
	}
	
	@ModifyArg(
		method = "renderCenter",
		index = 2,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/render/WorldRenderer;renderEntities(" +
					"Lnet/minecraft/client/render/Camera;" +
					"Lnet/minecraft/client/render/VisibleRegion;" +
					"F" +
				")V"
		)
	)
	private float modifyWorldRenderTickDelta(float oldTickDelta) {
		return gs_getGlobalTickDelta(oldTickDelta);
	}
}
