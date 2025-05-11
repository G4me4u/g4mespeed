package com.g4mesoft.setting;

import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

public final class GSSettingCategory {

	private final String name;
	
	public GSSettingCategory(String name) {
		if (name == null)
			throw new IllegalArgumentException("name is null");
		
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GSSettingCategory))
			return false;
		return ((GSSettingCategory)other).name.equals(name);
	}

	public static GSSettingCategory read(GSDecodeBuffer buf) {
		return new GSSettingCategory(buf.readString());
	}

	public void write(GSEncodeBuffer buf) {
		buf.writeString(name);
	}
}
