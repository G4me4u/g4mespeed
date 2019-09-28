package com.g4mesoft.module.probe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.math.BlockPos;

public class GSProbeInfo {

	private final String name;
	private final BlockPos pos;
	private final Set<GSEProbeEventType> types;

	public GSProbeInfo(String name, BlockPos pos, Set<GSEProbeEventType> types) {
		this.name = name;
		this.pos = pos;
		this.types = new HashSet<GSEProbeEventType>(types);
	}
	
	public String getName() {
		return name;
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public boolean isTrackingType(GSEProbeEventType type) {
		return types.contains(type);
	}
	
	public Set<GSEProbeEventType> getTrackedTypes() {
		return Collections.unmodifiableSet(types);
	}
}
