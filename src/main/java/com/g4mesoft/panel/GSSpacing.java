package com.g4mesoft.panel;

public final class GSSpacing {

	public int top;
	public int left;
	public int bottom;
	public int right;
	
	public GSSpacing() {
		this(0, 0, 0, 0);
	}

	public GSSpacing(int spacing) {
		this(spacing, spacing, spacing, spacing);
	}

	public GSSpacing(int vertical, int horizontal) {
		this(vertical, horizontal, vertical, horizontal);
	}
	
	public GSSpacing(int top, int left, int bottom, int right) {
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}
	
	public int getVertical() {
		return top + bottom;
	}

	public int getHorizontal() {
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
	
	@Override
	public int hashCode() {
		int hash = 0;
		hash += 31 * hash + right;
		hash += 31 * hash + bottom;
		hash += 31 * hash + left;
		hash += 31 * hash + top;
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof GSSpacing) {
			GSSpacing other = (GSSpacing)obj;
			return top == other.top &&
			       left == other.left &&
			       bottom == other.bottom &&
			       right == other.right;
		}
		return false;
	}
}
