package com.g4mesoft.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.g4mesoft.panel.event.GSEvent;
import com.g4mesoft.panel.event.GSIButtonStroke;
import com.g4mesoft.panel.event.GSIFocusEventListener;
import com.g4mesoft.panel.event.GSIKeyListener;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.panel.popup.GSDropdown;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSPanel implements GSIViewport {
	
	private GSPanel parent;
	
	private boolean visible;
	
	public int x;
	public int y;
	public int width;
	public int height;
	
	private List<GSIMouseListener> mouseEventListeners;
	private List<GSIKeyListener> keyEventListeners;
	private List<GSIFocusEventListener> focusEventListeners;
	
	private boolean passingEvents;
	private boolean focused;
	private boolean focusable;
	
	private Map<GSIButtonStroke, Runnable> buttonStrokes;
	private GSIMouseListener buttonMouseListener;
	private GSIKeyListener buttonKeyListener;

	protected GSECursorType cursor;
	
	protected GSDimension minimumSize;
	protected GSDimension preferredSize;
	
	protected GSPanel() {
		parent = null;
		
		mouseEventListeners = null;
		keyEventListeners = null;
		focusEventListeners = null;
		
		focused = false;
		// All panels are focusable by default
		focusable = true;
		
		cursor = GSECursorType.DEFAULT;
	}

	public void add(GSPanel panel) {
		throw new UnsupportedOperationException("Not a parent panel");
	}
	
	public void remove(GSPanel panel) {
		throw new UnsupportedOperationException("Not a parent panel");
	}

	public void removeAll() {
		throw new UnsupportedOperationException("Not a parent panel");
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public GSLocation getLocation() {
		return new GSLocation(x, y);
	}

	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	public GSDimension getSize() {
		return new GSDimension(width, height);
	}
	
	public GSRectangle getBounds() {
		return new GSRectangle(x, y, width, height);
	}
	
	public void setBounds(GSRectangle bounds) {
		setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
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

	public void layout() {
	}
	
	public boolean isAdded() {
		return (parent != null);
	}
	
	public GSPanel getParent() {
		return parent;
	}
	
	public void onAdded(GSPanel parent) {
		if (this.parent != null)
			throw new IllegalStateException("Panel already has a parent!");
		if (parent == null)
			throw new IllegalArgumentException("parent is null!");
		if (parent == this)
			throw new IllegalArgumentException("Can not set parent to self!");
		
		this.parent = parent;
	}

	public void onRemoved(GSPanel parent) {
		if (parent == null || parent != this.parent)
			throw new IllegalArgumentException("Panel does not have the specified parent!");
		
		this.parent = null;
		
		unfocus();
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		if (visible != this.visible) {
			this.visible = visible;
		
			if (visible) {
				onShown();
			} else {
				onHidden();
			}
		}
	}
	
	protected void onShown() {
	}

	protected void onHidden() {
	}
	
	public void update() {
	}
	
	public void preRender(GSIRenderer2D renderer) {
		renderer.pushMatrix();
		renderer.translate(x, y);
	}
	
	public void render(GSIRenderer2D renderer) {
	}
	
	public void postRender(GSIRenderer2D renderer) {
		renderer.popMatrix();
	}
	
	public GSPanel getChildAt(int x, int y) {
		return null;
	}
	
	public boolean isInBounds(int x, int y) {
		if (x < this.x || x >= this.x + width)
			return false;
		if (y < this.y || y >= this.y + height)
			return false;
		return true;
	}
	
	public void addMouseEventListener(GSIMouseListener eventListener) {
		if (mouseEventListeners == null)
			mouseEventListeners = new ArrayList<GSIMouseListener>(1);
		
		mouseEventListeners.add(eventListener);
	}

	public void removeMouseEventListener(GSIMouseListener eventListener) {
		if (mouseEventListeners != null) {
			mouseEventListeners.remove(eventListener);
			
			if (mouseEventListeners.isEmpty())
				mouseEventListeners = null;
		}
	}

	public List<GSIMouseListener> getMouseEventListeners() {
		if (mouseEventListeners == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(mouseEventListeners);
	}
	
	public void addKeyEventListener(GSIKeyListener eventListener) {
		if (keyEventListeners == null)
			keyEventListeners = new ArrayList<GSIKeyListener>(1);
		
		keyEventListeners.add(eventListener);
	}
	
	public void removeKeyEventListener(GSIKeyListener eventListener) {
		if (keyEventListeners != null) {
			keyEventListeners.remove(eventListener);
			
			if (keyEventListeners.isEmpty())
				keyEventListeners = null;
		}
	}

	public List<GSIKeyListener> getKeyEventListeners() {
		if (keyEventListeners == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(keyEventListeners);
	}

	public void addFocusEventListener(GSIFocusEventListener eventListener) {
		if (focusEventListeners == null)
			focusEventListeners = new ArrayList<GSIFocusEventListener>(1);
		
		focusEventListeners.add(eventListener);
	}
	
	public void removeFocusEventListener(GSIFocusEventListener eventListener) {
		if (focusEventListeners != null) {
			focusEventListeners.remove(eventListener);
			
			if (focusEventListeners.isEmpty())
				focusEventListeners = null;
		}
	}

	public List<GSIFocusEventListener> getFocusEventListeners() {
		if (focusEventListeners == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(focusEventListeners);
	}
	
	public int getEventOffsetX() {
		return x;
	}

	public int getEventOffsetY() {
		return y;
	}

	public boolean isPassingEvents() {
		return passingEvents;
	}
	
	public void setPassingEvents(boolean passingEvents) {
		this.passingEvents = passingEvents;
	}
	
	public void dispatchMouseEvent(GSMouseEvent event, GSPanel source) {
		GSPanelContext.dispatchMouseEvent(event, source, this);
	}

	public void dispatchKeyEvent(GSKeyEvent event, GSPanel source) {
		GSPanelContext.dispatchKeyEvent(event, source, this);
	}
	
	public boolean isFocused() {
		return focused && focusable;
	}
	
	public void setFocused(boolean focused) {
		this.focused = focused;
	}
	
	public boolean isFocusable() {
		return focusable;
	}

	public void setFocusable(boolean focusable) {
		this.focusable = focusable;
	}
	
	public void requestFocus() {
		GSPanelContext.requestFocus(this);
	}

	public void unfocus() {
		if (isFocused())
			GSPanelContext.unfocus(this);
	}
	
	public boolean isEditingText() {
		return false;
	}
	
	public GSECursorType getCursor() {
		return cursor;
	}

	public void setCursor(GSECursorType cursor) {
		if (cursor == null)
			throw new IllegalArgumentException("cursor is null!");
		
		this.cursor = cursor;
	}
	
	public GSDimension getMinimumSize() {
		return (minimumSize != null) ? minimumSize : calculateMinimumSize();
	}

	protected GSDimension calculateMinimumSize() {
		return GSDimension.ZERO;
	}

	public void setMinimumSize(GSDimension minimumSize) {
		this.minimumSize = minimumSize;

		if (parent != null)
			parent.requestLayout();
	}

	public GSDimension getPreferredSize() {
		return (preferredSize != null) ? preferredSize : calculatePreferredSize();
	}
	
	protected GSDimension calculatePreferredSize() {
		return GSDimension.MAX_VALUE;
	}
	
	public void setPreferredSize(GSDimension preferredSize) {
		this.preferredSize = preferredSize;
		
		if (parent != null)
			parent.requestLayout();
	}

	public void requestLayout() {
		layout();
	}
	
	protected void putButtonStroke(GSIButtonStroke button, Runnable listener) {
		if (buttonStrokes == null)
			buttonStrokes = new LinkedHashMap<>();

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
		return GSPanelContext.hasI18nTranslation(key);
	}
	
	public String i18nTranslate(String key) {
		return GSPanelContext.i18nTranslate(key);
	}

	public String i18nTranslateFormatted(String key, Object... args) {
		return GSPanelContext.i18nTranslateFormatted(key, args);
	}
	
	public GSDropdown getRightClickMenu() {
		return null;
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
