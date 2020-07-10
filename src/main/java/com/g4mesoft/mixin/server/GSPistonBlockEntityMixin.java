package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;

import com.g4mesoft.core.GSCoreOverride;

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
		return new BlockEntityUpdateS2CPacket(pos, 0, toInitialChunkDataTag());
	}
	
	@Override
	@GSCoreOverride
	public CompoundTag toInitialChunkDataTag() {
	   return toTag(new CompoundTag());
   }
}
