package com.g4mesoft.mixin.server;

import java.util.Deque;
import java.util.LinkedList;
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

import com.g4mesoft.access.GSIServerChunkManagerAccess;
import com.g4mesoft.access.GSIServerWorldAccess;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSFallingBlockInfo;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.EntityList;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

@Mixin(ServerWorld.class)
public abstract class GSServerWorldMixin extends World implements GSIServerWorldAccess {

	@Shadow @Final EntityList entityList;

	private Deque<GSFallingBlockInfo> destroyFallingBlockQueue = new LinkedList<>();
	private Deque<GSFallingBlockInfo> cachedDestroyFallingBlockQueue = new LinkedList<>();
	
	protected GSServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryKey,
			DimensionType dimensionType, Supplier<Profiler> supplier, boolean bl, boolean bl2, long l) {
		super(properties, registryKey, dimensionType, supplier, bl, bl2, l);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void onTickHead(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (GSServerController.getInstance().getTpsModule().sPrettySand.getValue() != GSTpsModule.PRETTY_SAND_DISABLED) {
			Deque<GSFallingBlockInfo> tmpQueue = cachedDestroyFallingBlockQueue;
			cachedDestroyFallingBlockQueue = destroyFallingBlockQueue;
			destroyFallingBlockQueue = tmpQueue;
			
			ServerChunkManager chunkManager = (ServerChunkManager)getChunkManager();
			for (GSFallingBlockInfo info : cachedDestroyFallingBlockQueue) {
				ServerPlayerEntity player = info.getPlayer();
				if (!player.isRemoved() && player.networkHandler != null)
					((GSIServerChunkManagerAccess)chunkManager).updateBlockImmdiately(info.getBlockPos());
			}
		} else {
			destroyFallingBlockQueue.clear();
		}
	}
	
	@Inject(method = "tick", at = @At("RETURN"))
	private void onTickReturn(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
	if (GSServerController.getInstance().getTpsModule().sPrettySand.getValue() != GSTpsModule.PRETTY_SAND_DISABLED) {
			GSFallingBlockInfo info;
			while ((info = cachedDestroyFallingBlockQueue.poll()) != null) {
				ServerPlayerEntity player = info.getPlayer();
				if (!player.isRemoved() && player.networkHandler != null)
					player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(new int[] { info.getEntityId() }));
			}

			ServerChunkManager chunkManager = (ServerChunkManager)getChunkManager();

			entityList.forEach((entity) -> {
				if (!entity.isRemoved() && entity.getType() == EntityType.FALLING_BLOCK) {
					((GSIServerChunkManagerAccess)chunkManager).setTrackerTickedFromFallingBlock(entity, true);
					((GSIServerChunkManagerAccess)chunkManager).tickEntityTracker(entity);
				}
			});
		}
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", shift = Shift.AFTER, 
			target = "Lnet/minecraft/server/world/ServerWorld;processSyncedBlockEvents()V"))
	private void onTickImmediateUpdates(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (GSServerController.getInstance().getTpsModule().sImmediateBlockBroadcast.getValue()) {
			getProfiler().swap("chunkSource");
			((GSIServerChunkManagerAccess) getChunkManager()).flushAndSendChunkUpdates();
		}
	}
	
	@ModifyArg(method = "processSyncedBlockEvents", allow = 1, index = 4, at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/server/PlayerManager;sendToAround(Lnet/minecraft/entity/player/PlayerEntity;DDDDLnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/network/Packet;)V"))
	private double blockEventDistance(PlayerEntity player, double x, double y, double z, double dist, RegistryKey<World> dimensionKey, Packet<?> packet) {
		Block block = ((GSIBlockEventS2CPacketAccess)packet).getBlock2();
		
		if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON) {
			GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
			dist = tpsModule.sBlockEventDistance.getValue() * 16.0;
		}
		
		return dist;
	}
	
	@Override
	public void scheduleDestroyFallingBlock(GSFallingBlockInfo fallingBlockInfo) {
		if (GSServerController.getInstance().getTpsModule().sPrettySand.getValue() != GSTpsModule.PRETTY_SAND_DISABLED)
			destroyFallingBlockQueue.add(fallingBlockInfo);
	}
}
