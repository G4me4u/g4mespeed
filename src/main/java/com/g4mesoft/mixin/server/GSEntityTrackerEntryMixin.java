package com.g4mesoft.mixin.server;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.server.GSIEntityTrackerEntryAccess;
import com.g4mesoft.access.server.GSIServerPlayerEntity;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSServerPlayerFixedMovementPacket;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.util.GSMathUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

@Mixin(EntityTrackerEntry.class)
public class GSEntityTrackerEntryMixin implements GSIEntityTrackerEntryAccess {

	private static final double FALLING_BLOCK_GRAVITY  = -0.04;
	private static final double FALLING_BLOCK_FRICTION =  0.98;
	
	@Shadow @Final private ServerWorld world;
	@Shadow @Final private Entity entity;
	@Shadow @Final private Consumer<Packet<?>> receiver;
	@Shadow private int trackingTick;
	@Shadow private boolean lastOnGround;
	@Shadow private Vec3d velocity;
	
	@Unique
	private boolean gs_fixedMovement = false;
	@Unique
	private boolean gs_lastFixedMovement = false;
	@Unique
	private boolean gs_tickedFromFallingBlock = false;
	@Unique
	private int gs_fallingBlockTrackingTick = 0;
	@Unique
	private Vec3d gs_lastFallingBlockVelocity = Vec3d.ZERO;
	
	@Inject(method = "tick", cancellable = true, at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		if (gs_fixedMovement != gs_lastFixedMovement) {
			gs_lastFixedMovement = gs_fixedMovement;

			if (entity.getType() == EntityType.PLAYER) {
				GSIPacket packet = new GSServerPlayerFixedMovementPacket(entity.getId(), gs_fixedMovement);
				// Encode packet to a vanilla packet. This is required for sending to all nearby
				// players. Note that vanilla players will not react to the packet.
				GSPacketManager packetManager = G4mespeedMod.getInstance().getPacketManager();
				receiver.accept(packetManager.encodePacket(packet, GSServerController.getInstance()));
			}
		}
		
		GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
		if (tpsModule.sPrettySand.getValue() != GSTpsModule.PRETTY_SAND_DISABLED && entity.getType() == EntityType.FALLING_BLOCK) {
			if (gs_tickedFromFallingBlock) {
				Vec3d currentVelocity = entity.getVelocity();
				double dvx = currentVelocity.getX() - gs_lastFallingBlockVelocity.getX() * FALLING_BLOCK_FRICTION;
				double dvy = currentVelocity.getY() - gs_lastFallingBlockVelocity.getY() * FALLING_BLOCK_FRICTION;
				double dvz = currentVelocity.getZ() - gs_lastFallingBlockVelocity.getZ() * FALLING_BLOCK_FRICTION;
				gs_lastFallingBlockVelocity = currentVelocity;
				
				if (tpsModule.sPrettySand.getValue() == GSTpsModule.PRETTY_SAND_FIDELITY ||
				    gs_fallingBlockTrackingTick == 0 ||
				    !GSMathUtil.equalsApproximate(dvx, 0.0) ||
				    !GSMathUtil.equalsApproximate(dvy, FALLING_BLOCK_GRAVITY * FALLING_BLOCK_FRICTION) ||
				    !GSMathUtil.equalsApproximate(dvz, 0.0)) {
					
					// Set dirty flag. This will update the position, rotation,
					// and velocity of the falling block immediately.
					entity.velocityDirty = true;
					
					// Force position and velocity to be sent in their entirety
					lastOnGround = !entity.isOnGround();
					trackingTick = Math.max(1, trackingTick);
				}
	
				gs_fallingBlockTrackingTick++;
				gs_tickedFromFallingBlock = false;
			} else {
				ci.cancel();
				// return;
			}
		}
	}
	
	@Inject(method = "startTracking", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/server/network/EntityTrackerEntry;sendPackets(Ljava/util/function/Consumer;)V"))
	private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
		if (entity.getType() == EntityType.PLAYER) {
			GSIPacket packet = new GSServerPlayerFixedMovementPacket(entity.getId(), gs_fixedMovement);
			// Note that player might be tracking the entity after just joining
			// in which case the extension versions will not yet have been sent.
			GSServerController.getInstance().sendPacket(packet, player, GSVersion.INVALID);
		} else if (entity.getType() == EntityType.FALLING_BLOCK) {
			((GSIServerPlayerEntity)player).gs_onStartTrackingFallingSand(entity);
		}
	}
	
	@Inject(method = "stopTracking", require = 1, allow = 1, expect = 1, cancellable = true, at = @At(value = "INVOKE", shift = Shift.BEFORE,
			target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
	private void onStopTracking(ServerPlayerEntity player, CallbackInfo ci) {
		GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
		if (tpsModule.sPrettySand.getValue() != GSTpsModule.PRETTY_SAND_DISABLED && entity.getType() == EntityType.FALLING_BLOCK) {
			((GSIServerPlayerEntity)player).gs_onStopTrackingFallingSand(entity);
			ci.cancel();
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
