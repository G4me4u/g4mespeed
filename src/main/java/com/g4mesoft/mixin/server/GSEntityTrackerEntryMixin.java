package com.g4mesoft.mixin.server;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.GSIEntityTrackerEntryAccess;
import com.g4mesoft.access.GSIServerWorldAccess;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSServerPlayerFixedMovementPacket;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(EntityTrackerEntry.class)
public class GSEntityTrackerEntryMixin implements GSIEntityTrackerEntryAccess {

	@Shadow @Final private ServerWorld world;
	@Shadow @Final private Entity entity;
	@Shadow @Final private Consumer<Packet<?>> receiver;
	@Shadow private int trackingTick;
	@Shadow private boolean lastOnGround;
	@Shadow private long lastX;
	@Shadow private long lastY;
	@Shadow private long lastZ;
	
	private boolean fixedMovement = false;
	private boolean lastFixedMovement = false;
	
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		if (fixedMovement != lastFixedMovement) {
			lastFixedMovement = fixedMovement;

			if (entity.getType() == EntityType.PLAYER) {
				GSIPacket packet = new GSServerPlayerFixedMovementPacket(entity.getId(), fixedMovement);
				// Encode packet to a vanilla packet. This is required for sending to all nearby
				// players. Note that vanilla players will not react to the packet.
				GSPacketManager packetManager = G4mespeedMod.getInstance().getPacketManager();
				receiver.accept(packetManager.encodePacket(packet, GSServerController.getInstance()));
			}
		}
		
		GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
		if (tpsModule.sPrettySand.getValue() && trackingTick > 0 && entity.getType() == EntityType.FALLING_BLOCK) {
			// Set dirty flag. This will update the position, rotation,
			// and velocity of the falling block every tick.
			entity.velocityDirty = true;

			if (trackingTick == 1) {
				// Force position and velocity to be sent in their entirety
				lastOnGround = !entity.isOnGround();
				trackingTick = 1;
			}
		}
	}
	
	@Inject(method = "startTracking", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/server/network/EntityTrackerEntry;sendPackets(Ljava/util/function/Consumer;)V"))
	private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
		if (entity.getType() == EntityType.PLAYER) {
			GSIPacket packet = new GSServerPlayerFixedMovementPacket(entity.getId(), fixedMovement);
			// Note that player might be tracking the entity after just joining
			// in which case the extension versions will not yet have been sent.
			GSServerController.getInstance().sendPacket(packet, player, GSVersion.INVALID);
		}
	}
	
	@Inject(method = "stopTracking", require = 1, allow = 1, expect = 1, cancellable = true, at = @At(value = "INVOKE", shift = Shift.BEFORE,
			target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
	private void onStopTracking(ServerPlayerEntity player, CallbackInfo ci) {
		GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
		if (tpsModule.sPrettySand.getValue() && entity.getType() == EntityType.FALLING_BLOCK) {
			// Schedule the destroy update in the next tick
			((GSIServerWorldAccess)world).scheduleDestroyEntityPacket(player, entity.getId());
			ci.cancel();
		}
	}
	
	@Override
	public boolean isFixedMovement() {
		return fixedMovement;
	}

	@Override
	public void setFixedMovement(boolean fixedMovement) {
		this.fixedMovement = fixedMovement;
	}
}
