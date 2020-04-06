package com.g4mesoft.access;

import com.g4mesoft.gui.GSClipRect;

public interface GSIBufferBuilderAccess {

	public void setClip(float x0, float y0, float x1, float y1);

	public void setClip(GSClipRect clip);

	public GSClipRect getClip();

	public double getOffsetX();

	public double getOffsetY();
	
	public double getOffsetZ();
}
