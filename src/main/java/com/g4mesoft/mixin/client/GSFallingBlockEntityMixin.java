package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(FallingBlockEntity.class)
public abstract class GSFallingBlockEntityMixin extends Entity {

	public GSFallingBlockEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			target =
				"Lnet/minecraft/world/World;removeBlock(" +
					"Lnet/minecraft/util/math/BlockPos;" +
					"Z" +
				")Z"
		)
	)
	private boolean redirectTickRemoveBlock(World world, BlockPos pos, boolean move) {
		if (!world.isClient || GSClientController.getInstance().getTpsModule().sPrettySand.get() == GSTpsModule.PRETTY_SAND_DISABLED) {
			// Do not remove the source block on the client when pretty sand is
			// enabled. This might cause the client to remove the final position,
			// if it is lagging behind, and desync the server and client world.
			return world.removeBlock(pos, move);
		}
		return false;
	}
	
	@GSCoreOverride
	@Override
	public void move(MovementType movementType, Vec3d movement) {
		if (!world.isClient || GSClientController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_FIDELITY) {
			// Do not move on the client if the server has pretty sand in fidelity
			// mode. (the positions are sent from the server every tick).
			super.move(movementType, movement);
		}
	}
}
