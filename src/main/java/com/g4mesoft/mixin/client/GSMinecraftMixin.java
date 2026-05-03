package com.g4mesoft.mixin.client;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.client.GSIClientLevelAccess;
import com.g4mesoft.access.client.GSIEntityAccess;
import com.g4mesoft.access.client.GSIMinecraftAccess;
import com.g4mesoft.access.client.GSIPistonMovingBlockEntityAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.compat.GSTweakerooCompat;
import com.g4mesoft.debug.GSDebug;
import com.g4mesoft.module.tps.GSBasicTickTimer;
import com.g4mesoft.module.tps.GSDeltaTrackerTimerWrapper;
import com.g4mesoft.module.tps.GSITickTimer;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;

@Mixin(Minecraft.class)
public abstract class GSMinecraftMixin implements GSIMinecraftAccess {

	@Shadow public ClientLevel level;
	@Shadow private boolean pause;
	@Shadow @Final private GameRenderer gameRenderer;
	@Shadow private int rightClickDelay;
	@Shadow private int missTime;
	@Shadow public MultiPlayerGameMode gameMode;
	@Shadow public Screen screen;
	@Shadow public Overlay overlay;
	@Shadow @Final public Gui gui;

	@Unique
	private GSClientController gs_controller;
	@Unique
	private GSTpsModule gs_tpsModule;
	
	private final GSDeltaTrackerTimerWrapper gs_playerTimer =
			new GSDeltaTrackerTimerWrapper(new GSBasicTickTimer(GSITickTimer.DEFAULT_MILLIS_PER_TICK));
	
	@Unique
	private boolean gs_flushingUpdates = false;
	@Unique
	private boolean gs_forceScheduledPistonBlockEntityUpdates = false;
	@Unique
	private final Set<BlockPos> gs_scheduledPistonBlockEntityUpdates = new LinkedHashSet<>();
	
	@Unique
	private GSTweakerooCompat gs_tweakerooCompat;
	@Unique
	private boolean gs_tweakerooWasCameraEntityEnabled = false;
	
	@Shadow protected abstract boolean isPaused();
	
	@Shadow protected abstract void handleKeybinds();
	
	@Shadow protected abstract Optional<Holder<Dialog>> getQuickActionsDialog();
	
