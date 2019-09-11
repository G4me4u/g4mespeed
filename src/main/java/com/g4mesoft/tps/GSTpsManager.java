package com.g4mesoft.tps;

import com.g4mesoft.core.GSController;

import net.minecraft.util.math.MathHelper;

public class GSTpsManager {

	public static final float DEFAULT_TPS = 20.0f;
	public static final float MIN_TPS = 0.01f;
	public static final float MAX_TPS = Float.MAX_VALUE;
	
	public static final float FAST_INTERVAL = 1.0f;
	public static final float SLOW_INTERVAL = 0.5f;
	
	public static final int TPS_INTRODUCTION_VERSION = 100;
	
	protected final GSController controller;
	
	protected float tps;
	
	public GSTpsManager(GSController controller) {
		this.controller = controller;
		
		tps = DEFAULT_TPS;
	}
	
	protected boolean setTps(float tps) {
		float newTps = MathHelper.clamp(tps, MIN_TPS, MAX_TPS);
		if (!MathHelper.equalsApproximate(this.tps, newTps)) {
			float oldTps = this.tps;
			this.tps = newTps;
			
			controller.tpsChanged(newTps, oldTps);
			return true;
		}

		return false;
	}

	public float getTps() {
		return tps;
	}
}
