package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIPistonBlockEntityAccess;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
public class GSWorldChunkMixin {
	
	@Shadow @Final private World world;
	
	@Inject(
		method = "setBlockEntity",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Ljava/util/Map;put(" +
					"Ljava/lang/Object;" +
					"Ljava/lang/Object;" +
				")Ljava/lang/Object;"
		)
	)
	private void onSetBlockEntityAfterPut(BlockEntity blockEntity, CallbackInfo ci) {
		if (blockEntity instanceof PistonBlockEntity)
			((GSIPistonBlockEntityAccess)blockEntity).gs_onAdded();
	}
}
