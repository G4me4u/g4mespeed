package com.g4mesoft.mixin.common;

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
import com.g4mesoft.access.common.GSIServerChunkCacheAccess;
import com.g4mesoft.access.common.GSIServerGamePacketListenerImplAccess;
import com.g4mesoft.core.GSCoreExtension;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class GSServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl implements GSIServerGamePacketListenerImplAccess {

	@Shadow public ServerPlayer player;
	@Shadow private int aboveGroundTickCount;

	public GSServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection,
			CommonListenerCookie clientData) {
		super(server, connection, clientData);
	}
	
	@Unique
	private final GSExtensionInfoList gs_extensionInfoList = new GSExtensionInfoList();
	@Unique
	private final Map<GSExtensionUID, Integer> gs_translationVersions = new HashMap<>();
	@Unique
	private boolean gs_fixedMovement = false;

	@Unique
	private boolean gs_trackerFixedMovement = false;

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTick(CallbackInfo ci) {
		if (gs_fixedMovement && aboveGroundTickCount > 70)
			aboveGroundTickCount--;
	}
	
	@ModifyConstant(
		method = "handleMovePlayer",
		allow = 1,
		constant = @Constant(
			intValue = 5
		), slice = @Slice(
			from = @At(
				value = "FIELD",
				opcode = Opcodes.PUTFIELD,
				target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;receivedMovePacketCount:I"
			),
			to = @At(
				value = "CONSTANT",
				args = "stringValue={} is sending move packets too frequently ({} packets since last tick)"
			)
		)
	)
	private int onHandleMovePlayerModifyConstant5(int oldValue) {
		// Allow for "infinite" packets between ticks when using fixed movement.
		return gs_fixedMovement ? Integer.MAX_VALUE : oldValue;
	}
	
	@Inject(
		method = "handleMovePlayer",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(" +
					"Lnet/minecraft/network/protocol/Packet;" +
					"Lnet/minecraft/network/PacketListener;" +
					"Lnet/minecraft/server/level/ServerLevel;" +
				")V"
		)
	)
	private void onHandleMovePlayer(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
		boolean trackerFixedMovement = false;
		// Only send movement packets if the server is not running 20 ticks per second.
		if (!GSServerController.getInstance().getTpsModule().isDefaultTps()) {
			// G4mespeed is not installed, assume that the player is moving with 20 ticks per second.
			trackerFixedMovement = gs_fixedMovement || !gs_isExtensionInstalled(GSCoreExtension.UID);
		}
		
		this.gs_trackerFixedMovement = trackerFixedMovement;
		
		((GSIServerChunkCacheAccess)player.level().getChunkSource()).gs_setTrackerFixedMovement(player, trackerFixedMovement);
	}

	@Inject(
		method = "handleMovePlayer",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/server/level/ServerPlayer;checkMovementStatistics(" +
					"D" +
					"D" +
					"D" +
				")V"
		)
	)
	private void onHandleMovePlayerUpdateCameraPosition(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
		if (gs_trackerFixedMovement)
			((GSIServerChunkCacheAccess)player.level().getChunkSource()).gs_tickEntityTracker(player);
	}
	
	@Inject(
		method = "handleCustomPayload",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onHandleCustomPayload(ServerboundCustomPayloadPacket customPayloadPacket, CallbackInfo ci) {
		if (!(this instanceof GSIServerGamePacketListenerImplAccess)) {
			// We only accept packets during play.
			return;
		}
		GSIServerGamePacketListenerImplAccess access = (GSIServerGamePacketListenerImplAccess)this;
		
		GSPacketManager packetManger = G4mespeedMod.getPacketManager();
		GSIPacket packet = packetManger.decodePacket(customPayloadPacket.payload(), access.gs_getExtensionInfoList());
		if (packet != null) {
			packetManger.handlePacket(packet, (ServerGamePacketListenerImpl)(Object)this, server, p -> {
				p.handleOnServer(GSServerController.getInstance(), access.gs_getPlayer());
			});
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
	
	@Override
	public GSExtensionInfoList gs_getExtensionInfoList() {
		return gs_extensionInfoList;
	}
	
	@Override
	public ServerPlayer gs_getPlayer() {
		return player;
	}
}
