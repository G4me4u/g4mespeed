package com.g4mesoft.panel;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.g4mesoft.panel.dropdown.GSDropdown;
import com.g4mesoft.panel.event.GSEvent;
import com.g4mesoft.panel.event.GSIButtonStroke;
import com.g4mesoft.panel.event.GSIFocusEventListener;
import com.g4mesoft.panel.event.GSIKeyListener;
import com.g4mesoft.panel.event.GSILayoutEventListener;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.panel.event.GSLayoutEvent;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSMathUtil;

public class GSPanel {
	
	public static final GSILayoutProperty<Integer> MINIMUM_WIDTH      = GSLayoutProperties.MINIMUM_WIDTH;
	public static final GSILayoutProperty<Integer> MINIMUM_HEIGHT     = GSLayoutProperties.MINIMUM_HEIGHT;
	public static final GSILayoutProperty<GSDimension> MINIMUM_SIZE   = GSLayoutProperties.MINIMUM_SIZE;

	public static final GSILayoutProperty<Integer> PREFERRED_WIDTH    = GSLayoutProperties.PREFERRED_WIDTH;
	public static final GSILayoutProperty<Integer> PREFERRED_HEIGHT   = GSLayoutProperties.PREFERRED_HEIGHT;
	public static final GSILayoutProperty<GSDimension> PREFERRED_SIZE = GSLayoutProperties.PREFERRED_SIZE;
	
	public static final int DEFAULT_EVENT_LISTENER_PRIORITY = 0;
	public static final float FULLY_OPAQUE      = 1.0f;
	public static final float FULLY_TRANSPARENT = 0.0f;
	
	private GSPanel parent;
	
	private boolean visible;
	
	public int x;
	public int y;
	public int width;
	public int height;
	
	private GSPriorityEventListenerList<GSIMouseListener> mouseEventListeners;
	private GSPriorityEventListenerList<GSIKeyListener> keyEventListeners;
	private GSPriorityEventListenerList<GSIFocusEventListener> focusEventListeners;
	private GSPriorityEventListenerList<GSILayoutEventListener> layoutEventListeners;
	
	private boolean passingEvents;
	private boolean focused;
	private boolean focusable;
	
	private boolean enabled;
	
	private int popupCount;
	
	private Map<GSIButtonStroke, Runnable> buttonStrokes;
	private GSIMouseListener buttonMouseListener;
	private GSIKeyListener buttonKeyListener;

	protected GSECursorType cursor;
	
	protected final GSLayout layout;
	protected GSDimension cachedMinimumSize;
	protected GSDimension cachedPreferredSize;
	private boolean valid;
	
	private boolean validating;
	private boolean invalidateLater;
	
	private float opacity;
	
	public GSPanel() {
		parent = null;
		
		mouseEventListeners = null;
		keyEventListeners = null;
		focusEventListeners = null;
		layoutEventListeners = null;
		
		focused = false;
		// All panels are focusable by default
		focusable = true;
		// All panels are enabled by default
		enabled = true;
		
		cursor = GSECursorType.DEFAULT;
		
		cachedMinimumSize = null;
		cachedPreferredSize = null;
		layout = new GSLayout(this);
		valid = false;

		validating = false;
		invalidateLater = false;
		
		opacity = FULLY_OPAQUE;
	}

	public void add(GSPanel panel) {
		throw new UnsupportedOperationException("Not a parent panel");
	}
	
	public void remove(GSPanel panel) {
		throw new UnsupportedOperationException("Not a parent panel");
	}

	public void remove(int index) {
		throw new UnsupportedOperationException("Not a parent panel");
	}

	public void removeAll() {
		throw new UnsupportedOperationException("Not a parent panel");
	}
	
	public GSPanel get(int index) {
		throw new UnsupportedOperationException("Not a parent panel");
	}
	
	public GSPanel getChildAt(int x, int y) {
		return null;
	}
	
	public boolean isEmpty() {
		return true;
	}
	
