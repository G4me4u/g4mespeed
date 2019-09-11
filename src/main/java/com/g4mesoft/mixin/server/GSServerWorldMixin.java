package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
public class GSServerWorldMixin {

	@Inject(method = "unloadEntity(Lnet/minecraft/entity/Entity)V", at = @At("RETURN"))
	public void onEntityUnload(Entity entity, CallbackInfo ci) {
		if (entity instanceof ServerPlayerEntity) {
			
		}
	}

	@Overwrite
	private void sendBlockActions() {
	}
}
