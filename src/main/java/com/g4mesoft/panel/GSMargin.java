package com.g4mesoft.panel;

public class GSMargin {

	public int top;
	public int left;
	public int bottom;
	public int right;
	
	public GSMargin() {
		this(0, 0, 0, 0);
	}

	public GSMargin(int vertMargin, int horizMargin) {
		this(vertMargin, horizMargin, vertMargin, horizMargin);
	}

	public GSMargin(int margin) {
		this(margin, margin, margin, margin);
	}
	
	public GSMargin(int top, int left, int bottom, int right) {
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}
	
	public int getVertMargin() {
		return top + bottom;
	}

	public int getHorizMargin() {
		return left + right;
	}
	
	public int getTop() {
		return top;
	}
	
	public void setTop(int top) {
		this.top = top;
	}
	
	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}
	
	public int getBottom() {
		return bottom;
	}

	public void setBottom(int bottom) {
		this.bottom = bottom;
	}
	
	public int getRight() {
		return right;
	}
	
	public void setRight(int right) {
		this.right = right;
	}
}
