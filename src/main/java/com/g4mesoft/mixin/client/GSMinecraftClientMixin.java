package com.g4mesoft.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIMinecraftClientAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.debug.GSDebug;
import com.g4mesoft.module.tps.GSBasicTickTimer;
import com.g4mesoft.module.tps.GSITickTimer;
import com.g4mesoft.module.tps.GSTpsModule;

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

	private GSClientController controller;
	private GSTpsModule tpsModule;
	
	private final GSITickTimer playerTimer = new GSBasicTickTimer(GSITickTimer.DEFAULT_MILLIS_PER_TICK);
	
	@Shadow protected abstract boolean isPaused();
	
	@Shadow protected abstract void handleInputEvents();
	
	@Inject(method = "run", at = @At(value = "FIELD", target="Lnet/minecraft/client/MinecraftClient;thread:Ljava/lang/Thread;",
			opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	public void onInit(CallbackInfo ci) {
		controller = GSClientController.getInstance();
		controller.init((MinecraftClient)(Object)this);
		tpsModule = controller.getTpsModule();
	}
	
	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
	public void onDisconnect(Screen screen, CallbackInfo ci) {
		// Check if player is null. This ensures that we only
		// call disconnect when we're leaving a play-session.
		if (this.player != null)
			controller.onDisconnectServer();
	}
	
	@Inject(method = "stop", at = @At(value = "CONSTANT", args = "stringValue=Stopping!"))
	public void onClientClose(CallbackInfo ci) {
		controller.onClientClose();
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		GSDebug.onClientTick();
		
		controller.tick(isPaused());
	}
	
	@Inject(method = "tick", at = @At(value = "FIELD", shift = Shift.AFTER, 
			opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/MinecraftClient;itemUseCooldown:I"))
	public void onTickAfterItemUseCooldownDecrement(CallbackInfo ci) {
		if (tpsModule.isMainPlayerFixedMovement()) {
			// Fix item cool-down by incrementing it.
			itemUseCooldown++;
		}
	}

	@Inject(method = "tick", at = @At(value = "FIELD", shift = Shift.AFTER, 
			opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/MinecraftClient;attackCooldown:I"))
	public void onTickAfterAttackCooldownDecrement(CallbackInfo ci) {
		if (tpsModule.isMainPlayerFixedMovement()) {
			// Fix attack cool-down by incrementing it.
			attackCooldown++;
		}
	}
	
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;tick()V"))
	public void onTickRedirectInteractionManagerTick(InGameHud inGameHud) {
		// Tick is handled elsewhere when correcting movement.
		if (!tpsModule.isMainPlayerFixedMovement())
			inGameHud.tick();
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;tick()V"))
	public void onTickRedirectInteractionManagerTick(ClientPlayerInteractionManager interactionManager) {
		// Tick is handled elsewhere when correcting movement.
		if (!tpsModule.isMainPlayerFixedMovement())
			interactionManager.tick();
	}
	
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V"))
	public void onTickRedirectHandleInputEvents(MinecraftClient ignore) {
		// Events are handled elsewhere when correcting movement.
		if (!tpsModule.isMainPlayerFixedMovement())
			handleInputEvents();
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;tick()V"))
	public void onTickRedirectGameRendererTick(GameRenderer gameRenderer) {
		// Tick is handled elsewhere when correcting movement.
		if (!tpsModule.isMainPlayerFixedMovement())
			gameRenderer.tick();
	}
	
	@Inject(method = "render", slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=tick")), 
			at = @At(value = "INVOKE", ordinal = 0, shift = Shift.AFTER, target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V"))
	public void onTickAndShouldTick(boolean tick, CallbackInfo ci) {
		playerTimer.update(Util.getMeasuringTimeMs());

		int tickCount = Math.min(playerTimer.getTickCount(), 10);
		for (int i = 0; i < tickCount; i++) {
			if (tpsModule.isMainPlayerFixedMovement())
				onTickCorrection();
			if (!paused && world != null)
				tickFixedMovementPlayers();
		}
	}
	
	private void onTickCorrection() {
		if (itemUseCooldown > 0)
			itemUseCooldown--;

		if (!paused) {
			inGameHud.tick();
		
			if (world != null)
				interactionManager.tick();
		}

		if (overlay == null && (currentScreen == null || currentScreen.passEvents)) {
			handleInputEvents();
			if (attackCooldown > 0)
				attackCooldown--;
		}
		
		if (!paused && world != null) {
			gameRenderer.tick();

			if (player != null && !player.hasVehicle() && !player.isRemoved())
				world.tickEntity(world::tickEntity, player);
		}
	}
	
	private void tickFixedMovementPlayers() {
		if (!tpsModule.isDefaultTps()) {
			for (AbstractClientPlayerEntity entity : world.getPlayers()) {
				if (entity != player && !entity.hasVehicle() && !entity.isRemoved() && tpsModule.isPlayerFixedMovement(entity))
					world.tickEntity(world::tickEntity, entity);
			}
		}
	}
	
	@ModifyArg(method = "render", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;render(FJZ)V"))
	public float onModifyGameRenderTickDelta(float oldTickDelta) {
		if (!paused && tpsModule.isMainPlayerFixedMovement())
			return playerTimer.getTickDelta();
		return oldTickDelta;
	}
	
	@Override
	public float getFixedMovementTickDelta() {
		return playerTimer.getTickDelta();
	}
}
