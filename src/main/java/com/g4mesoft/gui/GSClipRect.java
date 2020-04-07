package com.g4mesoft.gui;

public class GSClipRect {

	public float x0;
	public float y0;
	
	public float x1;
	public float y1;
	
	public GSClipRect(GSClipRect bounds) {
		setClipBounds(bounds);
	}

	public GSClipRect(float x0, float y0, float x1, float y1) {
		setClipBounds(x0, y0, x1, y1);
	}

	public void setClipBounds(GSClipRect bounds) {
		setClipBounds(bounds.x0, bounds.y0, bounds.x1, bounds.y1);
	}
	
	public void setClipBounds(float x0, float y0, float x1, float y1) {
		this.x0 = x0;
		this.y0 = y0;
		
		this.x1 = x1;
		this.y1 = y1;
	}
}
