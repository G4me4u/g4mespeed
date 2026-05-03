package com.g4mesoft.mixin.common;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.common.GSIServerChunkCacheAccess;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(ServerLevel.class)
public abstract class GSServerLevelMixin extends Level {

	protected GSServerLevelMixin(WritableLevelData properties, ResourceKey<Level> registryRef,
			RegistryAccess registryManager, Holder<DimensionType> dimensionEntry,
			Supplier<ProfilerFiller> profiler, boolean isClient, boolean debugWorld, long biomeAccess,
			int maxChainedNeighborUpdates) {
		super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess,
				maxChainedNeighborUpdates);
	}

	@Shadow @Final EntityTickList entityTickList;

	@Inject(
		method = "tick",
		at = @At("RETURN")
	)
	private void onTickReturn(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (GSServerController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED) {
			ServerChunkCache chunkManager = (ServerChunkCache)getChunkSource();

			entityTickList.forEach((entity) -> {
				if (!entity.isRemoved() && entity.getType() == EntityType.FALLING_BLOCK) {
					((GSIServerChunkCacheAccess)chunkManager).gs_setTrackerTickedFromFallingBlock(entity, true);
					((GSIServerChunkCacheAccess)chunkManager).gs_tickEntityTracker(entity);
				}
			});
		}
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER, 
			target =
				"Lnet/minecraft/server/level/ServerLevel;runBlockEvents(" +
				")V"
		)
	)
	private void onTickImmediateUpdates(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (GSServerController.getInstance().getTpsModule().sImmediateBlockBroadcast.get()) {
			getProfiler().popPush("chunkSource");
			((GSIServerChunkCacheAccess) getChunkSource()).gs_flushAndSendChunkUpdates();
		}
	}
	
	@ModifyArg(
		method = "runBlockEvents",
		allow = 1,
		index = 4,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/server/players/PlayerList;broadcast(" +
					"Lnet/minecraft/world/entity/player/Player;" +
					"DDDD" +
					"Lnet/minecraft/resources/ResourceKey;" +
					"Lnet/minecraft/network/protocol/Packet;" +
				")V"
		)
	)
	private double blockEventDistance(Player player, double x, double y, double z, double dist, ResourceKey<Level> dimensionKey, Packet<?> packet) {
		Block block = ((GSIClientboundBlockEventPacketAccess)packet).getBlock2();
		
		if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON) {
			GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
			dist = tpsModule.sBlockEventDistance.get() * 16.0;
		}
		
		return dist;
	}
}
