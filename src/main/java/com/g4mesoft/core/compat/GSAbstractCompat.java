package com.g4mesoft.core.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

abstract class GSAbstractCompat {

	public abstract void detect();
	
	/* Searching for classes, methods, and fields. */
	
	protected Class<?> findClassByName(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
		}
		// The class was not found.
		return null;
	}
	
	protected Field findDeclaredField(Class<?> clazz, String name) {
		try {
			return clazz.getDeclaredField(name);
		} catch (NoSuchFieldException | SecurityException e) {
		}
		// The field was not found.
		return null;
	}

	protected Field findField(Class<?> clazz, String name) {
		try {
			return clazz.getField(name);
		} catch (NoSuchFieldException | SecurityException e) {
		}
		// The field was not found.
		return null;
	}
	
	protected Method findDeclaredMethod(Class<?> clazz, String name, Class<?>... args) {
		try {
			return clazz.getDeclaredMethod(name, args);
		} catch (NoSuchMethodException | SecurityException e) {
		}
		// The field was not found.
		return null;
	}

	protected Method findMethod(Class<?> clazz, String name, Class<?>... args) {
		try {
			return clazz.getMethod(name, args);
		} catch (NoSuchMethodException | SecurityException e) {
		}
		// The field was not found.
		return null;
	}
	
	/* Invocation of methods */
	
	protected boolean invokeStatic(Method method, Object... args) {
		return invoke(null, method, args);
	}
	
	protected boolean invoke(Object instance, Method method, Object... args) {
		try {
			method.invoke(instance, args);
			return true;
		} catch (Exception e) {
		}
		return false;
	}
	
	/** Note: unchecked cast */
	protected <T> T getStatic(Method method, Object... args) {
		return get(null, method, args);
	}
	
	@SuppressWarnings("unchecked")
	/** Note: unchecked cast */
	protected <T> T get(Object instance, Method method, Object... args) {
		try {
			return (T)method.invoke(instance, args);
		} catch (Exception e) {
			// We have no way to determine if an error occurred during
			// invocation of a getter. For now just handle silently.
		}
		return null;
	}

	/** Note: unchecked cast */
	protected <T> T getStaticField(Field field) {
		return getField(null, field);
	}
	
	@SuppressWarnings("unchecked")
	/** Note: unchecked cast */
	protected <T> T getField(Object instance, Field field) {
		try {
			return (T)field.get(instance);
		} catch (Exception e) {
			// We have no way to determine if an error occurred during
			// invocation of a getter. For now just handle silently.
		}
		return null;
	}

	protected boolean setStaticField(Field field, Object newValue) {
		return setField(null, field, newValue);
	}
	
	protected boolean setField(Object instance, Field field, Object newValue) {
		try {
			field.set(instance, newValue);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