	@Inject(
		method = "run",
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER,
			opcode = Opcodes.PUTFIELD,
			target =
				"Lnet/minecraft/client/Minecraft;gameThread:Ljava/lang/Thread;"
		)
	)
	private void onInit(CallbackInfo ci) {
		gs_controller = GSClientController.getInstance();
		gs_controller.init((Minecraft)(Object)this);
		gs_tpsModule = gs_controller.getTpsModule();
		gs_tweakerooCompat = G4mespeedMod.getTweakerooCompat();
	}
	
	@Inject(
		method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Lnet/minecraft/client/Minecraft;clearDownloadedResourcePacks(" +
				")V"
		)
	)
	private void onClearDownloadedResourcePacks(CallbackInfo ci) {
		gs_controller.onDisconnectServer();
	}
	
	@Inject(
		method = "destroy",
		at = @At(
			value = "CONSTANT",
			args = "stringValue=Stopping!"
		)
	)
	private void onDestroy(CallbackInfo ci) {
		gs_controller.onClientClose();
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTick(CallbackInfo ci) {
		GSDebug.onClientTick();
		
		if (level != null) {
			((GSIClientLevelAccess)level).gs_forEachEntity(e -> {
				((GSIEntityAccess)e).gs_preTick();
			});
		}
		
		gs_controller.tick(isPaused());
		
		if (gs_flushingUpdates) {
			// Server is very slow. Perform scheduled updates anyway.
			gs_forceScheduledPistonBlockEntityUpdates = true;
		}
	}
	
	@Inject(
		method = "tick",
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER, 
			opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I"
		)
	)
	private void onTickAfterItemUseCooldownDecrement(CallbackInfo ci) {
		if (gs_tpsModule.isMainPlayerFixedMovement()) {
			// Fix item cool-down by incrementing it.
			rightClickDelay++;
		}
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER, 
			opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/client/Minecraft;missTime:I"
		)
	)
	private void onTickAfterAttackCooldownDecrement(CallbackInfo ci) {
		if (gs_tpsModule.isMainPlayerFixedMovement()) {
			// Fix attack cool-down by incrementing it.
			missTime++;
		}
	}
	
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/Gui;tick(Z)V"
		)
	)
	private void onTickRedirectInteractionManagerTick(Gui gui, boolean paused) {
		// Tick is handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			gui.tick(paused);
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;tick()V"
		)
	)
	private void onTickRedirectInteractionManagerTick(MultiPlayerGameMode gameMode) {
		// Tick is handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			gameMode.tick();
	}
	
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Minecraft;handleKeybinds()V"
		)
	)
	private void onTickRedirectHandleKeybinds(Minecraft ignore) {
		// Events are handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			handleKeybinds();
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GameRenderer;tick()V"
		)
	)
	private void onTickRedirectGameRendererTick(GameRenderer gameRenderer) {
		// Tick is handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			gameRenderer.tick();
	}
	
	@Inject(
		method = "tick",
		at = @At(
			value = "RETURN",
			shift = Shift.BEFORE
		)
	)
	private void onTickBeforeReturn(CallbackInfo ci) {
		if (gs_tpsModule.cTweakerooFreecamHack.get()) {
			// The malilib tick handler is invoked at return. Disable tweakeroo camera here.
			if (gs_tweakerooCompat.isCameraEntityRetreived() && gs_tpsModule.isMainPlayerFixedMovement()) {
				gs_tweakerooWasCameraEntityEnabled = gs_tweakerooCompat.isCameraEntityEnabled();
				if (gs_tweakerooWasCameraEntityEnabled)
					gs_tweakerooCompat.disableCameraEntity();
			}
		}
	}

	@Inject(
		method = "runTick",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target = "Lnet/minecraft/client/Minecraft;tick()V"
		)
	)
	private void onRunTickAfterTick(CallbackInfo ci) {
		// Re-enable tweakeroo camera after malilib tick handler (aka. after tick returns).
		if (gs_tweakerooWasCameraEntityEnabled) {
			gs_tweakerooCompat.enableCameraEntityTicking();
			gs_tweakerooWasCameraEntityEnabled = false;
		}
	}
	
	@Inject(
		method = "runTick",
		allow = 1,
		at = @At(
			value = "INVOKE_STRING",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/util/profiling/ProfilerFiller;push(" +
					"Ljava/lang/String;" +
				")V",
			args = "ldc=tick"
		)
	)
	private void onRunTickBeforeTickLoop(CallbackInfo ci) {
		if (!gs_flushingUpdates || gs_forceScheduledPistonBlockEntityUpdates) {
			// Perform piston block entity updates after runTasks()
			performScheduledPistonBlockEntityUpdates();
			gs_forceScheduledPistonBlockEntityUpdates = false;
		}

		gs_playerTimer.update0(Util.getMillis());

		if (!gs_tpsModule.isDefaultTps() || gs_tpsModule.isFixedMovementOnDefaultTps()) {
			int tickCount = Math.min(gs_playerTimer.getTickCount0(), 10);
			for (int i = 0; i < tickCount; i++) {
				if (gs_tpsModule.isMainPlayerFixedMovement()) {
					onTickCorrection();
					if (gs_tpsModule.cTweakerooFreecamHack.get() && gs_tweakerooCompat.isCameraEntityRetreived())
						gs_tweakerooCompat.tickCameraEntityMovement();
				}
				if (!pause && level != null)
					((GSIClientLevelAccess)level).gs_tickFixedMovementPlayers();
			}
		}
	}
	
	@Unique
	private void performScheduledPistonBlockEntityUpdates() {
		if (!gs_scheduledPistonBlockEntityUpdates.isEmpty()) {
			BlockPos[] positions = gs_scheduledPistonBlockEntityUpdates.toArray(new BlockPos[0]);
			gs_scheduledPistonBlockEntityUpdates.clear();
		
			for (BlockPos blockPos : positions) {
				BlockEntity blockEntity = level.getBlockEntity(blockPos);
				if (blockEntity instanceof PistonMovingBlockEntity)
					((GSIPistonMovingBlockEntityAccess)blockEntity).gs_handleScheduledUpdate();
			}
		}
	}
	
	@Unique
	private void onTickCorrection() {
		if (rightClickDelay > 0)
			rightClickDelay--;

		gui.tick(this.pause);

		if (!pause && level != null)
			gameMode.tick();

		if (overlay == null && screen == null) {
			handleKeybinds();
			if (missTime > 0)
				missTime--;
		}
		
		if (!pause && level != null)
			gameRenderer.tick();
	}
	
	@ModifyArg(
		method = "runTick",
		index = 0,
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/client/renderer/GameRenderer;render(" +
					"Lnet/minecraft/client/DeltaTracker;" +
					"Z" +
				")V"
		)
	)
	private DeltaTracker onModifyGameRenderTickDelta(DeltaTracker oldTimer) {
		if (!pause && gs_tpsModule.isMainPlayerFixedMovement())
			return gs_playerTimer;
		return oldTimer;
	}
	
	@ModifyExpressionValue(
		method = "handleKeybinds",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/client/Minecraft;getQuickActionsDialog(" +
				")Ljava/util/Optional;"
		)
	)
	private Optional<Holder<Dialog>> modifyHandleKeybindsGetQuickActionsDialog(Optional<Holder<Dialog>> original) {
		return gs_controller.isQuickActionsKeybindOverride() ? Optional.empty() : original;
	}
	
	@Override
	public void gs_setFlushingBlockEntityUpdates(boolean flushingUpdates) {
		this.gs_flushingUpdates = flushingUpdates;

		if (!flushingUpdates)
			gs_forceScheduledPistonBlockEntityUpdates = true;
	}
	
	@Override
	public void gs_schedulePistonMovingBlockEntityUpdate(BlockPos blockPos) {
		gs_scheduledPistonBlockEntityUpdates.add(blockPos);
	}
	
	@Override
	public float gs_getFixedMovementTickDelta() {
		return gs_playerTimer.getPartialTick0();
	}
	
	@Override
	public Optional<Holder<Dialog>> gs_getQuickActionsDialog() {
		return getQuickActionsDialog();
	}
}
