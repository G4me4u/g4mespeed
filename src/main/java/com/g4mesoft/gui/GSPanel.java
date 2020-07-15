package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.g4mesoft.gui.event.GSEvent;
import com.g4mesoft.gui.event.GSIButtonStroke;
import com.g4mesoft.gui.event.GSIFocusEventListener;
import com.g4mesoft.gui.event.GSIKeyListener;
import com.g4mesoft.gui.event.GSIMouseListener;
import com.g4mesoft.gui.event.GSKeyEvent;
import com.g4mesoft.gui.event.GSMouseEvent;
import com.g4mesoft.gui.renderer.GSIRenderer2D;

public class GSPanel implements GSIElement {
	
	private GSIElement parent;
	
	public int x;
	public int y;
	public int width;
	public int height;
	
	private List<GSIMouseListener> mouseEventListeners;
	private List<GSIKeyListener> keyEventListeners;
	private List<GSIFocusEventListener> focusEventListeners;
	
	private boolean passingEvents;
	private boolean focused;
	
	private Map<GSIButtonStroke, Runnable> buttonStrokes;
	private GSIMouseListener buttonMouseListener;
	private GSIKeyListener buttonKeyListener;

	protected GSCursorType cursor;
	
	protected GSPanel() {
		parent = null;
		
		mouseEventListeners = null;
		keyEventListeners = null;
		focusEventListeners = null;
		
		focused = false;
		
		cursor = GSCursorType.DEFAULT;
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("width and height must be non-negative!");
		
		if (this.x != x || this.y != y || this.width != width || this.height != height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		
			onBoundsChanged();
		}
	}

	protected void onBoundsChanged() {
	}

	@Override
	public void onAdded(GSIElement parent) {
		if (this.parent != null)
			throw new IllegalStateException("Panel already has a parent!");
		if (parent == null)
			throw new IllegalArgumentException("parent is null!");
		if (parent == this)
			throw new IllegalArgumentException("Can not set parent to self!");
		
		this.parent = parent;
	}

	@Override
	public void onRemoved(GSIElement parent) {
		if (parent == null || parent != this.parent)
			throw new IllegalArgumentException("Panel does not have the specified parent!");
		
		this.parent = null;
		
		unfocus();
	}
	
	@Override
	public void update() {
	}
	
	@Override
	public void preRender(GSIRenderer2D renderer) {
		renderer.pushTransform();
		renderer.translate(x, y);
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
	}
	
	@Override
	public void postRender(GSIRenderer2D renderer) {
		renderer.popTransform();
	}
	
	@Override
	public boolean isAdded() {
		return (parent != null);
	}
	
	@Override
	public GSIElement getParent() {
		return parent;
	}
	
	@Override
	public GSIElement getChildAt(int x, int y) {
		return null;
	}
	
	@Override
	public boolean isInBounds(int x, int y) {
		if (x < this.x || x >= this.x + width)
			return false;
		if (y < this.y || y >= this.y + height)
			return false;
		return true;
	}
	
	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public void addMouseEventListener(GSIMouseListener eventListener) {
		if (mouseEventListeners == null)
			mouseEventListeners = new ArrayList<GSIMouseListener>(1);
		
		mouseEventListeners.add(eventListener);
	}

	@Override
	public void removeMouseEventListener(GSIMouseListener eventListener) {
		if (mouseEventListeners != null) {
			mouseEventListeners.remove(eventListener);
			
			if (mouseEventListeners.isEmpty())
				mouseEventListeners = null;
		}
	}

