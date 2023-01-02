package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;

/**
 * Internally used to store setting types which have not been registered. This
 * allows the setting values to be restored if the type is later registered again,
 * by writing the original data to disk. This way if an extension (which registers
 * custom setting types) is at some point uninstalled and then later reinstalled,
 * we ensure that the setting values are not lost.
 * 
 * @author Christian
 */
public final class GSUnknownSetting extends GSSetting<Object> {

	private final String type;
	private final byte[] data;
	
	public GSUnknownSetting(String name, String type, byte[] data) {
		super(name, null, false);
		
		this.type = type;
		this.data = data;
	}
	
	@Override
	public Object get() {
		return null;
	}

	@Override
	public GSUnknownSetting set(Object value) {
		return this;
	}

	@Override
	public boolean isDefault() {
		return false;
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

	@Override
	public GSSetting<Object> copySetting() {
		return new GSUnknownSetting(name, type, data);
	}
}
