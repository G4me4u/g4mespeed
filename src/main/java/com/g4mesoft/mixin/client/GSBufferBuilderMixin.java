package com.g4mesoft.mixin.client;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIBufferBuilderAccess;
import com.g4mesoft.renderer.GSClipAdjuster;
import com.g4mesoft.renderer.GSClipRect;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;

@Mixin(BufferBuilder.class)
public class GSBufferBuilderMixin implements GSIBufferBuilderAccess {

	@Shadow private ByteBuffer buffer;
	@Shadow private int drawMode;
	@Shadow private VertexFormat format;

	@Shadow private boolean building;
	
	@Shadow private int buildStart;
	@Shadow private int vertexCount;
	@Shadow private int elementOffset;
	
	@Unique
	private final GSClipAdjuster adjuster = new GSClipAdjuster();

	@Inject(method = "next", at = @At("RETURN"))
	public void onNextReturn(CallbackInfo ci) {
		if (building && (vertexCount & 0x3 /* % 4 */) == 0)
			adjuster.clipPreviousShape((BufferBuilder)(Object)this);
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
	public GSClipRect getClip() {
		return adjuster.getClip();
	}

	@Override
	public float getClipOffsetX() {
		return adjuster.getClipOffsetX();
	}
	
	@Override
	public float getClipOffsetY() {
		return adjuster.getClipOffsetY();
	}
	
	@Override
	public void setClipOffset(float offsetX, float offsetY) {
		adjuster.setClipOffset(offsetX, offsetY);
	}
	
	@Override
	public ByteBuffer getByteBuffer() {
		return buffer;
	}

	@Override
	public int getDrawMode() {
		return drawMode;
	}

	@Override
	public VertexFormat getVertexFormat() {
		return format;
	}

	@Override
	public int getBuildStart() {
		return buildStart;
	}
	
	@Override
	public int getVertexCount() {
		return vertexCount;
	}

	@Override
	public void setVertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
	}

	@Override
	public void setElementOffset(int elementOffset) {
		this.elementOffset = elementOffset;
	}
}