	@Override
	public List<GSIMouseListener> getMouseEventListeners() {
		if (mouseEventListeners == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(mouseEventListeners);
	}
	
	@Override
	public void addKeyEventListener(GSIKeyListener eventListener) {
		if (keyEventListeners == null)
			keyEventListeners = new ArrayList<GSIKeyListener>(1);
		
		keyEventListeners.add(eventListener);
	}
	
	@Override
	public void removeKeyEventListener(GSIKeyListener eventListener) {
		if (keyEventListeners != null) {
			keyEventListeners.remove(eventListener);
			
			if (keyEventListeners.isEmpty())
				keyEventListeners = null;
		}
	}

	@Override
	public List<GSIKeyListener> getKeyEventListeners() {
		if (keyEventListeners == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(keyEventListeners);
	}

	@Override
	public void addFocusEventListener(GSIFocusEventListener eventListener) {
		if (focusEventListeners == null)
			focusEventListeners = new ArrayList<GSIFocusEventListener>(1);
		
		focusEventListeners.add(eventListener);
	}
	
	@Override
	public void removeFocusEventListener(GSIFocusEventListener eventListener) {
		if (focusEventListeners != null) {
			focusEventListeners.remove(eventListener);
			
			if (focusEventListeners.isEmpty())
				focusEventListeners = null;
		}
	}

	@Override
	public List<GSIFocusEventListener> getFocusEventListeners() {
		if (focusEventListeners == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(focusEventListeners);
	}

	@Override
	public boolean isPassingEvents() {
		return passingEvents;
	}
	
	@Override
	public void setPassingEvents(boolean passingEvents) {
		this.passingEvents = passingEvents;
	}
	
	@Override
	public void dispatchMouseEvent(GSMouseEvent event, GSIElement source) {
		GSElementContext.dispatchMouseEvent(event, source, this);
	}

	@Override
	public void dispatchKeyEvent(GSKeyEvent event, GSIElement source) {
		GSElementContext.dispatchKeyEvent(event, source, this);
	}
	
	@Override
	public boolean isFocused() {
		return focused;
	}
	
	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}
	
	@Override
	public void requestFocus() {
		GSElementContext.requestFocus(this);
	}

	@Override
	public void unfocus() {
		if (isFocused())
			GSElementContext.unfocus(this);
	}
	
	@Override
	public boolean isEditingText() {
		return false;
	}
	
	@Override
	public GSCursorType getCursor() {
		return cursor;
	}

	@Override
	public void setCursor(GSCursorType cursor) {
		if (cursor == null)
			throw new IllegalArgumentException("cursor is null!");
		
		this.cursor = cursor;
	}
	
	@Override
	public int getEventOffsetX() {
		return x;
	}

	@Override
	public int getEventOffsetY() {
		return y;
	}
	
	protected void putButtonStroke(GSIButtonStroke button, Runnable listener) {
		if (buttonStrokes == null)
			buttonStrokes = new LinkedHashMap<GSIButtonStroke, Runnable>();

		if (buttonMouseListener == null) {
			buttonMouseListener = new GSIMouseListener() {
				@Override
				public void mousePressed(GSMouseEvent event) {
					handleButtonEvent(event);
				}
			};
			
			addMouseEventListener(buttonMouseListener);
		}
		
		if (buttonKeyListener == null) {
			buttonKeyListener = new GSIKeyListener() {
				@Override
				public void keyPressed(GSKeyEvent event) {
					if (!event.isRepeating())
						handleButtonEvent(event);
				}
			};
			
			addKeyEventListener(buttonKeyListener);
		}
		
		buttonStrokes.put(button, listener);
	}

	protected void removeButtonStroke(GSIButtonStroke button) {
		if (buttonStrokes != null)
			buttonStrokes.remove(button);
	}
	
	private void handleButtonEvent(GSEvent event) {
		for (Map.Entry<GSIButtonStroke, Runnable> entry : buttonStrokes.entrySet()) {
			GSIButtonStroke button = entry.getKey();
			Runnable listener = entry.getValue();
			
			if (button.isMatching(event)) {
				listener.run();
				event.consume();
			}
		}
	}
	
	public boolean hasI18nTranslation(String key) {
		return GSElementContext.hasI18nTranslation(key);
	}
	
	public String i18nTranslate(String key) {
		return GSElementContext.i18nTranslate(key);
	}

	public String i18nTranslateFormatted(String key, Object... args) {
		return GSElementContext.i18nTranslateFormatted(key, args);
	}

	@Override
	public final int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public final boolean equals(Object other) {
		return super.equals(other);
	}
}
