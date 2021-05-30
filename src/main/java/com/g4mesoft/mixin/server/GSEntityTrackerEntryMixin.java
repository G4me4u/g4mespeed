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
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.module.tps.GSServerPlayerFixedMovementPacket;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(EntityTrackerEntry.class)
public class GSEntityTrackerEntryMixin implements GSIEntityTrackerEntryAccess {

	@Shadow @Final private Entity entity;
	@Shadow @Final private Consumer<Packet<?>> receiver;

	
	private boolean fixedMovement = false;
	private boolean lastFixedMovement = false;
	
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		if (fixedMovement != lastFixedMovement) {
			lastFixedMovement = fixedMovement;

			if (entity instanceof PlayerEntity) {
				GSIPacket packet = new GSServerPlayerFixedMovementPacket(entity.getEntityId(), fixedMovement);
				// Encode packet to a vanilla packet. This is required for sending to all nearby
				// players. Note that vanilla players will not react to the packet.
				GSPacketManager packetManager = G4mespeedMod.getInstance().getPacketManager();
				receiver.accept(packetManager.encodePacket(packet, GSControllerServer.getInstance()));
			}
		}
	}
	
	@Inject(method = "startTracking", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/server/network/EntityTrackerEntry;sendPackets(Ljava/util/function/Consumer;)V"))
	private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
		if (entity instanceof PlayerEntity) {
			GSIPacket packet = new GSServerPlayerFixedMovementPacket(entity.getEntityId(), fixedMovement);
			// Note that player might be tracking the entity after just joining
			// in which case the extension versions will not yet have been sent.
			GSControllerServer.getInstance().sendPacket(packet, player, GSVersion.INVALID);
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
