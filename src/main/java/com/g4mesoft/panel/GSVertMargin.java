package com.g4mesoft.panel;

public class GSVertMargin {

	public int top;
	public int bottom;
	
	public GSVertMargin() {
		this(0, 0);
	}

	public GSVertMargin(int vertMargin) {
		this(vertMargin, vertMargin);
	}
	
	public GSVertMargin(int top, int bottom) {
		this.top = top;
		this.bottom = bottom;
	}
	
	public int getTop() {
		return top;
	}
	
	public void setTop(int top) {
		this.top = top;
	}
	
	public int getBottom() {
		return bottom;
	}

	public void setBottom(int bottom) {
		this.bottom = bottom;
	}
}
