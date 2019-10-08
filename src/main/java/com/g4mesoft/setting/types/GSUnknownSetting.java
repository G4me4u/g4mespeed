package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;

public class GSUnknownSetting extends GSSetting<Object> {

	private final String type;
	private final byte[] data;
	
	public GSUnknownSetting(String name, String type, byte[] data) {
		super(name, null);
		
		this.type = type;
		this.data = data;
	}
	
	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public void setValue(Object value) {
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return false;
	}

	public String getType() {
		return type;
	}
	
	public byte[] getData() {
		return data;
	}
}
