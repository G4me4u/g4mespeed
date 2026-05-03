package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

@Mixin(FallingBlockEntity.class)
public abstract class GSFallingBlockEntityMixin extends Entity {

	public GSFallingBlockEntityMixin(EntityType<?> type, Level world) {
		super(type, world);
	}

	@Override
	public void move(MoverType movementType, Vec3 movement) {
		Level world = level();
		if (!world.isClientSide() || GSClientController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_MOVE_ON_SERVER) {
			// Do not move on the client if the server has pretty sand in 'Move
			// on Server' mode, as server-side positions are sent every tick.
			super.move(movementType, movement);
		}
	}
	
	@Override
	public PushReaction getPistonPushReaction() {
		Level world = level();
		if (!world.isClientSide() || GSClientController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_MOVE_ON_SERVER) {
			// See comment above.
			return super.getPistonPushReaction();
		}
		return PushReaction.IGNORE;
	}
}
