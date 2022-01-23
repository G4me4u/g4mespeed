package com.g4mesoft.mixin.server;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionInfoList;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.access.server.GSIServerChunkManagerAccess;
import com.g4mesoft.access.server.GSIServerPlayNetworkHandlerAccess;
import com.g4mesoft.core.GSCoreExtension;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.g4mesoft.packet.GSICustomPayloadPacket;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class GSServerPlayNetworkHandlerMixin implements GSIServerPlayNetworkHandlerAccess {

	@Shadow public ServerPlayerEntity player;
	@Shadow private int floatingTicks;

	@Shadow protected abstract boolean isHost();

	@Unique
	private final GSExtensionInfoList extensionInfoList = new GSExtensionInfoList();
	@Unique
	private final Map<GSExtensionUID, Integer> translationVersions = new HashMap<>();
	@Unique
	private boolean fixedMovement = false;

	@Unique
	private boolean trackerFixedMovement = false;

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		if (fixedMovement && floatingTicks > 70)
			floatingTicks--;
	}
	
	@ModifyConstant(method = "onPlayerMove", allow = 1, constant = @Constant(intValue = 5), slice = @Slice(
		from = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;movePacketsCount:I", opcode = Opcodes.PUTFIELD),
		to = @At(value = "CONSTANT", args = "stringValue={} is sending move packets too frequently ({} packets since last tick)")))
	private int onPlayerMoveModifyConstant5(int oldValue) {
		// Allow for "infinite" packets between ticks when using fixed movement.
		return fixedMovement ? Integer.MAX_VALUE : oldValue;
	}
	
	@Inject(method = "onPlayerMove", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"))
	private void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
		boolean trackerFixedMovement = false;
		// Only send movement packets if the server is not running 20 ticks per second.
		if (!GSServerController.getInstance().getTpsModule().isDefaultTps()) {
			// G4mespeed is not installed, assume that the player is moving with 20 ticks per second.
			trackerFixedMovement = fixedMovement || !isExtensionInstalled(GSCoreExtension.UID);
		}
		
		this.trackerFixedMovement = trackerFixedMovement;
		
		((GSIServerChunkManagerAccess)player.getWorld().getChunkManager()).setTrackerFixedMovement(player, trackerFixedMovement);
	}

	@Inject(method = "onPlayerMove", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/server/network/ServerPlayerEntity;increaseTravelMotionStats(DDD)V"))
	private void onPlayerMoveUpdateCameraPosition(PlayerMoveC2SPacket packet, CallbackInfo ci) {
		if (trackerFixedMovement)
			((GSIServerChunkManagerAccess)player.getWorld().getChunkManager()).tickEntityTracker(player);
	}

	
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		
		@SuppressWarnings("unchecked")
		GSICustomPayloadPacket<ServerPlayPacketListener> payload = (GSICustomPayloadPacket<ServerPlayPacketListener>)packet;
		
		GSServerController controllerServer = GSServerController.getInstance();
		GSIPacket gsPacket = packetManger.decodePacket(payload, extensionInfoList, (ServerPlayNetworkHandler)(Object)this, controllerServer.getServer());
		if (gsPacket != null) {
			gsPacket.handleOnServer(controllerServer, player);
			ci.cancel();
		}
	}
	
	@Override
	public boolean isExtensionInstalled(GSExtensionUID extensionUid) {
		return extensionInfoList.isExtensionInstalled(extensionUid);
	}
	
	@Override
	public boolean isExtensionInstalled(GSExtensionUID extensionUid, GSVersion minimumVersion) {
		return extensionInfoList.isExtensionInstalled(extensionUid, minimumVersion);
	}

	@Override
	public GSExtensionInfo getExtensionInfo(GSExtensionUID extensionUid) {
		return extensionInfoList.getInfo(extensionUid);
	}

	@Override
	public void clearAllExtensionInfo() {
		extensionInfoList.clearInfo();
	}
	
	@Override
	public void addAllExtensionInfo(GSExtensionInfo[] extensionInfo) {
		extensionInfoList.addAllInfo(extensionInfo);
	}

	@Override
	public void addExtensionInfo(GSExtensionInfo info) {
		extensionInfoList.addInfo(info);
	}
	
	@Override
	public void setTranslationVersion(GSExtensionUID uid, int translationVersion) {
		translationVersions.put(uid, translationVersion);
	}

	@Override
	public int getTranslationVersion(GSExtensionUID uid) {
		return translationVersions.getOrDefault(uid, GSTranslationModule.INVALID_TRANSLATION_VERSION);
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
