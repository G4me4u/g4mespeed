package com.g4mesoft.mixin.server;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIServerChunkManagerAccess;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;

@Mixin(ServerWorld.class)
public abstract class GSServerWorldMixin extends World {

	protected GSServerWorldMixin(LevelProperties levelProperties, DimensionType dimensionType,
			BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Profiler profiler, boolean isClient) {
		super(levelProperties, dimensionType, chunkManagerProvider, profiler, isClient);
	}

	@ModifyArg(method = "sendBlockActions", allow = 1, index = 4, at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/server/PlayerManager;sendToAround(Lnet/minecraft/entity/player/PlayerEntity;DDDDLnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/network/Packet;)V"))
	public double blockEventDistance(PlayerEntity player, double x, double y, double z, double dist, DimensionType dimension, Packet<?> packet) {
		Block block = ((GSIBlockActionS2CPacketAccess)packet).getBlock2();
		
		if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON) {
			GSTpsModule tpsModule = GSControllerServer.getInstance().getTpsModule();
			dist = tpsModule.sBlockEventDistance.getValue() * 16.0;
		}
		
		return dist;
	}
	
	@Inject(method = "tick", at = @At(value = "INVOKE", shift = Shift.AFTER, 
			target = "Lnet/minecraft/server/world/ServerWorld;sendBlockActions()V"))
	public void onTickImmediateUpdates(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (GSControllerServer.getInstance().getTpsModule().sImmediateBlockBroadcast.getValue()) {
			getProfiler().swap("chunkSource");
			((GSIServerChunkManagerAccess) getChunkManager()).flushAndSendChunkUpdates();
		}
	}
}
