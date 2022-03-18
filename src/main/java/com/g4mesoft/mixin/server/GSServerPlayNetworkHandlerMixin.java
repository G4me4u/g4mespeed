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
	private final GSExtensionInfoList gs_extensionInfoList = new GSExtensionInfoList();
	@Unique
	private final Map<GSExtensionUID, Integer> gs_translationVersions = new HashMap<>();
	@Unique
	private boolean gs_fixedMovement = false;

	@Unique
	private boolean gs_trackerFixedMovement = false;

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		if (gs_fixedMovement && floatingTicks > 70)
			floatingTicks--;
	}
	
	@ModifyConstant(method = "onPlayerMove", allow = 1, constant = @Constant(intValue = 5), slice = @Slice(
		from = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;movePacketsCount:I", opcode = Opcodes.PUTFIELD),
		to = @At(value = "CONSTANT", args = "stringValue={} is sending move packets too frequently ({} packets since last tick)")))
	private int onPlayerMoveModifyConstant5(int oldValue) {
		// Allow for "infinite" packets between ticks when using fixed movement.
		return gs_fixedMovement ? Integer.MAX_VALUE : oldValue;
	}
	
	@Inject(method = "onPlayerMove", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"))
	private void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
		boolean trackerFixedMovement = false;
		// Only send movement packets if the server is not running 20 ticks per second.
		if (!GSServerController.getInstance().getTpsModule().isDefaultTps()) {
			// G4mespeed is not installed, assume that the player is moving with 20 ticks per second.
			trackerFixedMovement = gs_fixedMovement || !gs_isExtensionInstalled(GSCoreExtension.UID);
		}
		
		this.gs_trackerFixedMovement = trackerFixedMovement;
		
		((GSIServerChunkManagerAccess)player.getWorld().getChunkManager()).gs_setTrackerFixedMovement(player, trackerFixedMovement);
	}

	@Inject(method = "onPlayerMove", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/server/network/ServerPlayerEntity;increaseTravelMotionStats(DDD)V"))
	private void onPlayerMoveUpdateCameraPosition(PlayerMoveC2SPacket packet, CallbackInfo ci) {
		if (gs_trackerFixedMovement)
			((GSIServerChunkManagerAccess)player.getWorld().getChunkManager()).gs_tickEntityTracker(player);
	}

	
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		
		@SuppressWarnings("unchecked")
		GSICustomPayloadPacket<ServerPlayPacketListener> payload = (GSICustomPayloadPacket<ServerPlayPacketListener>)packet;
		
		GSServerController controllerServer = GSServerController.getInstance();
		GSIPacket gsPacket = packetManger.decodePacket(payload, gs_extensionInfoList, (ServerPlayNetworkHandler)(Object)this, controllerServer.getServer());
		if (gsPacket != null) {
			gsPacket.handleOnServer(controllerServer, player);
			ci.cancel();
		}
	}
	
	@Override
	public boolean gs_isExtensionInstalled(GSExtensionUID extensionUid) {
		return gs_extensionInfoList.isExtensionInstalled(extensionUid);
	}
	
	@Override
	public boolean gs_isExtensionInstalled(GSExtensionUID extensionUid, GSVersion minimumVersion) {
		return gs_extensionInfoList.isExtensionInstalled(extensionUid, minimumVersion);
	}

	@Override
	public GSExtensionInfo gs_getExtensionInfo(GSExtensionUID extensionUid) {
		return gs_extensionInfoList.getInfo(extensionUid);
	}

	@Override
	public void gs_clearAllExtensionInfo() {
		gs_extensionInfoList.clearInfo();
	}
	
	@Override
	public void gs_addAllExtensionInfo(GSExtensionInfo[] extensionInfo) {
		gs_extensionInfoList.addAllInfo(extensionInfo);
	}

	@Override
	public void gs_addExtensionInfo(GSExtensionInfo info) {
		gs_extensionInfoList.addInfo(info);
	}
	
	@Override
	public void gs_setTranslationVersion(GSExtensionUID uid, int translationVersion) {
		gs_translationVersions.put(uid, translationVersion);
	}

	@Override
	public int gs_getTranslationVersion(GSExtensionUID uid) {
		return gs_translationVersions.getOrDefault(uid, GSTranslationModule.INVALID_TRANSLATION_VERSION);
	}
	
	@Override
	public boolean gs_isFixedMovement() {
		return gs_fixedMovement;
	}
	
	@Override
	public void gs_setFixedMovement(boolean fixedMovement) {
		this.gs_fixedMovement = fixedMovement;
	}
	
}
