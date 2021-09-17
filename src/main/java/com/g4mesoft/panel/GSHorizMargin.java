package com.g4mesoft.panel;

public class GSHorizMargin {

	public int left;
	public int right;
	
	public GSHorizMargin() {
		this(0, 0);
	}
	
	public GSHorizMargin(int horizMargin) {
		this(horizMargin, horizMargin);
	}

	public GSHorizMargin(int left, int right) {
		this.left = left;
		this.right = right;
	}
	
	public int getLeft() {
		return left;
	}
	
	public void setLeft(int left) {
		this.left = left;
	}
	
	public int getRight() {
		return right;
	}
	
	public void setRight(int right) {
		this.right = right;
	}
}
