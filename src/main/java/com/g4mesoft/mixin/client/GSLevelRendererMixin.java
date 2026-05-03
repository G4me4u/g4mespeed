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

import com.g4mesoft.access.client.GSIMinecraftAccess;
import com.g4mesoft.access.client.GSILevelRendererAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LevelRenderer.class)
public abstract class GSLevelRendererMixin implements GSILevelRendererAccess {

	@Shadow @Final private Minecraft minecraft;
	
	@Shadow protected abstract void setBlockDirty(BlockPos pos, boolean important);
	
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
		method = "renderLevel",
		index = 4,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(" +
					"Lnet/minecraft/world/entity/Entity;" +
					"D" +
					"D" +
					"D" +
					"F" +
					"Lcom/mojang/blaze3d/vertex/PoseStack;" +
					"Lnet/minecraft/client/renderer/MultiBufferSource;" +
				")V"
		)
	)
	private float onRenderLevelModifyRenderEntityDeltaTick(Entity entity, double cameraX, double cameraY, double cameraZ, float deltaTick, PoseStack matrices, MultiBufferSource vertexConsumers) {
		if (!minecraft.isPaused() && (entity instanceof AbstractClientPlayer)) {
			if (gs_tpsModule.isPlayerFixedMovement((AbstractClientPlayer)entity))
				return ((GSIMinecraftAccess)minecraft).gs_getFixedMovementTickDelta();
		}

		return deltaTick;
	}
	
	@Redirect(
		method = "renderLevel",
		allow = 1,
		require = 1,
		expect = 1,
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/world/entity/Entity;tickCount:I"
		)
	)
	private int onRenderGetEntityTickCount(Entity entity) {
		if (gs_tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED && entity.getType() == EntityType.FALLING_BLOCK) {
			// We do not want the render positions to be modified when
			// using pretty sand (already done by position packets).
			return (entity.tickCount == 0) ? -1 : entity.tickCount;
		}
		return entity.tickCount;
	}
	
	@ModifyExpressionValue(
		method =
			"getLightColor(" +
				"Lnet/minecraft/world/level/BlockAndTintGetter;" +
				"Lnet/minecraft/world/level/block/state/BlockState;" +
				"Lnet/minecraft/core/BlockPos;" +
			")I",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(" +
				")I"
		)
	)
	private static int onGetLightColorModifyBlockStateGetLightEmission(int luminance, BlockAndTintGetter world, BlockState state, BlockPos pos) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		return Math.max(luminance, tpsModule.getMovingBlockLuminance(state, world, pos));
	}
	
	@Override
	public void gs_scheduleBlockUpdate(BlockPos pos, boolean important) {
		setBlockDirty(pos, important);
	}
}
