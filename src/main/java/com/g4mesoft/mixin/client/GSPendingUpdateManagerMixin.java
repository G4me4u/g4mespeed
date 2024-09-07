package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.client.GSIPendingUpdateManagerAccess;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.util.math.BlockPos;

@Mixin(PendingUpdateManager.class)
public class GSPendingUpdateManagerMixin implements GSIPendingUpdateManagerAccess {

	@Shadow @Final private Long2ObjectOpenHashMap<?> blockPosToPendingUpdate;
	
	@Override
	public boolean gs_removePendingUpdate(BlockPos pos) {
		return blockPosToPendingUpdate.remove(pos.asLong()) != null;
	}
}
