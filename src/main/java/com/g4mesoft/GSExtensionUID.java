package com.g4mesoft;

import java.io.IOException;

import net.minecraft.network.PacketByteBuf;

public final class GSExtensionUID {

	private final int value;
	
	public GSExtensionUID(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public static void write(PacketByteBuf buf, GSExtensionUID uid) throws IOException {
		buf.writeInt(uid.getValue());
	}

	public static GSExtensionUID read(PacketByteBuf buf) throws IOException {
		return new GSExtensionUID(buf.readInt());
	}

	public static GSExtensionUID parseUID(String str) {
		int value;
		try {
			value = Integer.parseInt(str, 16);
		} catch (NumberFormatException e) {
			return null;
		}
		
		return new GSExtensionUID(value);
	}
	
	public static String toString(GSExtensionUID uid) {
		return Integer.toHexString(uid.getValue());
	}
	
	@Override
	public int hashCode() {
		return Integer.hashCode(value);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GSExtensionUID))
			return false;
		return (value == ((GSExtensionUID)other).value);
	}
	
	@Override
	public String toString() {
		return toString(this);
	}
}
