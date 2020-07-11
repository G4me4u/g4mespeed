package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.g4mesoft.gui.renderer.GSClipAdjuster;
import com.g4mesoft.gui.renderer.GSClipRect;

import net.minecraft.client.render.BufferBuilder;

@Mixin(BufferBuilder.class)
public class GSBufferBuilderMixin implements GSIBufferBuilderAccess {

	@Shadow private boolean building;
	
	@Shadow private int vertexCount;
	
	@Shadow private double offsetX;
	@Shadow private double offsetY;
	@Shadow private double offsetZ;
	
	private final GSClipAdjuster adjuster = new GSClipAdjuster();
	
	@Inject(method = "next", at = @At("HEAD"))
	public void onNext(CallbackInfo ci) {
		if ((vertexCount & 0x3 /* % 4 */) == 0)
			adjuster.clipPreviousShape((BufferBuilder)(Object)this, true);
	}

	@Inject(method = "end", at = @At("HEAD"))
	public void onEnd(CallbackInfo ci) {
		if (building)
			adjuster.clipPreviousShape((BufferBuilder)(Object)this, false);
	}
	
	@Override
	public void pushClip(float x0, float y0, float x1, float y1) {
		pushClip(new GSClipRect(x0, y0, x1, y1));
	}

	@Override
	public void pushClip(GSClipRect clip) {
		if (building)
			throw new IllegalStateException("Buffer Builder is building.");

		adjuster.pushClip(clip);
	}

	@Override
	public GSClipRect popClip() {
		return adjuster.popClip();
	}

	@Override
	public double getOffsetX() {
		return offsetX;
	}

	@Override
	public double getOffsetY() {
		return offsetY;
	}

	@Override
	public double getOffsetZ() {
		return offsetZ;
	}

	@Override
	public void setVertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
	}

	@Override
	public void setClipOffset(float offsetX, float offsetY) {
		adjuster.setClipOffset(offsetX, offsetY);
	}
	
	@Override
	public float getClipOffsetX() {
		return adjuster.getClipOffsetX();
	}

	@Override
	public float getClipOffsetY() {
		return adjuster.getClipOffsetY();
	}
}
