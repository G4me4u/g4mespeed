package com.g4mesoft.mixin.client;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(Chunk.class)
public class GSChunkMixin {

	@Unique
	private BlockPos gs_forEachBlockMatching_blockPos;
	
	@ModifyArg(
		method = "forEachLightSource",
		index = 0,
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/world/chunk/Chunk;forEachBlockMatchingPredicate(" +
					"Ljava/util/function/Predicate;" +
					"Ljava/util/function/BiConsumer;" +
				")V"
		)
	)
	private Predicate<BlockState> onForEachLightSourceModifyPedicate(Predicate<BlockState> predicate) {
		if (gs_forEachBlockMatching_blockPos != null) {
			// Note: only relevant for world chunks.
			if (((Object)this) instanceof WorldChunk) {
				World world = ((WorldChunk)(Object)this).getWorld();
				if (world.isClient) {
					GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
					return state -> predicate.test(state) || tpsModule.getMovingBlockLuminance(state, world, gs_forEachBlockMatching_blockPos) != 0;
				}
			}
		}
		return predicate;
	}
	
	@Inject(
		method = "forEachBlockMatchingPredicate",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Ljava/util/function/Predicate;test(" +
					"Ljava/lang/Object;" +
				")Z"
		)
	)
	private void onForEachBlockMatchingPredicateBeforePredicateTest(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> consumer, CallbackInfo ci, BlockPos.Mutable mutable, int i, ChunkSection chunkSection, BlockPos blockPos, int j, int k, int l, BlockState blockState) {
		gs_forEachBlockMatching_blockPos = mutable.set(blockPos, l, j, k);
	}
	
	@Inject(
		method = "forEachBlockMatchingPredicate",
		at = @At("RETURN")
	)
	private void onForEachBlockMatchingPredicateReturn(CallbackInfo ci) {
		gs_forEachBlockMatching_blockPos = null;
	}
}
