package com.g4mesoft.core;

import net.minecraft.network.PacketByteBuf;

public final class GSVersion {

	public static final GSVersion INVALID = new GSVersion(-1, -1, -1);
	public static final GSVersion MINIMUM_VERSION = new GSVersion(0, 0, 0);

	private static final String RELEASE_FORMAT = "%d.%d.%d";
	private static final String BETA_FORMAT    = "%d.%d.%d-beta";
	private static final String ALPHA_FORMAT   = "%d.%d.%d-alpha";
	
	private final int majorVersion;
	private final int minorVersion;
	private final int patchVersion;

	private String versionStringCache;

	public GSVersion(String versionString) throws IllegalArgumentException {
		int dashIndex = versionString.indexOf('-');
		if (dashIndex != -1)
			versionString = versionString.substring(0, dashIndex);
		
		String[] args = versionString.split("\\.");
		if (args.length != 3)
			throw new IllegalArgumentException("Invalid version string");
		
		try {
			majorVersion = Integer.parseInt(args[0]);
			minorVersion = Integer.parseInt(args[1]);
			patchVersion = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid version string");
		}

		versionStringCache = null;
	}

	public GSVersion(int majorVersion, int minorVersion, int patchVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.patchVersion = patchVersion;

		versionStringCache = null;
	}
	
	public int getMajorVersion() {
		return majorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}
	
	public int getPatchVersion() {
		return patchVersion;
	}
	
	public String getVersionString() {
		if (versionStringCache == null) {
			String format = (majorVersion != 0) ? RELEASE_FORMAT : ((minorVersion != 0) ? BETA_FORMAT : ALPHA_FORMAT);
			versionStringCache = String.format(format, majorVersion, minorVersion, patchVersion);
		}
		
		return versionStringCache;
	}
	
	public boolean isInvalid() {
		return majorVersion < 0 || minorVersion < 0 || patchVersion < 0;
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
				return patchVersion >= other.patchVersion;
		}
	
		return false;
	}

	public boolean isLessThanOrEqualTo(GSVersion other) {
		return other.isGreaterThanOrEqualTo(this);
	}

	public boolean isGreaterThan(GSVersion other) {
		return !isLessThanOrEqualTo(other);
	}
	
	public boolean isLessThan(GSVersion other) {
		return !isGreaterThanOrEqualTo(other);
	}

	public boolean isEqual(GSVersion other) {
		if (isInvalid())
			return other.isInvalid();
		
		return majorVersion == other.majorVersion &&
		       minorVersion == other.minorVersion &&
		       patchVersion == other.patchVersion;
	}
	
	public static GSVersion read(PacketByteBuf buf) {
		int major = buf.readShort();
		int minor = buf.readShort();
		int update = buf.readShort();
		return new GSVersion(major, minor, update);
	}

	public static void write(PacketByteBuf buf, GSVersion version) {
		buf.writeShort((short)version.majorVersion);
		buf.writeShort((short)version.minorVersion);
		buf.writeShort((short)version.patchVersion);
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
