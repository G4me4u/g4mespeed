package com.g4mesoft.gui;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.gui.event.GSEventDispatcher;
import com.g4mesoft.gui.renderer.GSBasicRenderer2D;
import com.g4mesoft.gui.renderer.GSIRenderer2D;
import com.g4mesoft.module.translation.GSTranslationModule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;

public final class GSElementContext {

	private static GSElementContext instance;
	
	private final MinecraftClient client;
	
	private final GSIRenderer2D renderer;
	private final GSRootPanel rootPanel;
	private final GSEventDispatcher eventDispatcher;

	private final Map<Integer, Long> standardCursors;
	
	private GSElementContext(MinecraftClient client) {
		this.client = client;
		
		renderer = new GSBasicRenderer2D(client);
		rootPanel = new GSRootPanel();
		eventDispatcher = new GSEventDispatcher(rootPanel);

		standardCursors = new HashMap<Integer, Long>();
	}

	public static void init(MinecraftClient client) {
		if (instance == null)
			instance = new GSElementContext(client);
	}
	
	public static boolean isInitialized() {
		return (instance != null);
	}
	
	public static void dispose() {
		if (instance != null)
			instance.disposeImpl();
	}
	
	public static void setContent(GSIElement content) {
		getContext().setContentImpl(content);
	}

	public static void setCursor(GSCursorType cursor) {
		getContext().setCursorImpl(cursor);
	}
	
	public static String getClipboardString() {
		return getContext().getClipboardStringImpl();
	}

	public static void setClipboardString(String clipboard) {
		getContext().setClipboardStringImpl(clipboard);
	}
	
	public static boolean hasI18nTranslation(String key) {
		return getContext().hasI18nTranslationImpl(key);
	}
	
	public static String i18nTranslate(String key) {
		return getContext().i18nTranslateImpl(key);
	}

	public static String i18nTranslateFormatted(String key, Object... args) {
		return getContext().i18nTranslateFormattedImpl(key, args);
	}
	
	public static GSIRenderer2D getRenderer() {
		return getContext().getRendererImpl();
	}
	
	public static void playSound(SoundInstance sound) {
		getContext().playSoundImpl(sound);
	}
	
	public static void requestFocus(GSIElement panel) {
		getContext().requestFocusImpl(panel);
	}
	
	public static GSEventDispatcher getEventDispatcher() {
		return getContext().getEventDispatcherImpl();
	}
	
	private void disposeImpl() {
		// Destroy the standard cursors
		for (Long cursorPtr : standardCursors.values())
			GLFW.glfwDestroyCursor(cursorPtr.longValue());
		standardCursors.clear();
	}
	
	private void setContentImpl(GSIElement content) {
		eventDispatcher.reset();

		rootPanel.setContent(content);
		
		if (content != null) {
			if (client.currentScreen != rootPanel)
				client.openScreen(rootPanel);
		} else {
			if (client.currentScreen != null)
				client.openScreen(null);
		}
	}

	private void setCursorImpl(GSCursorType cursor) {
		if (cursor == null)
			throw new IllegalArgumentException("cursor is null!");
		
		switch (cursor) {
		case DEFAULT:
			setGLFWCursor(GLFW.GLFW_ARROW_CURSOR);
			break;
		case IBEAM:
			setGLFWCursor(GLFW.GLFW_IBEAM_CURSOR);
			break;
		case CROSSHAIR:
			setGLFWCursor(GLFW.GLFW_CROSSHAIR_CURSOR);
			break;
		case HAND:
			setGLFWCursor(GLFW.GLFW_HAND_CURSOR);
			break;
		case HRESIZE:
			setGLFWCursor(GLFW.GLFW_HRESIZE_CURSOR);
			break;
		case VRESIZE:
			setGLFWCursor(GLFW.GLFW_VRESIZE_CURSOR);
			break;
		default:
			throw new IllegalStateException("Unsupported cursor");
		}
	}
	
	private void setGLFWCursor(int cursorType) {
		long cursorPtr;
		
		if (cursorType == GLFW.GLFW_ARROW_CURSOR) {
			cursorPtr = MemoryUtil.NULL;
		} else if (standardCursors.containsKey(cursorType)) {
			cursorPtr = standardCursors.get(cursorType);
		} else {
			cursorPtr = GLFW.glfwCreateStandardCursor(cursorType);
			standardCursors.put(cursorType, cursorPtr);
		}

		GLFW.glfwSetCursor(client.getWindow().getHandle(), cursorPtr);
	}
	
	private String getClipboardStringImpl() {
		return client.keyboard.getClipboard();
	}

	private void setClipboardStringImpl(String clipboard) {
		client.keyboard.setClipboard(clipboard);
	}

	private boolean hasI18nTranslationImpl(String key) {
		return getTranslationModule().hasTranslation(key);
	}
	
	private String i18nTranslateImpl(String key) {
		return getTranslationModule().getTranslation(key);
	}

	private String i18nTranslateFormattedImpl(String key, Object... args) {
		return getTranslationModule().getFormattedTranslation(key, args);
	}
	
	private GSTranslationModule getTranslationModule() {
		return GSControllerClient.getInstance().getTranslationModule();
	}
	
	private GSIRenderer2D getRendererImpl() {
		return renderer;
	}

	private void playSoundImpl(SoundInstance sound) {
		client.getSoundManager().play(sound);
	}
	
	private void requestFocusImpl(GSIElement panel) {
		eventDispatcher.requestFocus(panel);
	}
	
	private GSEventDispatcher getEventDispatcherImpl() {
		return eventDispatcher;
	}
	
	private static GSElementContext getContext() {
		if (instance == null)
			throw new IllegalStateException("Context is not initialized!");
		return instance;
	}
}
