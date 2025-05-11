package com.g4mesoft.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

@Mixin(EntityRenderDispatcher.class)
public class GSEntityRenderDispatcherMixin {

	@Redirect(
		method = { "render", "renderSecondPass" },
		allow = 2,
		require = 2,
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target="Lnet/minecraft/entity/Entity;age:I"
		)
	)
	private int onRenderGetEntityAge(Entity entity) {
		if (GSClientController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED && entity.getType() == EntityType.FALLING_BLOCK) {
			// We do not want the render positions to be modified when
			// using pretty sand (already done by position packets).
			return (entity.age == 0) ? -1 : entity.age;
		}
		return entity.age;
	}
}
