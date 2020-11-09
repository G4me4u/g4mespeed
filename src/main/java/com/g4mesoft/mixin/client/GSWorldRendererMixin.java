package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.renderer.GSBasicRenderer3D;
import com.g4mesoft.renderer.GSERenderPhase;
import com.g4mesoft.renderer.GSIRenderable3D;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(WorldRenderer.class)
public class GSWorldRendererMixin {

	@Shadow @Final private MinecraftClient client;
	
	private GSBasicRenderer3D renderer3d;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(MinecraftClient client, BufferBuilderStorage builderStorage, CallbackInfo ci) {
		renderer3d = new GSBasicRenderer3D(client);
	}
	
	@Inject(method = "render", at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/client/render/WorldRenderer;renderChunkDebugInfo(Lnet/minecraft/client/render/Camera;)V"))
	private void onRenderAlwaysOnTop(MatrixStack matrixStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		
		// Fix model matrix
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        
		renderer3d.begin(Tessellator.getInstance().getBuffer(), matrixStack);
		for (GSIRenderable3D renderable : GSControllerClient.getInstance().getRenderables()) {
			if (renderable.getRenderPhase() == GSERenderPhase.TRANSPARENT_LAST)
				renderable.render(renderer3d);
		}
		renderer3d.end();

		RenderSystem.popMatrix();

		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
}
