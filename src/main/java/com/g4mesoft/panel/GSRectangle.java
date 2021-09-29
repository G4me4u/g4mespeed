package com.g4mesoft.panel;

public final class GSRectangle {

	public int x;
	public int y;
	public int width;
	public int height;
	
	public GSRectangle() {
		this(0, 0, 0, 0);
	}
	
	public GSRectangle(GSRectangle other) {
		this(other.x, other.y, other.width, other.height);
	}

	public GSRectangle(GSLocation location, int width, int height) {
		this(location.getX(), location.getY(), width, height);
	}

	public GSRectangle(int x, int y, GSDimension size) {
		this(x, y, size.getWidth(), size.getHeight());
	}

	public GSRectangle(GSLocation location, GSDimension size) {
		this(location.getX(), location.getY(), size.getWidth(), size.getHeight());
	}
	
	public GSRectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public GSLocation getLocation() {
		return new GSLocation(x, y);
	}

	public void setLocation(GSLocation location) {
		setLocation(location.getX(), location.getY());
	}
	
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public GSDimension getSize() {
		return new GSDimension(width, height);
	}
	
	public void setSize(GSDimension size) {
		setSize(size.getWidth(), size.getHeight());
	}
	
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean contains(GSRectangle other) {
		return contains(other.x, other.y, other.width, other.height);
	}
	
	public boolean contains(int x, int y, int width, int height) {
		int _width = this.width;
		int _height = this.height;
		
		// Either this rectangle or the given is non-existent.
		if ((_width | width | _height | height) < 0)
			return false;
		
		int _x = this.x;
		int _y = this.y;
		
		// Other rectangle is definitely outside
		if (x < _x || y < _y)
			return false;

		// We can not have overflow since dimensions
		// are guaranteed positive.
		int _x1 = _x + _width;
		int x1 = x + width;
		
		if (x1 <= x) {
			// x + width has overflowed. We do not contain
			// the given rectangle if _x + _width did not
			// overflow or _width is zero or we overflowed
			// less.
			if (_x1 >= _x || x1 > _x1)
				return false;
		} else {
			// x + width did not overflow. We do not contain
			// the given rectangle if _x + _width did also
			// not overflow and x + width > _x + _width.
			if (_x1 >= _x && x1 > _x1)
				return false;
		}
		
		int _y1 = _y + _height;
		int y1 = y + height;
		
		if (y1 <= y) {
			if (_y1 >= _y || y1 > _y1)
				return false;
		} else {
			if (_y1 >= _y && y1 > _y1)
				return false;
		}
		
		return true;
	}
	
	public boolean contains(GSLocation location) {
		return contains(location.getX(), location.getY());
	}
	
	public boolean contains(int x, int y) {
		int _width = this.width;
		int _height = this.height;
		
		// Rectangle is non-existent
		if ((_width | _height) < 0)
			return false;
		
		int _x = this.x;
		int _y = this.y;
		
		// Outside either left or top edge
		if (x < _x || y < _y)
			return false;

		// Guaranteed to never underflow
		int _x1 = _x + _width;
		int _y1 = _y + _height;
		
		// Either we overflow or the point is contained
		return (_x1 < _x || _x1 > x) && 
		       (_y1 < _y || _y1 > y);
	}
	
	public boolean intersects(GSRectangle other) {
		return intersects(other.x, other.y, other.width, other.height);
	}
	
	public boolean intersects(int x, int y, int width, int height) {
		int _width = this.width;
		int _height = this.height;
		
		// Either of the rectangles are empty
		if (_width <= 0 || _height <= 0 || width <= 0 || height <= 0)
			return false;
		
		int _x = this.x;
		int _y = this.y;
		
		int _x1 = _x + _width;
		int _y1 = _y + _height;
		int x1 = x + width;
		int y1 = y + height;
		
		// Either we overflow or we intersect normally
		return (_x1 < _x || _x1 >  x) && 
		       ( x1 <  x ||  x1 > _x) &&
		       (_y1 < _y || _y1 >  y) &&
		       ( y1 <  y ||  y1 > _y);
	}
	
	public GSRectangle union(GSRectangle other) {
		return union(other.x, other.y, other.width, other.height);
	}
	
	public GSRectangle union(int x, int y, int width, int height) {
		int _width = this.width;
		int _height = this.height;
		
		if ((_width | _height) < 0)
			return new GSRectangle(x, y, width, height);
		if ((width | height) < 0)
			return new GSRectangle(this);
	
		int _x = this.x;
		int _y = this.y;

		long _x1 = _x + _width;
		long _y1 = _y + _height;
		long x1 = x + width;
		long y1 = y + height;
	
		if (x < _x)
			_x = x;
		if (y < _y)
			_y = y;
		if (x1 > _x1)
			_x1 = x1;
		if (y1 > _y1)
			_y1 = y1;
		
		_x1 -= _x;
		_y1 -= _y;
		
		_width  = (int)((_x1 > Integer.MAX_VALUE) ? Integer.MAX_VALUE : _x1);
		_height = (int)((_y1 > Integer.MAX_VALUE) ? Integer.MAX_VALUE : _y1);
		
		return new GSRectangle(_x, _y, _width, _height);
	}
	
	public boolean isEmpty() {
		return (width <= 0 || height <= 0);
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		hash += 31 * hash + height;
		hash += 31 * hash + width;
		hash += 31 * hash + y;
		hash += 31 * hash + x;
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof GSRectangle) {
			GSRectangle other = (GSRectangle)obj;
			return x == other.x &&
			       y == other.y &&
			       width == other.width &&
			       height == other.height;
		}
		return false;
	}
}
