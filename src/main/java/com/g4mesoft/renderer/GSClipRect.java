package com.g4mesoft.renderer;

public class GSClipRect {

	public final float x0;
	public final float y0;
	
	public final float x1;
	public final float y1;
	
	public GSClipRect(GSClipRect bounds) {
		this(bounds.x0, bounds.y0, bounds.x1, bounds.y1);
	}

	public GSClipRect(float x0, float y0, float x1, float y1) {
		this.x0 = x0;
		this.y0 = y0;
		
		this.x1 = x1;
		this.y1 = y1;
	}

	public GSClipRect offset(float clipOffsetX, float clipOffsetY) {
		return new GSClipRect(x0 + clipOffsetX, y0 + clipOffsetY, 
		                      x1 + clipOffsetX, y1 + clipOffsetY);
	}
}
