package com.g4mesoft.mixin.client;

import java.util.LinkedHashSet;
import java.util.Set;

import org.lwjgl.input.Mouse;
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
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.client.GSIClientWorldAccess;
import com.g4mesoft.access.client.GSIMinecraftAccess;
import com.g4mesoft.access.client.GSIMovingBlockEntityAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.compat.GSTweakerooCompat;
import com.g4mesoft.debug.GSDebug;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.module.tps.GSBasicTickTimer;
import com.g4mesoft.module.tps.GSITickTimer;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.client.ClientPlayerInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportCategory;
import net.minecraft.util.math.BlockPos;

@Mixin(Minecraft.class)
public abstract class GSMinecraftMixin implements GSIMinecraftAccess {

	@Shadow public ClientWorld world;
	@Shadow private boolean paused;
	@Shadow @Final private GameRenderer gameRenderer;
	@Shadow private int itemUseDelay;
	@Shadow private int attackCooldown;
	@Shadow public ClientPlayerInteractionManager interactionManager;
	@Shadow public Screen screen;
	@Shadow public GameGui gui;
	@Shadow long sysTime;

	@Unique
	private GSClientController gs_controller;
	@Unique
	private GSTpsModule gs_tpsModule;
	
	private final GSITickTimer gs_playerTimer = new GSBasicTickTimer(GSITickTimer.DEFAULT_MILLIS_PER_TICK);
	
	@Unique
	private boolean gs_flushingUpdates = false;
	@Unique
	private boolean gs_forceScheduledPistonBlockEntityUpdates = false;
	@Unique
	private final Set<BlockPos> gs_scheduledPistonBlockEntityUpdates = new LinkedHashSet<>();
	
	@Unique
	private GSTweakerooCompat gs_tweakerooCompat;
	@Unique
	private GSKeyManager gs_keyManager;
	@Unique
	private boolean gs_tweakerooWasCameraEntityEnabled = false;
	
	@Shadow protected abstract boolean isPaused();
	
	@Shadow protected abstract void handleMouseEvents();

	@Shadow protected abstract void handleKeyboardEvents();
	
	@Inject(
		method = "init()V",
		at = @At("RETURN")
	)
	public void onInit(CallbackInfo ci) {
		gs_controller = GSClientController.getInstance();
		gs_controller.init((Minecraft)(Object)this);
		gs_tpsModule = gs_controller.getTpsModule();
		gs_tweakerooCompat = G4mespeedMod.getTweakerooCompat();
		gs_keyManager = gs_controller.getKeyManager();
	}
	
	@Inject(
		method =
			"setWorld(" +
				"Lnet/minecraft/client/world/ClientWorld;" +
				"Ljava/lang/String;" +
			")V",
		at = @At("HEAD")
	)
	private void onDisconnect(ClientWorld world, String title, CallbackInfo ci) {
		if (world == null && this.world != null)
			gs_controller.onDisconnectServer();
	}
	
