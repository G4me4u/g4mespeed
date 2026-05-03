package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.client.GSIBlockStatePredictionHandlerAccess;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;

@Mixin(BlockStatePredictionHandler.class)
public class GSBlockStatePredictionHandlerMixin implements GSIBlockStatePredictionHandlerAccess {

	@Shadow @Final private Long2ObjectOpenHashMap<?> serverVerifiedStates;
	
	@Override
	public boolean gs_removePendingUpdate(BlockPos pos) {
		return serverVerifiedStates.remove(pos.asLong()) != null;
	}
}
