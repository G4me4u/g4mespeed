package com.g4mesoft.panel;

public class GSLocation {

	public static final GSLocation ZERO = new GSLocation(0, 0);
	public static final GSLocation MAX_VALUE = new GSLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
	
	private final int x;
	private final int y;
	
	public GSLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
