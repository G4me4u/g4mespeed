package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIMinecraftAccess;
import com.g4mesoft.access.client.GSIWorldRendererAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@Mixin(WorldRenderer.class)
public abstract class GSWorldRendererMixin implements GSIWorldRendererAccess {

	@Shadow @Final private Minecraft minecraft;
	
	@Shadow protected abstract void setDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important);
	
	@Unique
	private GSClientController gs_controller;
	@Unique
	private GSTpsModule gs_tpsModule;

	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(Minecraft client, CallbackInfo ci) {
		gs_controller = GSClientController.getInstance();
		gs_tpsModule = gs_controller.getTpsModule();
	}

	@Unique
	private float gs_getGlobalTickDelta(float oldTickDelta) {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.isPaused() ? oldTickDelta : minecraft.getPartialTick();
	}
	
	@ModifyVariable(
		method = "renderEntities",
		argsOnly = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/client/render/entity/EntityRenderDispatcher;setCameraPos(" +
					"D" +
					"D" +
					"D" +
				")V"
		)
	)
	private float modifyParticleRenderTickDelta(float oldTickDelta) {
		return gs_getGlobalTickDelta(oldTickDelta);
	}
	
	@Unique
	private float gs_getEntityDeltaTick(Entity entity, float deltaTick) {
		if (!minecraft.isPaused() && (entity instanceof ClientPlayerEntity)) {
			if (gs_tpsModule.isPlayerFixedMovement((ClientPlayerEntity)entity))
				return ((GSIMinecraftAccess)minecraft).gs_getFixedMovementTickDelta();
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
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		setDirty(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1, important);
	}
}
