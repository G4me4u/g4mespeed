package com.g4mesoft.mixin.client;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIPistonBlockEntityAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
public abstract class GSWorldChunkMixin {
	
	@Shadow @Final private World world;
	
	@Shadow public abstract BlockState getBlockState(BlockPos pos);
	
	@ModifyArg(
		method = "getLightSourcesStream",
		index = 0,
		at = @At(
			value = "INVOKE",
			target =
				"Ljava/util/stream/Stream;filter(" +
					"Ljava/util/function/Predicate;" +
				")Ljava/util/stream/Stream;"
		)
	)
	private Predicate<BlockPos> onForEachLightSourceModifyPedicate(Predicate<BlockPos> predicate) {
		if (world.isClient) {
			GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
			return pos -> predicate.test(pos) || tpsModule.getMovingBlockLuminance(this.getBlockState(pos), world, pos) != 0;
		}
		return predicate;
	}
	
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
