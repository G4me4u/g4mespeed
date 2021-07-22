package com.g4mesoft.mixin.client;

import java.util.Collection;

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
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.renderer.GSBasicRenderer3D;
import com.g4mesoft.renderer.GSERenderPhase;
import com.g4mesoft.renderer.GSIRenderable3D;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Matrix4f;

@Mixin(WorldRenderer.class)
public class GSWorldRendererMixin {

	@Shadow @Final private MinecraftClient client;
	
	private GSClientController controller;
	private GSTpsModule tpsModule;
	private GSBasicRenderer3D renderer3d;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(MinecraftClient client, BufferBuilderStorage builderStorage, CallbackInfo ci) {
		controller = GSClientController.getInstance();
		tpsModule = controller.getTpsModule();
		renderer3d = new GSBasicRenderer3D();
	}
	
	@Inject(method = "render", slice = @Slice(
			from = @At(value = "INVOKE", ordinal = 0, shift = Shift.BEFORE, target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V"),
			to = @At(value = "INVOKE", ordinal = 1, shift = Shift.AFTER, target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V")), 
			at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/client/gl/ShaderEffect;render(F)V"))
	private void onRenderTransparentLastFabulous(MatrixStack matrixStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		if (MinecraftClient.isFabulousGraphicsOrBetter())
			client.worldRenderer.getTranslucentFramebuffer().beginWrite(false);

		handleOnRenderTransparentLast(matrixStack);

		if (MinecraftClient.isFabulousGraphicsOrBetter())
			client.getFramebuffer().beginWrite(false);
	}
	
	@Inject(method = "render", at = @At(value = "INVOKE", ordinal = 1, shift = Shift.AFTER, target = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V"))
	private void onRenderTransparentLastDefault(MatrixStack matrixStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		handleOnRenderTransparentLast(matrixStack);
	}

	private void handleOnRenderTransparentLast(MatrixStack matrixStack) {
		Collection<GSIRenderable3D> renderables = controller.getRenderables();
		
		if (hasRenderPhase(renderables, GSERenderPhase.TRANSPARENT_LAST)) {
			// Rendering world border sometimes has depth and
			// depth mask disabled. Fix it here.
			RenderSystem.depthMask(true);
			RenderSystem.enableDepthTest();

			// Sometimes face culling is disabled
			RenderSystem.enableCull();
			
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableTexture();
			
			// View matrix is already uploaded to shader uniform
			matrixStack.push();
			matrixStack.loadIdentity();
			
			renderer3d.begin(Tessellator.getInstance().getBuffer(), matrixStack);
			for (GSIRenderable3D renderable : renderables) {
				if (renderable.getRenderPhase() == GSERenderPhase.TRANSPARENT_LAST)
					renderable.render(renderer3d);
			}
			renderer3d.end();

			matrixStack.pop();
	
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
		}
	}
	
	private boolean hasRenderPhase(Collection<GSIRenderable3D> renderables, GSERenderPhase phase) {
		for (GSIRenderable3D renderable : renderables) {
			if (renderable.getRenderPhase() == phase)
				return true;
		}

		return false;
	}
	
	@ModifyArg(method = "render", index = 4, at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/client/render/WorldRenderer;renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
	private float onRenderEntityModifyDeltaTick(Entity entity, double cameraX, double cameraY, double cameraZ, float deltaTick, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
		if (!client.isPaused() && (entity instanceof AbstractClientPlayerEntity)) {
			if (tpsModule.isPlayerFixedMovement((AbstractClientPlayerEntity)entity))
				return ((GSIMinecraftClientAccess)client).getFixedMovementTickDelta();
		}

		return deltaTick;
	}
	
	@Redirect(method = "render", allow = 1, require = 1, expect = 1,
			at = @At(value = "FIELD", target="Lnet/minecraft/entity/Entity;age:I", opcode = Opcodes.GETFIELD))
	private int onRenderGetEntityAge(Entity entity) {
		if (tpsModule.sPrettySand.getValue() != GSTpsModule.PRETTY_SAND_DISABLED && entity.getType() == EntityType.FALLING_BLOCK) {
			// We do not want the render positions to be modified when
			// using pretty sand (already done by position packets).
			return (entity.age == 0) ? -1 : entity.age;
		}
		return entity.age;
	}
}
