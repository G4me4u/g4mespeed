package com.g4mesoft;

import java.io.IOException;

import com.g4mesoft.core.GSVersion;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

public class GSExtensionInfo {

	private final String name;
	private final GSExtensionUID uid;
	private final GSVersion version;
	
	public GSExtensionInfo(String name, GSExtensionUID uid, GSVersion version) {
		this.name = name;
		this.uid = uid;
		this.version = version;
	}

	public String getName() {
		return name;
	}
	
	public GSExtensionUID getUniqueId() {
		return uid;
	}

	public GSVersion getVersion() {
		return version;
	}

	public static GSExtensionInfo read(GSDecodeBuffer buf) throws IOException {
		String name = buf.readString();
		GSExtensionUID uid = GSExtensionUID.read(buf);
		GSVersion version = GSVersion.read(buf);
		return new GSExtensionInfo(name, uid, version);
	}

	public static void write(GSEncodeBuffer buf, GSExtensionInfo info) throws IOException {
		buf.writeString(info.name);
		GSExtensionUID.write(buf, info.uid);
		GSVersion.write(buf, info.version);
	}
}
