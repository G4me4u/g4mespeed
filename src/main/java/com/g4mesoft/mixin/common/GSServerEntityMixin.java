package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.common.GSIServerEntityAccess;
import com.g4mesoft.access.common.GSIServerPlayerAccess;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSServerPlayerFixedMovementPacket;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.ui.util.GSMathUtil;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerEntity.class)
public class GSServerEntityMixin implements GSIServerEntityAccess {

	private static final double FALLING_BLOCK_GRAVITY  = -0.04;
	private static final double FALLING_BLOCK_FRICTION =  0.98;
	
	@Shadow @Final private ServerLevel level;
	@Shadow @Final private Entity entity;
	@Shadow @Final private ServerEntity.Synchronizer synchronizer;
	@Shadow private int tickCount;
	@Shadow private boolean wasOnGround;
	@Shadow private Vec3 lastSentMovement;
	
	@Unique
	private boolean gs_fixedMovement = false;
	@Unique
	private boolean gs_lastFixedMovement = false;
	@Unique
	private boolean gs_tickedFromFallingBlock = false;
	@Unique
	private int gs_fallingBlockTrackingTick = 0;
	@Unique
	private Vec3 gs_lastFallingBlockVelocity = Vec3.ZERO;
	
	@Inject(
		method = "sendChanges",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onSendChanges(CallbackInfo ci) {
		if (gs_fixedMovement != gs_lastFixedMovement) {
			gs_lastFixedMovement = gs_fixedMovement;

			if (entity.getType() == EntityTypes.PLAYER) {
				GSIPacket packet = new GSServerPlayerFixedMovementPacket(entity.getId(), gs_fixedMovement);
				// Encode packet to a vanilla packet. This is required for sending to all nearby
				// players. Note that vanilla players will not react to the packet.
				GSPacketManager packetManager = G4mespeedMod.getPacketManager();
				@SuppressWarnings("unchecked")
				Packet<? super ClientGamePacketListener> encodedPacket = (Packet<? super ClientGamePacketListener>)packetManager.encodePacket(packet, GSServerController.getInstance());
				synchronizer.sendToTrackingPlayers(encodedPacket);
			}
		}
		
		GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
		if (tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED && entity.getType() == EntityTypes.FALLING_BLOCK) {
			if (gs_tickedFromFallingBlock) {
				Vec3 currentVelocity = entity.getDeltaMovement();
				double dvx = currentVelocity.x() - gs_lastFallingBlockVelocity.x() * FALLING_BLOCK_FRICTION;
				double dvy = currentVelocity.y() - gs_lastFallingBlockVelocity.y() * FALLING_BLOCK_FRICTION;
				double dvz = currentVelocity.z() - gs_lastFallingBlockVelocity.z() * FALLING_BLOCK_FRICTION;
				gs_lastFallingBlockVelocity = currentVelocity;
				
				if (tpsModule.sPrettySand.get() == GSTpsModule.PRETTY_SAND_MOVE_ON_SERVER ||
				    gs_fallingBlockTrackingTick == 0 ||
				    !GSMathUtil.equalsApproximate(dvx, 0.0) ||
				    !GSMathUtil.equalsApproximate(dvy, FALLING_BLOCK_GRAVITY * FALLING_BLOCK_FRICTION) ||
				    !GSMathUtil.equalsApproximate(dvz, 0.0)) {
					
					// Set dirty flag. This will update the position, rotation,
					// and velocity of the falling block immediately.
					entity.needsSync = true;
					
					// Force position and velocity to be sent in their entirety
					wasOnGround = !entity.onGround();
					tickCount = Math.max(1, tickCount);
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
		method = "addPairing",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/server/level/ServerEntity;sendPairingData(" +
					"Lnet/minecraft/server/level/ServerPlayer;" +
					"Ljava/util/function/Consumer;" +
				")V"
		)
	)
	private void onAddPairing(ServerPlayer player, CallbackInfo ci) {
		if (entity.getType() == EntityTypes.PLAYER) {
			GSIPacket packet = new GSServerPlayerFixedMovementPacket(entity.getId(), gs_fixedMovement);
			// Note that player might be tracking the entity after just joining
			// in which case the extension versions will not yet have been sent.
			GSServerController.getInstance().sendPacket(packet, player, GSVersion.INVALID);
		} else if (entity.getType() == EntityTypes.FALLING_BLOCK) {
			((GSIServerPlayerAccess)player).gs_onStartTrackingFallingSand(entity);
		}
	}
	
	@Inject(
		method = "removePairing",
		require = 1,
		allow = 1,
		expect = 1,
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(" +
					"Lnet/minecraft/network/protocol/Packet;" +
				")V"
		)
	)
	private void onRemovePairing(ServerPlayer player, CallbackInfo ci) {
		GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
		if (tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED && entity.getType() == EntityTypes.FALLING_BLOCK) {
			((GSIServerPlayerAccess)player).gs_onStopTrackingFallingSand(entity);
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
