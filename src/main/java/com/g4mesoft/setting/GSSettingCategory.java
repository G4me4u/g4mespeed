package com.g4mesoft.setting;

import com.g4mesoft.util.GSBufferUtil;

import net.minecraft.util.PacketByteBuf;

public class GSSettingCategory {

	private final String name;
	
	public GSSettingCategory(String name) {
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

	public static GSSettingCategory read(PacketByteBuf buf) {
		return new GSSettingCategory(buf.readString(GSBufferUtil.MAX_STRING_LENGTH));
	}

	public void write(PacketByteBuf buf) {
		buf.writeString(name);
	}
}
