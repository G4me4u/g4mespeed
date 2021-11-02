package com.g4mesoft.panel;

public final class GSLocation {

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
	
	@Override
	public int hashCode() {
		int hash = 0;
		hash += 31 * hash + y;
		hash += 31 * hash + x;
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof GSLocation) {
			GSLocation other = (GSLocation)obj;
			return x == other.x && y == other.y;
		}
		return false;
	}
}
