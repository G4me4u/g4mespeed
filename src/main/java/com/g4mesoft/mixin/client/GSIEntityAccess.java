package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public interface GSIEntityAccess {

	@Invoker("adjustMovementForCollisions")
	public Vec3d invokeAdjustMovementForCollisions(Vec3d movement);

}
