package com.g4mesoft.panel;

public class GSDimension {

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
}
