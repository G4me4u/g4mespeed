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

	public GSClipRect intersection(GSClipRect other) {
		float _x0 = Math.max(x0, other.x0);
		float _y0 = Math.max(y0, other.y0);
		float _x1 = Math.min(x1, other.x1);
		float _y1 = Math.min(y1, other.y1);
		return new GSClipRect(_x0, _y0, _x1, _y1);
	}

	public GSClipRect union(GSClipRect other) {
		float _x0 = Math.min(x0, other.x0);
		float _y0 = Math.min(y0, other.y0);
		float _x1 = Math.max(x1, other.x1);
		float _y1 = Math.max(y1, other.y1);
		return new GSClipRect(_x0, _y0, _x1, _y1);
	}
}
