package com.g4mesoft.registry;

@SuppressWarnings("serial")
public class GSDuplicateRegisterException extends RuntimeException {

	public GSDuplicateRegisterException() {
	}

	public GSDuplicateRegisterException(String msg) {
		super(msg);
	}
}
