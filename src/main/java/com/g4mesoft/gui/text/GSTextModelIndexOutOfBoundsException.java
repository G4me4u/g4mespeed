package com.g4mesoft.gui.text;

public class GSTextModelIndexOutOfBoundsException extends IndexOutOfBoundsException {
	private static final long serialVersionUID = -8048127724794232190L;

	public GSTextModelIndexOutOfBoundsException() {
		super();
	}

	public GSTextModelIndexOutOfBoundsException(String msg) {
		super(msg);
	}
	
	public GSTextModelIndexOutOfBoundsException(int index) {
		super("TextModel index out of bounds: " + index);
	}
}
