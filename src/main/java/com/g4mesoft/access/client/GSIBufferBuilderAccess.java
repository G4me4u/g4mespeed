package com.g4mesoft.access.client;

import java.nio.ByteBuffer;

import com.g4mesoft.renderer.GSClipRect;

import net.minecraft.client.render.VertexFormat;

public interface GSIBufferBuilderAccess {

	public void gs_pushClip(float x0, float y0, float x1, float y1);

	public void gs_pushClip(GSClipRect clip);

	public GSClipRect gs_popClip();

	public GSClipRect gs_getClip();

	public ByteBuffer gs_getByteBuffer();

	public int gs_getDrawMode();

	public VertexFormat gs_getVertexFormat();

	public int gs_getBuildStart();

	public int gs_getVertexCount();

	public void gs_setVertexCount(int vertexCount);

	public void gs_setElementOffset(int elementOffset);
	
}
