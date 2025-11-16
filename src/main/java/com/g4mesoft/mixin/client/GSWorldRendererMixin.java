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

import com.g4mesoft.access.client.GSIMinecraftClientAccess;
import com.g4mesoft.access.client.GSIWorldRendererAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

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
	private void onInit(CallbackInfo ci) {
		gs_controller = GSClientController.getInstance();
		gs_tpsModule = gs_controller.getTpsModule();
	}
	
	@ModifyArg(
		method = "fillEntityRenderStates",
		index = 1,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/client/render/WorldRenderer;getAndUpdateRenderState(" +
					"Lnet/minecraft/entity/Entity;" +
					"F" +
				")Lnet/minecraft/client/render/entity/state/EntityRenderState;"
		)
	)
	private float onRenderEntityModifyDeltaTick(Entity entity, float deltaTick) {
		if (!client.isPaused() && (entity instanceof AbstractClientPlayerEntity)) {
			if (gs_tpsModule.isPlayerFixedMovement((AbstractClientPlayerEntity)entity))
				return ((GSIMinecraftClientAccess)client).gs_getFixedMovementTickDelta();
		}

		return deltaTick;
	}
	
	@Redirect(
		method = "fillEntityRenderStates",
		allow = 1,
		require = 1,
		expect = 1,
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/entity/Entity;age:I"
		)
	)
	private int onRenderGetEntityAge(Entity entity) {
		if (gs_tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED && entity.getType() == EntityType.FALLING_BLOCK) {
			// We do not want the render positions to be modified when
			// using pretty sand (already done by position packets).
			return (entity.age == 0) ? -1 : entity.age;
		}
		return entity.age;
	}
	
	@ModifyExpressionValue(
		method =
			"getLightmapCoordinates(" +
				"Lnet/minecraft/client/render/WorldRenderer$BrightnessGetter;" +
				"Lnet/minecraft/world/BlockRenderView;" +
				"Lnet/minecraft/block/BlockState;" +
				"Lnet/minecraft/util/math/BlockPos;" +
			")I",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/block/BlockState;getLuminance(" +
				")I"
		)
	)
	private static int onGetLightmapCoordinatesModifyBlockStateGetLuminance(int luminance, WorldRenderer.BrightnessGetter brightnessGetter, BlockRenderView world, BlockState state, BlockPos pos) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		return Math.max(luminance, tpsModule.getMovingBlockLuminance(state, world, pos));
	}
	
	@Override
	public void gs_scheduleBlockUpdate(BlockPos pos, boolean important) {
		scheduleSectionRender(pos, important);
	}
}
