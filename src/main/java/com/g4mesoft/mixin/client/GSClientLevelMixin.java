package com.g4mesoft.mixin.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIClientLevelAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(ClientLevel.class)
public abstract class GSClientLevelMixin extends Level implements GSIClientLevelAccess {

	@Shadow @Final private Minecraft minecraft;
	@Shadow @Final EntityTickList tickingEntities;
	@Shadow @Final private BlockStatePredictionHandler blockStatePredictionHandler;
	
	@Unique
	private boolean gs_tickingEntities;
	@Unique
	private GSTpsModule gs_tpsModule = GSClientController.getInstance().getTpsModule();
	
	@Shadow public abstract void tickNonPassenger(Entity entity);

	protected GSClientLevelMixin(WritableLevelData properties, ResourceKey<Level> registryRef,
			RegistryAccess registryManager, Holder<DimensionType> dimensionEntry,
			Supplier<ProfilerFiller> profiler, boolean isClient, boolean debugWorld, long biomeAccess,
			int maxChainedNeighborUpdates) {
		super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess,
				maxChainedNeighborUpdates);
	}
	
	@Inject(
		method = "tickEntities",
		at = @At("HEAD")
	)
	private void onTickEntitiesHead(CallbackInfo ci) {
		gs_tickingEntities = true;
	}

	@Inject(
		method = "tickEntities",
		at = @At("RETURN")
	)
	private void onTickEntitiesReturn(CallbackInfo ci) {
		gs_tickingEntities = false;
	}
	
	@Inject(
		method = "tickNonPassenger",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onTickEntity(Entity entity, CallbackInfo ci) {
		if (gs_tickingEntities && (entity instanceof AbstractClientPlayer)) {
			if (gs_tpsModule.isPlayerFixedMovement((AbstractClientPlayer)entity))
				ci.cancel();
		}
	}
	
	@Override
	public void gs_forEachEntity(Consumer<Entity> action) {
		tickingEntities.forEach(action);
	}
	
	@Override
	public void gs_tickFixedMovementPlayers() {
		tickingEntities.forEach((entity) -> {
			if (entity instanceof AbstractClientPlayer) {
				AbstractClientPlayer player = (AbstractClientPlayer)entity;
				if (!player.isPassenger() && !player.isRemoved() && gs_tpsModule.isPlayerFixedMovement(player))
					((Level)(Object)this).guardEntityTick(this::tickNonPassenger, player);
			}
		});
	}
	
	@Override
	public boolean gs_setBlockStateImmediate(BlockPos pos, BlockState state, int flags) {
		// Update the block position in the pending sequence.
		// Note: nothing happens if there is no sequence, but
		//       this ensures that the update sequence does
		//       not override changes to states.
		blockStatePredictionHandler.updateKnownServerState(pos, state);
		return super.setBlock(pos, state, flags);
	}
	
	public BlockStatePredictionHandler gs_getPendingUpdateManager() {
		return blockStatePredictionHandler;
	}
}
