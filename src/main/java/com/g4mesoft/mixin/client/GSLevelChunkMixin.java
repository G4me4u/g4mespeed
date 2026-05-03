package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIPistonMovingBlockEntityAccess;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(LevelChunk.class)
public class GSLevelChunkMixin {
	
	@Shadow @Final private Level level;
	
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
		if (blockEntity instanceof PistonMovingBlockEntity)
			((GSIPistonMovingBlockEntityAccess)blockEntity).gs_onAdded();
	}
}
