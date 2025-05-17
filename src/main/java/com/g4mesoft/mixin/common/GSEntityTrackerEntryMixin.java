package com.g4mesoft.mixin.common;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.common.GSIEntityTrackerEntryAccess;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSServerPlayerFixedMovementPacket;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.ui.util.GSMathUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.AddEntityS2CPacket;
import net.minecraft.server.entity.EntityTrackerEntry;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;

@Mixin(EntityTrackerEntry.class)
public abstract class GSEntityTrackerEntryMixin implements GSIEntityTrackerEntryAccess {

	private static final double FALLING_BLOCK_GRAVITY  = -0.04;
	private static final double FALLING_BLOCK_FRICTION =  0.98;
	
	@Shadow @Final private Entity currentTrackedEntity;
	@Shadow private int ticks;
	@Shadow private boolean onGround;
	
	@Shadow public abstract void sendToListeners(Packet<?> packet);
	
	@Unique
	private boolean gs_fixedMovement = false;
	@Unique
	private boolean gs_lastFixedMovement = false;
	@Unique
	private boolean gs_tickedFromFallingBlock = false;
	@Unique
	private int gs_fallingBlockTrackingTick = 0;
	@Unique
	private double gs_lastFallingBlockVelocityX = 0.0;
	@Unique
	private double gs_lastFallingBlockVelocityY = 0.0;
	@Unique
	private double gs_lastFallingBlockVelocityZ = 0.0;
	
	@Inject(
		method = "notifyNewLocation",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onNotifyNewLocation(List<PlayerEntity> players, CallbackInfo ci) {
		if (gs_fixedMovement != gs_lastFixedMovement) {
			gs_lastFixedMovement = gs_fixedMovement;

			if (currentTrackedEntity.getType() == EntityType.PLAYER) {
				GSIPacket packet = new GSServerPlayerFixedMovementPacket(currentTrackedEntity.getNetworkId(), gs_fixedMovement);
				// Encode packet to a vanilla packet. This is required for sending to all nearby
				// players. Note that vanilla players will not react to the packet.
				GSPacketManager packetManager = G4mespeedMod.getPacketManager();
				sendToListeners(packetManager.encodePacket(packet, GSServerController.getInstance()));
			}
		}
		
		GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
		if (tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED && currentTrackedEntity.getType() == EntityType.FALLING_BLOCK) {
			if (gs_tickedFromFallingBlock) {
				double dvx = currentTrackedEntity.velocityX - gs_lastFallingBlockVelocityX * FALLING_BLOCK_FRICTION;
				double dvy = currentTrackedEntity.velocityY - gs_lastFallingBlockVelocityY * FALLING_BLOCK_FRICTION;
				double dvz = currentTrackedEntity.velocityZ - gs_lastFallingBlockVelocityZ * FALLING_BLOCK_FRICTION;
				gs_lastFallingBlockVelocityX = currentTrackedEntity.velocityX;
				gs_lastFallingBlockVelocityY = currentTrackedEntity.velocityY;
				gs_lastFallingBlockVelocityZ = currentTrackedEntity.velocityZ;
				
				if (tpsModule.sPrettySand.get() == GSTpsModule.PRETTY_SAND_FIDELITY ||
				    gs_fallingBlockTrackingTick == 0 ||
				    !GSMathUtil.equalsApproximate(dvx, 0.0) ||
				    !GSMathUtil.equalsApproximate(dvy, FALLING_BLOCK_GRAVITY * FALLING_BLOCK_FRICTION) ||
				    !GSMathUtil.equalsApproximate(dvz, 0.0)) {
					
					// Set dirty flag. This will update the position, rotation,
					// and velocity of the falling block immediately.
					currentTrackedEntity.velocityDirty = true;
					
					// Force position and velocity to be sent in their entirety
					onGround = !currentTrackedEntity.onGround;
					ticks = Math.max(1, ticks);
				}
	
				gs_fallingBlockTrackingTick++;
				gs_tickedFromFallingBlock = false;
			} else {
				ci.cancel();
				// return;
			}
		}
	}
	
	@Inject(
		method = "updateListener",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/entity/Entity;onStartedTrackingBy(" +
					"Lnet/minecraft/server/entity/living/player/ServerPlayerEntity;" +
				")V"
		)
	)
	private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
		if (currentTrackedEntity.getType() == EntityType.PLAYER) {
			GSIPacket packet = new GSServerPlayerFixedMovementPacket(currentTrackedEntity.getNetworkId(), gs_fixedMovement);
			// Note that player might be tracking the entity after just joining
			// in which case the extension versions will not yet have been sent.
			GSServerController.getInstance().sendPacket(packet, player, GSVersion.INVALID);
		}
	}
	
	@Inject(
		method = "createAddEntityPacket",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onCreateAddEntityPacket(CallbackInfoReturnable<Packet<?>> cir) {
		if (currentTrackedEntity instanceof FallingBlockEntity) {
			FallingBlockEntity fallingBlockEntity = (FallingBlockEntity)currentTrackedEntity;
			
			if (GSServerController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED) {
				// Note: falling block entity has id 70.
				AddEntityS2CPacket packet = new AddEntityS2CPacket(currentTrackedEntity,
						70, Block.serialize(fallingBlockEntity.getBlock()));
	
				// Calculate offset applied to position (falling block entity is not 1.0 tall)
				double yOffs = (double)((1.0F - currentTrackedEntity.height) / 2.0F);
				((GSIAddEntityS2CPacketAccess)packet).setY(currentTrackedEntity.y - yOffs);
				
				cir.setReturnValue(packet);
			}
		}
	}
	
	@Override
	public boolean gs_isFixedMovement() {
		return gs_fixedMovement;
	}

	@Override
	public void gs_setFixedMovement(boolean fixedMovement) {
		this.gs_fixedMovement = fixedMovement;
	}

	@Override
	public void gs_setTickedFromFallingBlock(boolean tickedFromFallingBlock) {
		this.gs_tickedFromFallingBlock = tickedFromFallingBlock;
	}
}
