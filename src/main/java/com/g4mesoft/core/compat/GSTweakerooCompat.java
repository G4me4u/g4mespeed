package com.g4mesoft.core.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSClientController;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;

public class GSTweakerooCompat extends GSAbstractCompat {

	private static final String CAMERA_ENTITY_CLASSPATH = "fi.dy.masa.tweakeroo.util.CameraEntity";
	private static final String MOVEMENT_TICK_METHOD = "movementTick";
	private static final String CAMERA_FIELD = "camera";
	
	private boolean tweakerooDetected;
	private Class<?> cameraEntityClazz;
	private Field cameraField;
	private Method movementTickMethod;
	
	private Object tmpCamera;
	
	public GSTweakerooCompat() {
		tweakerooDetected = false;
		cameraEntityClazz = null;
		cameraField = null;
		movementTickMethod = null;
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
		
		cameraField = findDeclaredField(cameraEntityClazz, CAMERA_FIELD);
		movementTickMethod = findDeclaredMethod(cameraEntityClazz, MOVEMENT_TICK_METHOD);
		
		if (cameraField == null || movementTickMethod == null) {
			G4mespeedMod.GS_LOGGER.warn("Unable to retreive CameraEntity fields and methods.");
			resetFields();
		} else {
			try {
				cameraField.setAccessible(true);
				movementTickMethod.setAccessible(true);
			} catch (Exception e) {
				G4mespeedMod.GS_LOGGER.warn("Unable to make camera field and movementTick method accessible.");
				resetFields();
			}
		}
	}
	
	private void resetFields() {
		cameraField = null;
		movementTickMethod = null;
	}
	
	public boolean isTweakerooDetected() {
		return tweakerooDetected;
	}

	public boolean isCameraEntityRetreived() {
		return tweakerooDetected && cameraField != null && movementTickMethod != null;
	}

	public boolean isCameraEntityInstance(Entity entity) {
		if (cameraEntityClazz != null)
			return cameraEntityClazz.isInstance(entity);
		return false;
	}
	
	public void disableCameraEntity() {
		if (cameraField != null) {
			tmpCamera = getStaticField(cameraField);
			setStaticField(cameraField, null);
		}
	}

	public void enableCameraEntityTicking() {
		if (cameraField != null) {
			setStaticField(cameraField, tmpCamera);
			tmpCamera = null;
		}
	}
	
	public void tickCameraEntityMovement() {
		if (movementTickMethod != null)
			invokeStatic(movementTickMethod);
	}

	public boolean isCameraEntityEnabled() {
		return cameraField != null && getStaticField(cameraField) != null;
	}
}