	@Inject(
		method = "stop",
		at = @At(
			value = "CONSTANT",
			args = "stringValue=Stopping!"
		)
	)
	private void onClientClose(CallbackInfo ci) {
		gs_controller.onClientClose();
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		GSDebug.onClientTick();
		
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
			target = "Lnet/minecraft/client/Minecraft;itemUseDelay:I"
		)
	)
	private void onTickAfterItemUseCooldownDecrement(CallbackInfo ci) {
		if (gs_tpsModule.isMainPlayerFixedMovement()) {
			// Fix item cool-down by incrementing it.
			itemUseDelay++;
		}
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER, 
			opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/client/Minecraft;attackCooldown:I"
		)
	)
	private void onTickAfterAttackCooldownDecrement(CallbackInfo ci) {
		if (gs_tpsModule.isMainPlayerFixedMovement()) {
			// Fix attack cool-down by incrementing it.
			attackCooldown++;
		}
	}
	
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GameGui;tick()V"
		)
	)
	private void onTickRedirectInteractionManagerTick(GameGui inGameHud) {
		// Tick is handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			inGameHud.tick();
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/ClientPlayerInteractionManager;tick()V"
		)
	)
	private void onTickRedirectInteractionManagerTick(ClientPlayerInteractionManager interactionManager) {
		// Tick is handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			interactionManager.tick();
	}
	
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screen/Screen;handleInputs()V"
		)
	)
	private void onTickRedirectScreenHandleInputs(Screen screen) {
		// Events are handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			screen.handleInputs();
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screen/Screen;tick()V"
		)
	)
	private void onTickRedirectScreenTick(Screen screen) {
		// Tick is handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			screen.tick();
	}
	
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Minecraft;handleMouseEvents()V"
		)
	)
	private void onTickRedirectHandleMouseEvents(Minecraft ignore) {
		// Events are handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			handleMouseEvents();
	}
	
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Minecraft;handleKeyboardEvents()V"
		)
	)
	private void onTickRedirectHandleKeyboardEvents(Minecraft ignore) {
		// Events are handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			handleKeyboardEvents();
	}

	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/GameRenderer;tick()V"
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
		method = "runGame",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target = "Lnet/minecraft/client/Minecraft;tick()V"
		)
	)
	private void onRenderAfterTick(CallbackInfo ci) {
		// Re-enable tweakeroo camera after malilib tick handler (aka. after tick returns).
		if (gs_tweakerooWasCameraEntityEnabled) {
			gs_tweakerooCompat.enableCameraEntityTicking();
			gs_tweakerooWasCameraEntityEnabled = false;
		}
	}
	
	@Inject(
		method = "runGame",
		slice = @Slice(
			from = @At(
				value = "CONSTANT",
				args = "stringValue=tick"
			)
		), 
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/util/profiler/Profiler;push(" +
					"Ljava/lang/String;" +
				")V"
		)
	)
	private void onRenderBeforeTickLoop(CallbackInfo ci) {
		if (!gs_flushingUpdates || gs_forceScheduledPistonBlockEntityUpdates) {
			// Perform piston block entity updates after runTasks()
			performScheduledPistonBlockEntityUpdates();
			gs_forceScheduledPistonBlockEntityUpdates = false;
		}

		gs_playerTimer.update(System.currentTimeMillis());

		if (!gs_tpsModule.isDefaultTps() || gs_tpsModule.isFixedMovementOnDefaultTps()) {
			int tickCount = Math.min(gs_playerTimer.getTickCount(), 10);
			for (int i = 0; i < tickCount; i++) {
				if (gs_tpsModule.isMainPlayerFixedMovement()) {
					onTickCorrection();
					if (gs_tpsModule.cTweakerooFreecamHack.get() && gs_tweakerooCompat.isCameraEntityRetreived())
						gs_tweakerooCompat.tickCameraEntityMovement();
				}
				if (!paused && world != null)
					((GSIClientWorldAccess)world).gs_tickFixedMovementPlayers();
			}
		}
	}
	
	@Unique
	private void performScheduledPistonBlockEntityUpdates() {
		if (!gs_scheduledPistonBlockEntityUpdates.isEmpty()) {
			BlockPos[] positions = gs_scheduledPistonBlockEntityUpdates.toArray(new BlockPos[0]);
			gs_scheduledPistonBlockEntityUpdates.clear();
		
			for (BlockPos blockPos : positions) {
				BlockEntity blockEntity = world.getBlockEntity(blockPos);
				if (blockEntity instanceof MovingBlockEntity)
					((GSIMovingBlockEntityAccess)blockEntity).gs_handleScheduledUpdate();
			}
		}
	}
	
	@Unique
	private void onTickCorrection() {
		if (itemUseDelay > 0)
			itemUseDelay--;

		if (!paused) {
			gui.tick();
		
			if (world != null)
				interactionManager.tick();
		}

		if (screen != null) {
			try {
				screen.handleInputs();
			} catch (Throwable throwable) {
				CrashReport crashReport = CrashReport.of(throwable, "Updating screen events");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Affected screen");
				crashReportCategory.add("Screen name", () -> screen.getClass().getCanonicalName());
				throw new CrashException(crashReport);
			}

			if (screen != null) {
				try {
					screen.tick();
				} catch (Throwable throwable) {
					CrashReport crashReport = CrashReport.of(throwable, "Ticking screen");
					CrashReportCategory crashReportCategory = crashReport.addCategory("Affected screen");
					crashReportCategory.add("Screen name", () -> screen.getClass().getCanonicalName());
					throw new CrashException(crashReport);
				}
			}
		}
		
		if (screen == null || screen.passEvents) {
			handleMouseEvents();
			if (attackCooldown > 0)
				attackCooldown--;
			handleKeyboardEvents();
		}
		
		if (!paused && world != null)
			gameRenderer.tick();
		
		sysTime = Minecraft.getTime();
	}
	
	@ModifyArg(
		method = "runGame",
		index = 0,
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/client/render/GameRenderer;render(" +
					"F" +
					"J" +
				")V"
		)
	)
	private float onModifyGameRenderTickDelta(float oldTickDelta) {
		if (!paused && gs_tpsModule.isMainPlayerFixedMovement())
			return gs_playerTimer.getTickDelta0();
		return oldTickDelta;
	}

	@Inject(
		method = "handleKeyboardEvents",
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			shift = Shift.BEFORE,
			target =
				"Lorg/lwjgl/input/Keyboard;getEventKey()I"
		)
	)
	private void onHandleKeyboardEventsKeyboardNext(CallbackInfo ci) {
		gs_keyManager.handleKeyboard();
	}
	
	@Inject(
		method = "handleKeyboardEvents",
		expect = 1,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/client/options/KeyBinding;click(" +
					"I" +
				")V"
		)
	)
	private void onHandleKeyboardEventsKeyBindingPress(CallbackInfo ci) {
		gs_keyManager.dispatchEvents(GSEKeyEventType.PRESS);
	}
	
	@Inject(
		method = "handleKeyboardEvents",
		at = @At(
			value = "INVOKE",
			ordinal = 2,
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/client/options/KeyBinding;set(" +
					"I" +
					"Z" +
				")V"
		)
	)
	private void onHandleKeyboardEventsKeyBindingRelease(CallbackInfo ci) {
		gs_keyManager.dispatchEvents(GSEKeyEventType.RELEASE);
	}
	
	@Inject(
		method = "handleMouseEvents",
		expect = 1,
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Lorg/lwjgl/input/Mouse;getEventButton()I"
		)
	)
	private void onHandleMouseEventsMouseNext(CallbackInfo ci) {
		gs_keyManager.handleMouse();
	}
	
	@Inject(
		method = "handleMouseEvents",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/client/options/KeyBinding;set(" +
					"I" +
					"Z" +
				")V"
		)
	)
	private void onHandleMouseEventsKeyBindingSet(CallbackInfo ci) {
		if (Mouse.getEventButtonState()) {
			gs_keyManager.dispatchEvents(GSEKeyEventType.PRESS);
		} else {
			gs_keyManager.dispatchEvents(GSEKeyEventType.RELEASE);
		}
	}
	
	@Redirect(
		method = "openScreen",
		at = @At(
			value = "INVOKE",
			target =
				"Lorg/lwjgl/input/Mouse;next()Z"
		)
	)
	private boolean onOpenScreenRedirectMouseNext() {
		if (Mouse.next() && gs_keyManager != null) {
			gs_keyManager.handleMouse();
			return true;
		}
		return false;
	}

	@Redirect(
		method = "openScreen",
		at = @At(
			value = "INVOKE",
			target =
			"Lorg/lwjgl/input/Keyboard;next()Z"
		)
	)
	private boolean onOpenScreenRedirectKeyboardNext() {
		if (Mouse.next() && gs_keyManager != null) {
			gs_keyManager.handleKeyboard();
			return true;
		}
		return false;
	}
	
	@Override
	public void gs_setFlushingBlockEntityUpdates(boolean flushingUpdates) {
		this.gs_flushingUpdates = flushingUpdates;

		if (!flushingUpdates)
			gs_forceScheduledPistonBlockEntityUpdates = true;
	}
	
	@Override
	public void gs_schedulePistonBlockEntityUpdate(BlockPos blockPos) {
		gs_scheduledPistonBlockEntityUpdates.add(blockPos);
	}
	
	@Override
	public float gs_getFixedMovementTickDelta() {
		return gs_playerTimer.getTickDelta0();
	}
}
