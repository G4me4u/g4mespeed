package com.g4mesoft.mixin.client;

import java.util.LinkedHashSet;
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
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIMinecraftClientAccess;
import com.g4mesoft.access.client.GSIPistonBlockEntityAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.debug.GSDebug;
import com.g4mesoft.module.tps.GSBasicTickTimer;
import com.g4mesoft.module.tps.GSITickTimer;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

@Mixin(MinecraftClient.class)
public abstract class GSMinecraftClientMixin implements GSIMinecraftClientAccess {

	@Shadow @Final private RenderTickCounter renderTickCounter;
	@Shadow private SoundManager soundManager;
	@Shadow public ClientPlayerEntity player;
	@Shadow public ClientWorld world;
	@Shadow private boolean paused;
	@Shadow @Final private GameRenderer gameRenderer;
	@Shadow private int itemUseCooldown;
	@Shadow private int attackCooldown;
	@Shadow public ClientPlayerInteractionManager interactionManager;
	@Shadow public Screen currentScreen;
	@Shadow public Overlay overlay;
	@Shadow @Final public InGameHud inGameHud;

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
	
	@Shadow protected abstract boolean isPaused();
	
	@Shadow protected abstract void handleInputEvents();
	
	@Inject(method = "run", at = @At(value = "FIELD", target="Lnet/minecraft/client/MinecraftClient;thread:Ljava/lang/Thread;",
			opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void onInit(CallbackInfo ci) {
		gs_controller = GSClientController.getInstance();
		gs_controller.init((MinecraftClient)(Object)this);
		gs_tpsModule = gs_controller.getTpsModule();
	}
	
	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
	private void onDisconnect(Screen screen, CallbackInfo ci) {
		// Check if player is null. This ensures that we only
		// call disconnect when we're leaving a play-session.
		if (this.player != null)
			gs_controller.onDisconnectServer();
	}
	
	@Inject(method = "stop", at = @At(value = "CONSTANT", args = "stringValue=Stopping!"))
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
	
	@Inject(method = "tick", at = @At(value = "FIELD", shift = Shift.AFTER, 
			opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/MinecraftClient;itemUseCooldown:I"))
	private void onTickAfterItemUseCooldownDecrement(CallbackInfo ci) {
		if (gs_tpsModule.isMainPlayerFixedMovement()) {
			// Fix item cool-down by incrementing it.
			itemUseCooldown++;
		}
	}

	@Inject(method = "tick", at = @At(value = "FIELD", shift = Shift.AFTER, 
			opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/MinecraftClient;attackCooldown:I"))
	private void onTickAfterAttackCooldownDecrement(CallbackInfo ci) {
		if (gs_tpsModule.isMainPlayerFixedMovement()) {
			// Fix attack cool-down by incrementing it.
			attackCooldown++;
		}
	}
	
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;tick(Z)V"))
	private void onTickRedirectInteractionManagerTick(InGameHud inGameHud, boolean paused) {
		// Tick is handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			inGameHud.tick(paused);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;tick()V"))
	private void onTickRedirectInteractionManagerTick(ClientPlayerInteractionManager interactionManager) {
		// Tick is handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			interactionManager.tick();
	}
	
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V"))
	private void onTickRedirectHandleInputEvents(MinecraftClient ignore) {
		// Events are handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			handleInputEvents();
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;tick()V"))
	private void onTickRedirectGameRendererTick(GameRenderer gameRenderer) {
		// Tick is handled elsewhere when correcting movement.
		if (!gs_tpsModule.isMainPlayerFixedMovement())
			gameRenderer.tick();
	}
	
	@Inject(method = "render", slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=tick")), 
			at = @At(value = "INVOKE", ordinal = 0, shift = Shift.AFTER, target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V"))
	private void onRenderBeforeTickLoop(CallbackInfo ci) {
		if (!gs_flushingUpdates || gs_forceScheduledPistonBlockEntityUpdates) {
			// Perform piston block entity updates after runTasks()
			performScheduledPistonBlockEntityUpdates();
			gs_forceScheduledPistonBlockEntityUpdates = false;
		}

		gs_playerTimer.update0(Util.getMeasuringTimeMs());

		int tickCount = Math.min(gs_playerTimer.getTickCount0(), 10);
		for (int i = 0; i < tickCount; i++) {
			if (gs_tpsModule.isMainPlayerFixedMovement())
				onTickCorrection();
			if (!paused && world != null)
				tickFixedMovementPlayers();
		}
	}
	
	@Unique
	private void performScheduledPistonBlockEntityUpdates() {
		if (!gs_scheduledPistonBlockEntityUpdates.isEmpty()) {
			BlockPos[] positions = gs_scheduledPistonBlockEntityUpdates.toArray(new BlockPos[0]);
			gs_scheduledPistonBlockEntityUpdates.clear();
		
			for (BlockPos blockPos : positions) {
				BlockEntity blockEntity = world.getBlockEntity(blockPos);
				if (blockEntity instanceof PistonBlockEntity)
					((GSIPistonBlockEntityAccess)blockEntity).gs_handleScheduledUpdate();
			}
		}
	}
	
	@Unique
	private void onTickCorrection() {
		if (itemUseCooldown > 0)
			itemUseCooldown--;

		if (!paused) {
			inGameHud.tick(this.paused);
		
			if (world != null)
				interactionManager.tick();
		}

		if (overlay == null && (currentScreen == null || currentScreen.passEvents)) {
			handleInputEvents();
			if (attackCooldown > 0)
				attackCooldown--;
		}
		
		if (!paused && world != null)
			gameRenderer.tick();
	}
	
	@Unique
	private void tickFixedMovementPlayers() {
		if (!gs_tpsModule.isDefaultTps() || gs_tpsModule.isFixedMovementOnDefaultTps()) {
			for (AbstractClientPlayerEntity entity : world.getPlayers()) {
				if (entity != player && !entity.hasVehicle() && !entity.isRemoved() && gs_tpsModule.isPlayerFixedMovement(entity))
					world.tickEntity(world::tickEntity, entity);
			}
		}
	}
	
	@ModifyArg(method = "render", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;render(FJZ)V"))
	private float onModifyGameRenderTickDelta(float oldTickDelta) {
		if (!paused && gs_tpsModule.isMainPlayerFixedMovement())
			return gs_playerTimer.getTickDelta0();
		return oldTickDelta;
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
