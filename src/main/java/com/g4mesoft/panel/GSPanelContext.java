package com.g4mesoft.panel;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.g4mesoft.panel.event.GSEventDispatcher;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSBasicRenderer2D;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;

public final class GSPanelContext {

	private static GSPanelContext instance;
	
	private final MinecraftClient client;
	
	private final GSIRenderer2D renderer;
	private final GSScreen screen;
	private final GSEventDispatcher eventDispatcher;

	private final Map<Integer, Long> standardCursors;
	
	private GSPanelContext(MinecraftClient client) {
		this.client = client;
		
		renderer = new GSBasicRenderer2D(client);
		
		screen = new GSScreen();
		eventDispatcher = new GSEventDispatcher(screen.getRootPanel());

		standardCursors = new HashMap<>();
	}

	public static void init(MinecraftClient client) {
		if (instance == null)
			instance = new GSPanelContext(client);
	}
	
	public static boolean isInitialized() {
		return (instance != null);
	}
	
	public static void dispose() {
		if (instance != null)
			instance.disposeImpl();
	}
	
	public static void setContent(GSPanel content) {
		getContext().setContentImpl(content);
	}

	public static void setCursor(GSECursorType cursor) {
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
	
	public static void requestFocus(GSPanel panel) {
		getContext().requestFocusImpl(panel);
	}

	public static void unfocus(GSPanel panel) {
		getContext().unfocusImpl(panel);
	}
	
	public static void dispatchMouseEvent(GSMouseEvent event, GSPanel source, GSPanel dest) {
		getContext().dispatchMouseEventImpl(event, source, dest);
	}

	public static void dispatchKeyEvent(GSKeyEvent event, GSPanel source, GSPanel dest) {
		getContext().dispatchKeyEventImpl(event, source, dest);
	}
	
	static GSEventDispatcher getEventDispatcher() {
		return getContext().getEventDispatcherImpl();
	}
	
	public static GSRootPanel getRootPanel() {
		return getContext().getRootPanelImpl();
	}
	
	private void disposeImpl() {
		// If the screen is currently visible, hide it.
		if (client.currentScreen == screen)
			setContent(null);
		// Destroy the standard cursors
		for (Long cursorPtr : standardCursors.values())
			GLFW.glfwDestroyCursor(cursorPtr.longValue());
		standardCursors.clear();
	}
	
	private void setContentImpl(GSPanel content) {
		eventDispatcher.reset();

		GSRootPanel rootPanel = screen.getRootPanel();

		rootPanel.setContent(content);
		if (content == null)
			rootPanel.removeAll();
		
		if (content != null) {
			if (client.currentScreen != screen)
				client.openScreen(screen);
		} else {
			if (client.currentScreen != null)
				client.openScreen(null);
		}
	}

	private void setCursorImpl(GSECursorType cursor) {
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
	
	private void requestFocusImpl(GSPanel panel) {
		eventDispatcher.requestFocus(panel);
	}

	private void unfocusImpl(GSPanel panel) {
		eventDispatcher.unfocus(panel);
	}
	
	private void dispatchMouseEventImpl(GSMouseEvent event, GSPanel source, GSPanel dest) {
		eventDispatcher.dispatchMouseEvent(event, source, dest);
	}

	private void dispatchKeyEventImpl(GSKeyEvent event, GSPanel source, GSPanel dest) {
		eventDispatcher.dispatchKeyEvent(event, source, dest);
	}
	
	private GSEventDispatcher getEventDispatcherImpl() {
		return eventDispatcher;
	}
	
	private GSRootPanel getRootPanelImpl() {
		return screen.getRootPanel();
	}
	
	private static GSPanelContext getContext() {
		if (instance == null)
			throw new IllegalStateException("Context is not initialized!");
		return instance;
	}
}
