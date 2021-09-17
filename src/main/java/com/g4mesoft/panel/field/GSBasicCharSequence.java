package com.g4mesoft.panel.field;

public class GSBasicCharSequence implements CharSequence {

	private final char[] buffer;
	private final int offset;
	private final int length;

	public GSBasicCharSequence(char[] buffer) {
		this(buffer, 0, buffer.length);
	}
	
	public GSBasicCharSequence(char[] buffer, int offset, int length) {
		if (offset < 0)
			throw new ArrayIndexOutOfBoundsException(offset);
		if (length < 0)
			throw new ArrayIndexOutOfBoundsException(length);
		if (offset + length > buffer.length)
			throw new ArrayIndexOutOfBoundsException(buffer.length);
		
		this.buffer = buffer;
		this.offset = offset;
		this.length = length;
	}
	
	@Override
	public int length() {
		return length;
	}

	@Override
	public char charAt(int index) {
		if (index < 0 || index >= length)
			throw new StringIndexOutOfBoundsException(index);
		return buffer[offset + index];
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		if (start < 0)
			throw new StringIndexOutOfBoundsException(start);
		if (end > length)
			throw new StringIndexOutOfBoundsException(length);
		// start > end, checked in the constructor below
		return new GSBasicCharSequence(buffer, offset + start, end - start);
	}
}
