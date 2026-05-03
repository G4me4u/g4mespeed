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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

@Mixin(ChunkAccess.class)
public class GSChunkAccessMixin {

	@Unique
	private BlockPos gs_findBlocks_blockPos;
	
	@ModifyArg(
		method = "findBlockLightSources",
		index = 0,
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/world/level/chunk/ChunkAccess;findBlocks(" +
					"Ljava/util/function/Predicate;" +
					"Ljava/util/function/BiConsumer;" +
				")V"
		)
	)
	private Predicate<BlockState> onFindBlockLightSourcesModifyPedicate(Predicate<BlockState> predicate) {
		if (gs_findBlocks_blockPos != null) {
			// Note: only relevant for world chunks.
			if (((Object)this) instanceof LevelChunk) {
				Level world = ((LevelChunk)(Object)this).getLevel();
				if (world.isClientSide) {
					GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
					return state -> predicate.test(state) || tpsModule.getMovingBlockLuminance(state, world, gs_findBlocks_blockPos) != 0;
				}
			}
		}
		return predicate;
	}
	
	@Inject(
		method = "findBlocks",
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
	private void onForEachBlockMatchingPredicateBeforePredicateTest(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> consumer, CallbackInfo ci, BlockPos.MutableBlockPos mutable, int i, LevelChunkSection chunkSection, BlockPos blockPos, int j, int k, int l, BlockState blockState) {
		gs_findBlocks_blockPos = mutable.setWithOffset(blockPos, l, j, k);
	}
	
	@Inject(
		method = "findBlocks",
		at = @At("RETURN")
	)
	private void onForEachBlockMatchingPredicateReturn(CallbackInfo ci) {
		gs_findBlocks_blockPos = null;
	}
}
