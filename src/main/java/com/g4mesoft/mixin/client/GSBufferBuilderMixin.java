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
	private final GSClipAdjuster gs_adjuster = new GSClipAdjuster();

	@Inject(
		method = "next", 
		at = @At("RETURN")
	)
	public void onNextReturn(CallbackInfo ci) {
		if (building && (vertexCount & 0x3 /* % 4 */) == 0)
			gs_adjuster.clipPreviousShape((BufferBuilder)(Object)this);
	}
	
	@Override
	public void gs_pushClip(float x0, float y0, float x1, float y1) {
		gs_pushClip(new GSClipRect(x0, y0, x1, y1));
	}

	@Override
	public void gs_pushClip(GSClipRect clip) {
		if (building)
			throw new IllegalStateException("Buffer Builder is building.");

		gs_adjuster.pushClip(clip);
	}
	
	@Override
	public GSClipRect gs_popClip() {
		return gs_adjuster.popClip();
	}

	@Override
	public GSClipRect gs_getClip() {
		return gs_adjuster.getClip();
	}

	@Override
	public ByteBuffer gs_getByteBuffer() {
		return buffer;
	}

	@Override
	public int gs_getDrawMode() {
		return drawMode;
	}

	@Override
	public VertexFormat gs_getVertexFormat() {
		return format;
	}

	@Override
	public int gs_getBuildStart() {
		return buildStart;
	}
	
	@Override
	public int gs_getVertexCount() {
		return vertexCount;
	}

	@Override
	public void gs_setVertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
	}

	@Override
	public void gs_setElementOffset(int elementOffset) {
		this.elementOffset = elementOffset;
	}
}
