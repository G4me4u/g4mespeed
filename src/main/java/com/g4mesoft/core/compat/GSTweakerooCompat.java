package com.g4mesoft.core.compat;

import com.g4mesoft.G4mespeedMod;

import net.minecraft.entity.Entity;

public class GSTweakerooCompat extends GSAbstractCompat {

	private static final String CAMERA_ENTITY_CLASSPATH = "fi.dy.masa.tweakeroo.util.CameraEntity";
	
	private boolean tweakerooDetected;
	private Class<?> cameraEntityClazz;
	
	public GSTweakerooCompat() {
		tweakerooDetected = false;
		cameraEntityClazz = null;
	}
	
	@Override
	public void detect() {
		cameraEntityClazz = findClassByName(CAMERA_ENTITY_CLASSPATH);
		if (cameraEntityClazz == null) {
			// CameraEntity class has either moved, or tweakeroo is not installed.
			// Assume the latter and return silently.
			return;
		}

		tweakerooDetected = true;
	
		G4mespeedMod.GS_LOGGER.info("Tweakeroo mod detected!");
	}
	
	public boolean isTweakerooDetected() {
		return tweakerooDetected;
	}

	public boolean isCameraEntityInstance(Entity entity) {
		if (cameraEntityClazz != null)
			return cameraEntityClazz.isInstance(entity);
		return false;
	}
}
