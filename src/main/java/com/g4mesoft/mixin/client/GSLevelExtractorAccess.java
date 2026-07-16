package com.g4mesoft.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSILevelExtractorAccess;
import com.g4mesoft.access.client.GSIMinecraftAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;

@Mixin(LevelExtractor.class)
public abstract class GSLevelExtractorAccess implements GSILevelExtractorAccess {

	@Shadow @Final private Minecraft minecraft;
	
	@Shadow public abstract void setBlockDirty(BlockPos pos, boolean playerChanged);
	
	@Unique
	private GSClientController gs_controller;
	@Unique
	private GSTpsModule gs_tpsModule;

	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(CallbackInfo ci) {
		gs_controller = GSClientController.getInstance();
		gs_tpsModule = gs_controller.getTpsModule();
	}
	
	@ModifyArg(
		method = "extractVisibleEntities",
		index = 1,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/renderer/extract/LevelExtractor;extractEntity(" +
					"Lnet/minecraft/world/entity/Entity;" +
					"F" +
				")Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
		)
	)
	private float onExtractVisibleEntitiesModifyEntityDeltaTick(Entity entity, float deltaTick) {
		if (!minecraft.isPaused() && (entity instanceof AbstractClientPlayer)) {
			if (gs_tpsModule.isPlayerFixedMovement((AbstractClientPlayer)entity))
				return ((GSIMinecraftAccess)minecraft).gs_getFixedMovementTickDelta();
		}

		return deltaTick;
	}
	
	@Redirect(
		method = "extractVisibleEntities",
		allow = 1,
		require = 1,
		expect = 1,
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target =
				"Lnet/minecraft/world/entity/Entity;tickCount:I"
		)
	)
	private int onExtractVisibleEntitiesGetEntityTickCount(Entity entity) {
		if (gs_tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED && entity.getType() == EntityTypes.FALLING_BLOCK) {
			// We do not want the render positions to be modified when
			// using pretty sand (already done by position packets).
			return (entity.tickCount == 0) ? -1 : entity.tickCount;
		}
		return entity.tickCount;
	}

	@Override
	public void gs_scheduleBlockUpdate(BlockPos pos, boolean important) {
		setBlockDirty(pos, important);
	}
}
