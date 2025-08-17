package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.piston.PistonMoveBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(FallingBlockEntity.class)
public abstract class GSFallingBlockEntityMixin extends Entity {

	public GSFallingBlockEntityMixin(World world) {
		super(world);
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			target =
				"Lnet/minecraft/world/World;removeBlock(" +
					"Lnet/minecraft/util/math/BlockPos;" +
				")Z"
		)
	)
	private boolean redirectTickRemoveBlock(World world, BlockPos pos) {
		if (!world.isClient || GSClientController.getInstance().getTpsModule().sPrettySand.get() == GSTpsModule.PRETTY_SAND_DISABLED) {
			// Do not remove the source block on the client when pretty sand is
			// enabled. This might cause the client to remove the final position,
			// if it is lagging behind, and desync the server and client world.
			return world.removeBlock(pos);
		}
		return false;
	}
	
	@Override
	public void move(double x, double y, double z) {
		if (!world.isClient || GSClientController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_MOVE_ON_SERVER) {
			// Do not move on the client if the server has pretty sand in 'Move
			// on Server' mode, as server-side positions are sent every tick.
			super.move(x, y, z);
		}
	}
	
	@Override
	public PistonMoveBehavior getPistonMoveBehavior() {
		if (!world.isClient || GSClientController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_MOVE_ON_SERVER) {
			// See comment above.
			return super.getPistonMoveBehavior();
		}
		return PistonMoveBehavior.IGNORE;
	}
}
