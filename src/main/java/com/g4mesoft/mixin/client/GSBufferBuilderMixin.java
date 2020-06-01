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
	
	private boolean positionFormat;
	
	private final GSClipAdjuster adjuster = new GSClipAdjuster();

	@Inject(method = "begin", at = @At("RETURN"))
	public void begin(int drawMode, VertexFormat format, CallbackInfo ci) {
		positionFormat = hasPosition(format);
	}
	
	@Inject(method = "next", at = @At("RETURN"))
	public void onNextReturn(CallbackInfo ci) {
		if (building && positionFormat && (vertexCount & 0x3 /* % 4 */) == 0)
			adjuster.clipPreviousShape((BufferBuilder)(Object)this);
	}

	private static boolean hasPosition(VertexFormat format) {
		ImmutableList<VertexFormatElement> elements = format.getElements();
		return (elements.size() > 0 && elements.get(0).getType() == VertexFormatElement.Type.POSITION);
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
	public void setClipOffset(float xOffset, float yOffset) {
		adjuster.setClipOffset(xOffset, yOffset);
	}
	
	@Override
	public float getClipXOffset() {
		return adjuster.getClipXOffset();
	}
	
	@Override
	public float getClipYOffset() {
		return adjuster.getClipYOffset();
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