	public List<GSPanel> getChildren() {
		return Collections.emptyList();
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

	public int getWidth() {
		return width;
	}
	
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

	public void setBounds(GSLocation location, GSDimension size) {
		setBounds(location.getX(), location.getY(), size.getWidth(), size.getHeight());
	}
	
	public void setBounds(int x, int y, int width, int height) {
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("width and height must be non-negative!");
		
		int oldX = this.x;
		int oldY = this.y;
		int oldWidth = this.width;
		int oldHeight = this.height;

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		if (width != oldWidth || height != oldHeight) {
			invalidate();
			onResized(oldWidth, oldHeight);
			dispatchLayoutEvent(GSLayoutEvent.createResizedEvent(), this);
		}

		if (x != oldX || y != oldY) {
			onMoved(oldX, oldY);
			dispatchLayoutEvent(GSLayoutEvent.createMovedEvent(), this);
		}
	}
	
	protected void onResized(int oldWidth, int oldHeight) {
	}

	protected void onMoved(int oldX, int oldY) {
	}
	
	protected void layout() {
	}
	
	public GSILayoutManager getLayoutManager() {
		throw new UnsupportedOperationException("Not a parent panel");
	}

	public void setLayoutManager(GSILayoutManager layoutManager) {
		throw new UnsupportedOperationException("Not a parent panel");
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
		
		dispatchLayoutEvent(GSLayoutEvent.createAddedEvent(), parent);
	}

	public void onRemoved(GSPanel parent) {
		if (parent == null || parent != this.parent)
			throw new IllegalArgumentException("Panel does not have the specified parent!");
		
		this.parent = null;
		
		unfocus();

		dispatchLayoutEvent(GSLayoutEvent.createRemovedEvent(), parent);
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		if (visible != this.visible) {
			this.visible = visible;
		
			if (visible) {
				onShown();
				dispatchLayoutEvent(GSLayoutEvent.createShownEvent(), this);
			} else {
				onHidden();
				dispatchLayoutEvent(GSLayoutEvent.createHiddenEvent(), this);
			}
		}
	}
	
	protected void onShown() {
	}

	protected void onHidden() {
	}
	
	public void preRender(GSIRenderer2D renderer) {
		renderer.pushMatrix();
		renderer.translate(getX(), getY());
		renderer.pushOpacity(opacity);
	}
	
	public void render(GSIRenderer2D renderer) {
	}
	
	public void postRender(GSIRenderer2D renderer) {
		renderer.popOpacity();
		renderer.popMatrix();
	}
	
	public boolean isInBounds(int x, int y) {
		if (x < this.x || x >= this.x + width)
			return false;
		if (y < this.y || y >= this.y + height)
			return false;
		return true;
	}

	public void addMouseEventListener(GSIMouseListener eventListener) {
		addMouseEventListener(eventListener, DEFAULT_EVENT_LISTENER_PRIORITY);
	}
	
	public void addMouseEventListener(GSIMouseListener eventListener, int priority) {
		if (mouseEventListeners == null)
			mouseEventListeners = new GSPriorityEventListenerList<GSIMouseListener>();
		
		mouseEventListeners.add(eventListener, priority);
	}

	public void removeMouseEventListener(GSIMouseListener eventListener) {
		if (mouseEventListeners != null) {
			mouseEventListeners.remove(eventListener);
			
			if (mouseEventListeners.isEmpty())
				mouseEventListeners = null;
		}
	}

	public Collection<GSIMouseListener> getMouseEventListeners() {
		if (mouseEventListeners == null)
			return Collections.emptyList();
		return mouseEventListeners.asCollection();
	}

	public void addKeyEventListener(GSIKeyListener eventListener) {
		addKeyEventListener(eventListener, DEFAULT_EVENT_LISTENER_PRIORITY);
	}
	
	public void addKeyEventListener(GSIKeyListener eventListener, int priority) {
		if (keyEventListeners == null)
			keyEventListeners = new GSPriorityEventListenerList<GSIKeyListener>();
		
		keyEventListeners.add(eventListener, priority);
	}
	
	public void removeKeyEventListener(GSIKeyListener eventListener) {
		if (keyEventListeners != null) {
			keyEventListeners.remove(eventListener);
			
			if (keyEventListeners.isEmpty())
				keyEventListeners = null;
		}
	}

	public Collection<GSIKeyListener> getKeyEventListeners() {
		if (keyEventListeners == null)
			return Collections.emptyList();
		return keyEventListeners.asCollection();
	}

	public void addFocusEventListener(GSIFocusEventListener eventListener) {
		addFocusEventListener(eventListener, DEFAULT_EVENT_LISTENER_PRIORITY);
	}

	public void addFocusEventListener(GSIFocusEventListener eventListener, int priority) {
		if (focusEventListeners == null)
			focusEventListeners = new GSPriorityEventListenerList<GSIFocusEventListener>();
		
		focusEventListeners.add(eventListener, priority);
	}
	
	public void removeFocusEventListener(GSIFocusEventListener eventListener) {
		if (focusEventListeners != null) {
			focusEventListeners.remove(eventListener);
			
			if (focusEventListeners.isEmpty())
				focusEventListeners = null;
		}
	}

	public Collection<GSIFocusEventListener> getFocusEventListeners() {
		if (focusEventListeners == null)
			return Collections.emptyList();
		return focusEventListeners.asCollection();
	}
	
	public void addLayoutEventListener(GSILayoutEventListener eventListener) {
		addLayoutEventListener(eventListener, DEFAULT_EVENT_LISTENER_PRIORITY);
	}

	public void addLayoutEventListener(GSILayoutEventListener eventListener, int priority) {
		if (layoutEventListeners == null)
			layoutEventListeners = new GSPriorityEventListenerList<GSILayoutEventListener>();
		
		layoutEventListeners.add(eventListener, priority);
	}
	
	public void removeLayoutEventListener(GSILayoutEventListener layoutListener) {
		if (layoutEventListeners != null) {
			layoutEventListeners.remove(layoutListener);
			
			if (layoutEventListeners.isEmpty())
				layoutEventListeners = null;
		}
	}

	public Collection<GSILayoutEventListener> getLayoutEventListeners() {
		if (layoutEventListeners == null)
			return Collections.emptyList();
		return layoutEventListeners.asCollection();
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

	public void dispatchLayoutEvent(GSLayoutEvent event, GSPanel source) {
		GSPanelContext.dispatchLayoutEvent(event, source, this);
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
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean hasPopupVisible() {
		return (popupCount != 0);
	}
	
	/* Visible for GSPopup */
	void incrementPopupCount() {
		popupCount++;
	}

	/* Visible for GSPopup */
	void decrementPopupCount() {
		if (popupCount <= 0)
			throw new IllegalStateException("Popup count must be non-negative");
		popupCount--;
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
	
	/* Visible for GSLayoutProperties */
	int getDefaultMinimumWidth() {
		if (cachedMinimumSize == null)
			cachedMinimumSize = calculateMinimumSize();
		return cachedMinimumSize.getWidth();
	}

	/* Visible for GSLayoutProperties */
	int getDefaultMinimumHeight() {
		if (cachedMinimumSize == null)
			cachedMinimumSize = calculateMinimumSize();
		return cachedMinimumSize.getHeight();
	}
	
	/* Visible for GSLayoutProperties */
	int getDefaultPreferredWidth() {
		if (cachedPreferredSize == null)
			cachedPreferredSize = calculatePreferredSize();
		return cachedPreferredSize.getWidth();
	}

	/* Visible for GSLayoutProperties */
	int getDefaultPreferredHeight() {
		if (cachedPreferredSize == null)
			cachedPreferredSize = calculatePreferredSize();
		return cachedPreferredSize.getHeight();
	}
	
	protected GSDimension calculateMinimumSize() {
		return calculatePreferredSize();
	}
	
	protected GSDimension calculatePreferredSize() {
		return GSDimension.ZERO;
	}

	public GSLayout getLayout() {
		return layout;
	}
	
	public <T> T getProperty(GSILayoutProperty<T> property) {
		return layout.get(property);
	}

	public <T> void setProperty(GSILayoutProperty<T> property, T value) {
		layout.set(property, value);
	}

	protected final void invalidate() {
		if (validating) {
			invalidateLater = true;
		} else if (valid) {
			invalidateNow();
			dispatchLayoutEvent(GSLayoutEvent.createInvalidatedEvent(), this);
		}
	}
	
	void invalidateNow() {
		valid = false;
		
		// Invalidate cached sizes
		cachedMinimumSize = null;
		cachedPreferredSize = null;
		
		GSPanelContext.scheduleValidation(this);
	}
	
	void revalidate() {
		if (!valid) {
			validating = true;
			try {
				validate();
				valid = true;
			} finally {
				validating = false;
			}
			dispatchLayoutEvent(GSLayoutEvent.createValidatedEvent(), this);
		}
		
		if (invalidateLater) {
			// We invalidated during validation. Move the invalidation
			// here instead, to ensure that children are invalidated.
			// A fresh revalidation will be scheduled immediately.
			invalidate();
			invalidateLater = false;
		}
	}

	protected void validate() {
		layout();
	}
	
	public final boolean isValid() {
		return valid;
	}

	public final boolean isValidating() {
		return validating;
	}
	
	public float getOpacity() {
		return opacity;
	}
	
	public void setOpacity(float opacity) {
		this.opacity = GSMathUtil.clamp(opacity, FULLY_TRANSPARENT, FULLY_OPAQUE);
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
	
	public void populateRightClickMenu(GSDropdown dropdown, int x, int y) {
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
