package com.g4mesoft.access.client;

import java.nio.ByteBuffer;

import com.g4mesoft.renderer.GSClipRect;

import net.minecraft.client.render.VertexFormat;

public interface GSIBufferBuilderAccess {

	public void pushClip(float x0, float y0, float x1, float y1);

	public void pushClip(GSClipRect clip);

	public GSClipRect popClip();

	public GSClipRect getClip();

	public float getClipOffsetX();

	public float getClipOffsetY();
	
	public void setClipOffset(float offsetX, float offsetY);
	
	public ByteBuffer getByteBuffer();

	public int getDrawMode();

	public VertexFormat getVertexFormat();

	public int getBuildStart();

	public int getVertexCount();

	public void setVertexCount(int vertexCount);

	public void setElementOffset(int elementOffset);
	
}
