package com.g4mesoft.access;

import java.nio.ByteBuffer;

import com.g4mesoft.gui.GSClipRect;

import net.minecraft.client.render.VertexFormat;

public interface GSIBufferBuilderAccess {

	public void setClip(float x0, float y0, float x1, float y1);

	public void setClip(GSClipRect clip);

	public GSClipRect getClip();

	public void setOffset(float offsetX, float offsetY, float offsetZ);
	
	public float getOffsetX();

	public float getOffsetY();
	
	public float getOffsetZ();

	public ByteBuffer getByteBuffer();

	public int getDrawMode();

	public VertexFormat getVertexFormat();

	public int getBuildStart();

	public int getVertexCount();

	public void setVertexCount(int vertexCount);

	public void setElementOffset(int elementOffset);

}
