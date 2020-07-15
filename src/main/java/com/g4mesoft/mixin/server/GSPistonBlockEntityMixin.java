package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;

import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.core.server.GSControllerServer;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;

@Mixin(PistonBlockEntity.class)
public class GSPistonBlockEntityMixin extends BlockEntity {

	public GSPistonBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Override
	@GSCoreOverride
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		if (GSControllerServer.getInstance().getTpsModule().sParanoidMode.getValue())
			return new BlockEntityUpdateS2CPacket(pos, 0, toInitialChunkDataTag());
		return null;
	}
	
	@Override
	@GSCoreOverride
	public CompoundTag toInitialChunkDataTag() {
	   return toTag(new CompoundTag());
   }
}
