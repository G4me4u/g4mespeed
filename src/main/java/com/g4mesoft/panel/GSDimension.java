package com.g4mesoft.panel;

public final class GSDimension {

	public static final GSDimension ZERO = new GSDimension(0, 0);
	public static final GSDimension MAX_VALUE = new GSDimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	
	private final int width;
	private final int height;

	public GSDimension(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		hash += 31 * hash + height;
		hash += 31 * hash + width;
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof GSDimension) {
			GSDimension other = (GSDimension)obj;
			return width  == other.width &&
			       height == other.height;
		}
		return false;
	}
}
