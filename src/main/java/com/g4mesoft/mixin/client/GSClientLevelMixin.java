package com.g4mesoft.mixin.client;

import java.util.function.Consumer;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(ClientLevel.class)
public abstract class GSClientLevelMixin extends Level implements GSIClientLevelAccess {

	protected GSClientLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey,
			RegistryAccess registryAccess, Holder<DimensionType> holder, boolean bl, boolean bl2, long l, int i) {
		super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
	}
	
	@Shadow @Final private Minecraft minecraft;
	@Shadow @Final EntityTickList tickingEntities;
	@Shadow @Final private BlockStatePredictionHandler blockStatePredictionHandler;
	
	@Shadow public abstract void tickNonPassenger(Entity entity);

	@Unique
	private boolean gs_tickingEntities;
	@Unique
	private GSTpsModule gs_tpsModule = GSClientController.getInstance().getTpsModule();

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
