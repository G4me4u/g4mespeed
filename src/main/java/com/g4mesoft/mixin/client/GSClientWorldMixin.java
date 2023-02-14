package com.g4mesoft.mixin.client;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIClientWorldAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.EntityList;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

@Mixin(ClientWorld.class)
public abstract class GSClientWorldMixin extends World implements GSIClientWorldAccess {

	@Shadow @Final private MinecraftClient client;
	@Shadow @Final EntityList entityList;
	
	@Shadow @Final private PendingUpdateManager pendingUpdateManager;

	@Unique
	private boolean gs_tickingEntities;
	@Unique
	private GSTpsModule gs_tpsModule = GSClientController.getInstance().getTpsModule();

	protected GSClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef,
			RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld,
			long seed, int maxChainedNeighborUpdates) {
		super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
	}
	
	@Shadow public abstract void tickEntity(Entity entity);
	
	@Inject(method = "tickEntities", at = @At("HEAD"))
	private void onTickEntitiesHead(CallbackInfo ci) {
		gs_tickingEntities = true;
	}

	@Inject(method = "tickEntities", at = @At("RETURN"))
	private void onTickEntitiesReturn(CallbackInfo ci) {
		gs_tickingEntities = false;
	}
	
	@Inject(method = "tickEntity", cancellable = true, at = @At("HEAD"))
	private void onTickEntity(Entity entity, CallbackInfo ci) {
		if (gs_tickingEntities && (entity instanceof AbstractClientPlayerEntity)) {
			if (gs_tpsModule.isPlayerFixedMovement((AbstractClientPlayerEntity)entity))
				ci.cancel();
		}
	}
	
	@Override
	public void gs_tickFixedMovementPlayers() {
		entityList.forEach((entity) -> {
			if (entity instanceof AbstractClientPlayerEntity) {
				AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)entity;
				if (!player.hasVehicle() && !player.isRemoved() && gs_tpsModule.isPlayerFixedMovement(player))
					((World)(Object)this).tickEntity(this::tickEntity, player);
			}
		});
	}
	
	@Override
	public boolean gs_setBlockStateImmediate(BlockPos pos, BlockState state, int flags) {
		// Update the block position in the pending sequence.
		// Note: nothing happens if there is no sequence, but
		//       this ensures that the update sequence does
		//       not override changes to states.
		pendingUpdateManager.hasPendingUpdate(pos, state);
		return super.setBlockState(pos, state, flags);
	}
}
