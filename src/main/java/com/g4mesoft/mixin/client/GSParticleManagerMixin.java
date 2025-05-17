package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.particle.ParticleManager;

@Mixin(ParticleManager.class)
public class GSParticleManagerMixin {

	@Unique
	private float gs_getGlobalTickDelta(float oldTickDelta) {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.isPaused() ? oldTickDelta : minecraft.getPartialTick();
	}
	
	@ModifyVariable(
		method = "renderParticles",
		argsOnly = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/entity/Entity;getRotationVec(" +
					"F" +
				")Lnet/minecraft/util/math/Vec3d;"
		)
	)
	private float modifyParticleRenderTickDelta(float oldTickDelta) {
		return gs_getGlobalTickDelta(oldTickDelta);
	}
}
