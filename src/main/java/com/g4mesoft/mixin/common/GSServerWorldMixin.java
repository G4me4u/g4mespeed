package com.g4mesoft.mixin.common;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.common.GSIEntityTrackerAccess;
import com.g4mesoft.access.common.GSIServerChunkMapAccess;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.entity.EntityTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.DimensionDataStorage;
import net.minecraft.world.storage.WorldStorage;

@Mixin(ServerWorld.class)
public abstract class GSServerWorldMixin extends World {

	@Shadow @Final private ChunkMap chunkMap;
	@Shadow @Final private EntityTracker entityTracker;
	
	protected GSServerWorldMixin(WorldStorage storage, DimensionDataStorage dimensionDataStorage, WorldData data,
			Dimension dimension, Profiler profiler, boolean isClient) {
		super(storage, dimensionDataStorage, data, dimension, profiler, isClient);
	}
	
	@Inject(
		method = "tickEntities",
		at = @At("RETURN")
	)
	private void onTickEntitiesReturn(CallbackInfo ci) {
		if (GSServerController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED) {
			for (Entity entity : entities) {
				if (!entity.removed && entity.getType() == EntityType.FALLING_BLOCK) {
					((GSIEntityTrackerAccess)entityTracker).gs_setTrackerTickedFromFallingBlock(entity, true);
					((GSIEntityTrackerAccess)entityTracker).gs_tickEntityTracker(entity);
				}
			}
		}
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER, 
			target = "Lnet/minecraft/server/world/ServerWorld;doBlockEvents()V"
		)
	)
	private void onTickImmediateUpdates(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (GSServerController.getInstance().getTpsModule().sImmediateBlockBroadcast.get()) {
			profiler.swap("chunkMap");
			((GSIServerChunkMapAccess)chunkMap).gs_flushAndSendChunkUpdates();
		}
	}
	
	@ModifyArg(
		method = "doBlockEvents",
		allow = 1,
		index = 4,
		at = @At(
			value = "INVOKE", 
			target =
				"Lnet/minecraft/server/PlayerManager;sendPacket(" +
					"Lnet/minecraft/entity/living/player/PlayerEntity;" +
					"DDDD" +
					"Lnet/minecraft/world/dimension/DimensionType;" +
					"Lnet/minecraft/network/packet/Packet;" +
				")V"
		)
	)
	private double blockEventDistance(PlayerEntity player, double x, double y, double z, double dist, DimensionType dimensionType, Packet<?> packet) {
		Block block = ((GSIBlockEventS2CPacketAccess)packet).getBlock2();
		
		if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON) {
			GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
			dist = tpsModule.sBlockEventDistance.get() * 16.0;
		}
		
		return dist;
	}
}
