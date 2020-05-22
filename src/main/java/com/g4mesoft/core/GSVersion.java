package com.g4mesoft.core;

import net.minecraft.network.PacketByteBuf;

public final class GSVersion {

	public static final GSVersion INVALID = new GSVersion(-1, -1, -1);
	public static final GSVersion MINIMUM_VERSION = new GSVersion(0, 0, 0);

	private final int majorVersion;
	private final int minorVersion;
	private final int updateVersion;

	private String versionStringCache;
	
	public GSVersion(int majorVersion, int minorVersion, int updateVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.updateVersion = updateVersion;

		versionStringCache = null;
	}
	
	public int getMajorVersion() {
		return majorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}
	
	public int getUpdateVersion() {
		return updateVersion;
	}
	
	public String getVersionString() {
		if (versionStringCache == null)
			versionStringCache = String.format("%d.%d.%d", majorVersion, minorVersion, updateVersion);
		return versionStringCache;
	}
	
	public boolean isInvalid() {
		return majorVersion < 0 || minorVersion < 0 || updateVersion < 0;
	}

	public static GSVersion read(PacketByteBuf buf) {
		int major = buf.readShort();
		int minor = buf.readShort();
		int update = buf.readShort();
		return new GSVersion(major, minor, update);
	}

	public void write(PacketByteBuf buf) {
		buf.writeShort((short)majorVersion);
		buf.writeShort((short)minorVersion);
		buf.writeShort((short)updateVersion);
	}

	public boolean isGreaterThanOrEqualTo(GSVersion other) {
		if (isInvalid())
			return other.isInvalid();
		if (other.isInvalid())
			return true;

		if (majorVersion > other.majorVersion)
			return true;
		if (majorVersion == other.majorVersion) {
			if (minorVersion > other.minorVersion)
				return true;
			if (minorVersion == other.minorVersion)
				return updateVersion >= other.updateVersion;
		}
	
		return false;
	}

	public boolean isLessThan(GSVersion other) {
		return !isGreaterThanOrEqualTo(other);
	}
	
	public boolean isEqual(GSVersion other) {
		if (isInvalid())
			return other.isInvalid();
		
		return majorVersion  == other.majorVersion &&
		       minorVersion  == other.minorVersion &&
		       updateVersion == other.updateVersion;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GSVersion))
			return false;
		return isEqual((GSVersion)other);
	}
	
	@Override
	public String toString() {
		return getVersionString();
	}
}
