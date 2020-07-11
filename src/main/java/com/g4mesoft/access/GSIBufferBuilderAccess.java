package com.g4mesoft.access;

import com.g4mesoft.gui.renderer.GSClipRect;

public interface GSIBufferBuilderAccess {

	public void pushClip(float x0, float y0, float x1, float y1);

	public void pushClip(GSClipRect clip);

	public GSClipRect popClip();

	public double getOffsetX();

	public double getOffsetY();
	
	public double getOffsetZ();

	public void setVertexCount(int vertexCount);

	public void setClipOffset(float offsetX, float offsetY);
	
	public float getClipOffsetX();

	public float getClipOffsetY();
	
}
