package com.g4mesoft.access;

import java.nio.ByteBuffer;

import com.g4mesoft.gui.GSClipRect;

import net.minecraft.client.render.VertexFormat;

public interface GSIBufferBuilderAccess {

	public void pushClip(float x0, float y0, float x1, float y1);

	public void pushClip(GSClipRect clip);

	public GSClipRect popClip();

	public void setOffset(float offsetX, float offsetY, float offsetZ);
	
	public float getOffsetX();

	public float getOffsetY();
	
	public float getOffsetZ();

	public void setClipOffset(float xOffset, float yOffset);
	
	public float getClipXOffset();
	
	public float getClipYOffset();

	public ByteBuffer getByteBuffer();

	public int getDrawMode();

	public VertexFormat getVertexFormat();

	public int getBuildStart();

	public int getVertexCount();

	public void setVertexCount(int vertexCount);

	public void setElementOffset(int elementOffset);
	
}
