package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.g4mesoft.gui.GSClipAdjuster;
import com.g4mesoft.gui.GSClipRect;

import net.minecraft.client.render.BufferBuilder;

@Mixin(BufferBuilder.class)
public class GSBufferBuilderMixin implements GSIBufferBuilderAccess {

	@Shadow private boolean building;
	
	@Shadow private int vertexCount;
	
	@Shadow private double offsetX;
	@Shadow private double offsetY;
	@Shadow private double offsetZ;
	
	private GSClipRect clipRect;
	private final GSClipAdjuster adjuster = new GSClipAdjuster();
	
	@Inject(method = "next", at = @At("HEAD"))
	public void onNext(CallbackInfo ci) {
		if (clipRect != null && (vertexCount & 0x3 /* % 4 */) == 0)
			adjuster.clipPreviousShape((BufferBuilder)(Object)this, clipRect, true);
	}

	@Inject(method = "end", at = @At("HEAD"))
	public void onEnd(CallbackInfo ci) {
		if (clipRect != null && building)
			adjuster.clipPreviousShape((BufferBuilder)(Object)this, clipRect, false);
	}
	
	@Override
	public void setClip(float x0, float y0, float x1, float y1) {
		if (building)
			throw new IllegalStateException("Buffer Builder is building.");

		if (clipRect == null) {
			clipRect = new GSClipRect(x0, y0, x1, y1);
		} else {
			clipRect.setClipBounds(x0, y0, x1, y1);
		}
	}

	@Override
	public void setClip(GSClipRect clip) {
		if (building)
			throw new IllegalStateException("Buffer Builder is building.");

		if (clip == null) {
			clipRect = null;
		} else if (clipRect == null) {
			clipRect = new GSClipRect(clip);
		} else {
			clipRect.setClipBounds(clip);
		}
	}

	@Override
	public GSClipRect getClip() {
		return (clipRect == null) ? null : new GSClipRect(clipRect);
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
}
