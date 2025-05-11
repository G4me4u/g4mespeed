package com.g4mesoft.core.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class GSCompatUtil {

	private GSCompatUtil() {
	}
	
	/* Searching for classes, methods, and fields. */
	
	static Class<?> findClassByName(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
		}
		// The class was not found.
		return null;
	}
	
	static Field findDeclaredField(Class<?> clazz, String name) {
		try {
			return clazz.getDeclaredField(name);
		} catch (NoSuchFieldException | SecurityException e) {
		}
		// The field was not found.
		return null;
	}

	static Field findField(Class<?> clazz, String name) {
		try {
			return clazz.getField(name);
		} catch (NoSuchFieldException | SecurityException e) {
		}
		// The field was not found.
		return null;
	}
	
	static Method findDeclaredMethod(Class<?> clazz, String name, Class<?>... args) {
		try {
			return clazz.getDeclaredMethod(name, args);
		} catch (NoSuchMethodException | SecurityException e) {
		}
		// The field was not found.
		return null;
	}

	static Method findMethod(Class<?> clazz, String name, Class<?>... args) {
		try {
			return clazz.getMethod(name, args);
		} catch (NoSuchMethodException | SecurityException e) {
		}
		// The field was not found.
		return null;
	}
	
	/* Invocation of methods */
	
	static boolean invokeStatic(Method method, Object... args) {
		return invoke(null, method, args);
	}
	
	static boolean invoke(Object instance, Method method, Object... args) {
		try {
			method.invoke(instance, args);
			return true;
		} catch (Exception e) {
		}
		return false;
	}
	
	/** Note: unchecked cast */
	static <T> T getStatic(Method method, Object... args) {
		return get(null, method, args);
	}
	
	@SuppressWarnings("unchecked")
	/** Note: unchecked cast */
	static <T> T get(Object instance, Method method, Object... args) {
		try {
			return (T)method.invoke(instance, args);
		} catch (Exception e) {
			// We have no way to determine if an error occurred during
			// invocation of a getter. For now just handle silently.
		}
		return null;
	}

	/** Note: unchecked cast */
	static <T> T getStaticField(Field field) {
		return getField(null, field);
	}
	
	@SuppressWarnings("unchecked")
	/** Note: unchecked cast */
	static <T> T getField(Object instance, Field field) {
		try {
			return (T)field.get(instance);
		} catch (Exception e) {
			// We have no way to determine if an error occurred during
			// invocation of a getter. For now just handle silently.
		}
		return null;
	}

	static boolean setStaticField(Field field, Object newValue) {
		return setField(null, field, newValue);
	}
	
	static boolean setField(Object instance, Field field, Object newValue) {
		try {
			field.set(instance, newValue);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
