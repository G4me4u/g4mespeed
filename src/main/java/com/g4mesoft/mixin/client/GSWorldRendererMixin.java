package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIMinecraftClientAccess;
import com.g4mesoft.access.client.GSIWorldRendererAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@Mixin(WorldRenderer.class)
public abstract class GSWorldRendererMixin implements GSIWorldRendererAccess {

	@Shadow @Final private MinecraftClient client;
	
	@Shadow protected abstract void scheduleSectionRender(BlockPos pos, boolean important);
	
	@Unique
	private GSClientController gs_controller;
	@Unique
	private GSTpsModule gs_tpsModule;

	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(MinecraftClient client, CallbackInfo ci) {
		gs_controller = GSClientController.getInstance();
		gs_tpsModule = gs_controller.getTpsModule();
	}

	@Unique
	private float gs_getEntityDeltaTick(Entity entity, float deltaTick) {
		if (!client.isPaused() && (entity instanceof AbstractClientPlayerEntity)) {
			if (gs_tpsModule.isPlayerFixedMovement((AbstractClientPlayerEntity)entity))
				return ((GSIMinecraftClientAccess)client).gs_getFixedMovementTickDelta();
		}

		return deltaTick;
	}
	
	@ModifyArg(
		method = "renderEntities",
		index = 1,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(" +
					"Lnet/minecraft/entity/Entity;" +
					"F" +
					"Z" +
				")V"
		)
	)
	private float onRenderEntityModifyDeltaTick(Entity entity, float deltaTick, boolean flag) {
		return gs_getEntityDeltaTick(entity, deltaTick);
	}

	@ModifyArg(
		method = "renderEntities",
		index = 1,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/render/entity/EntityRenderDispatcher;renderSecondPass(" +
					"Lnet/minecraft/entity/Entity;" +
					"F" +
				")V"
		)
	)
	private float onRenderEntitySecondPassModifyDeltaTick(Entity entity, float deltaTick) {
		return gs_getEntityDeltaTick(entity, deltaTick);
	}
	
	@Override
	public void gs_scheduleBlockUpdate(BlockPos pos, boolean important) {
		scheduleSectionRender(pos, important);
	}
}
