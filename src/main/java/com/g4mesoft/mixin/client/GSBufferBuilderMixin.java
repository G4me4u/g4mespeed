package com.g4mesoft.mixin.client;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.g4mesoft.gui.GSClipAdjuster;
import com.g4mesoft.gui.GSClipRect;
import com.g4mesoft.util.GSMathUtils;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;

@Mixin(BufferBuilder.class)
public class GSBufferBuilderMixin implements GSIBufferBuilderAccess {

	@Shadow private ByteBuffer buffer;
	@Shadow private int drawMode;
	@Shadow private VertexFormat format;

	@Shadow private boolean building;
	
	@Shadow private int buildStart;
	@Shadow private int vertexCount;
	@Shadow private int elementOffset;
	
	private float offsetX;
	private float offsetY;
	private float offsetZ;
	
	private boolean offsetVertices;
	private boolean positionFormat;
	
	private GSClipRect clipRect;

	private final GSClipAdjuster adjuster = new GSClipAdjuster();

	@Inject(method = "begin", at = @At("RETURN"))
	public void begin(int drawMode, VertexFormat format, CallbackInfo ci) {
		positionFormat = hasPosition(format);
	}
	
	@Inject(method = "next", at = @At("HEAD"))
	public void onNextHead(CallbackInfo ci) {
		if (building && positionFormat && offsetVertices)
			offsetVertexPosition(vertexCount);
	}
	
	@Inject(method = "next", at = @At("RETURN"))
	public void onNextReturn(CallbackInfo ci) {
		if (clipRect != null && building && positionFormat && (vertexCount & 0x3 /* % 4 */) == 0)
			adjuster.clipPreviousShape((BufferBuilder)(Object)this, clipRect);
	}

	private static boolean hasPosition(VertexFormat format) {
		ImmutableList<VertexFormatElement> elements = format.getElements();
		return (elements.size() > 0 && elements.get(0).getType() == VertexFormatElement.Type.POSITION);
	}
	
	private void offsetVertexPosition(int vertexIndex) {
		int index = buildStart + vertexIndex * format.getVertexSize();
		buffer.putFloat(index + 0, buffer.getFloat(index + 0) + offsetX);
		buffer.putFloat(index + 4, buffer.getFloat(index + 4) + offsetY);
		buffer.putFloat(index + 8, buffer.getFloat(index + 8) + offsetZ);
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
	public void setOffset(float offsetX, float offsetY, float offsetZ) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		
		offsetVertices = !GSMathUtils.equalsApproximate(offsetX, 0.0f) ||
		                 !GSMathUtils.equalsApproximate(offsetY, 0.0f) ||
		                 !GSMathUtils.equalsApproximate(offsetZ, 0.0f);
	}
	
	@Override
	public float getOffsetX() {
		return offsetX;
	}

	@Override
	public float getOffsetY() {
		return offsetY;
	}

	@Override
	public float getOffsetZ() {
		return offsetZ;
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
