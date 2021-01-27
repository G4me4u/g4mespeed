package com.g4mesoft.panel.event;

import java.util.function.BiConsumer;

import com.g4mesoft.panel.GSECursorType;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSRootPanel;
import com.g4mesoft.panel.popup.GSDropdown;
import com.g4mesoft.panel.popup.GSPopup;

public class GSEventDispatcher {

	private static final int TRANSLATE_TO_ROOT   =  1;
	private static final int TRANSLATE_FROM_ROOT = -1;
	
	private final GSRootPanel rootPanel;
	
	private GSPanel focusedPanel;
	private GSECursorType cursor;

	public GSEventDispatcher(GSRootPanel rootPanel) {
		this.rootPanel = rootPanel;
		
		focusedPanel = null;
		cursor = GSECursorType.DEFAULT;
	}
	
	public void reset() {
		setFocusedPanel(null);
		setCurrentCursor(GSECursorType.DEFAULT);
	}
	
	public void mouseMoved(float mouseX, float mouseY) {
		int x = convertMouseX(mouseX);
		int y = convertMouseY(mouseY);

		GSPanelResult result = getTopPanelResultAt(x, y);
		if (result.panel != null) {
			setCurrentCursor(result.panel.getCursor());

			GSMouseEvent event = GSMouseEvent.createMouseMovedEvent(result.x, result.y);
			distributeMouseEvent(result.panel, event, GSIMouseListener::mouseMoved);
		} else {
			setCurrentCursor(GSECursorType.DEFAULT);
		}
	}
	
	private void setCurrentCursor(GSECursorType cursor) {
		if (cursor != this.cursor) {
			this.cursor = cursor;
			
			GSPanelContext.setCursor(cursor);
		}
	}

	public void mouseDragged(int button, float mouseX, float mouseY, float dragX, float dragY) {
		if (focusedPanel != null && button == GSMouseEvent.BUTTON_LEFT) {
			int x = convertMouseX(mouseX);
			int y = convertMouseY(mouseY);
			
			GSMouseEvent event = GSMouseEvent.createMouseDraggedEvent(x, y, button, dragX, dragY);
			event = translateMouseEvent(focusedPanel, event, TRANSLATE_FROM_ROOT);
			
			distributeMouseEvent(focusedPanel, event, GSIMouseListener::mouseDragged);
		}
	}

	public void mousePressed(int button, float mouseX, float mouseY, int modifiers) {
		int x = convertMouseX(mouseX);
		int y = convertMouseY(mouseY);
		
		GSPanel panel = getTopPanelAt(x, y);
		
		if (panel != null) {
			if (button != GSMouseEvent.UNKNOWN_BUTTON) {
				// Focus the top-most focusable ancestor of the panel
				// that was clicked (or the panel itself if focusable).
				setFocusedPanel(getTopFocusableAncestor(panel));
			}

			GSMouseEvent event = GSMouseEvent.createMousePressedEvent(x, y, button, modifiers);
			event = translateMouseEvent(panel, event, TRANSLATE_FROM_ROOT);
			
			distributeMouseEvent(panel, event, GSIMouseListener::mousePressed);
		}
	}
	
	private GSPanel getTopFocusableAncestor(GSPanel panel) {
		while (panel != null && !panel.isFocusable())
			panel = panel.getParent();
		return panel;
	}

	public void mouseReleased(int button, float mouseX, float mouseY, int modifiers) {
		int x = convertMouseX(mouseX);
		int y = convertMouseY(mouseY);
		
		GSPanelResult result = getTopPanelResultAt(x, y);
		
		if (result.panel != null) {
			GSMouseEvent event = GSMouseEvent.createMouseReleasedEvent(result.x, result.y, button, modifiers);
			distributeMouseEvent(result.panel, event, GSIMouseListener::mouseReleased);
			
			if (!event.isConsumed() && event.getButton() == GSMouseEvent.BUTTON_RIGHT) {
				GSDropdown dropdown = result.panel.getRightClickMenu();
				
				if (dropdown != null) {
					GSPopup popup = new GSPopup(dropdown);
					// The location is relative to the root panel
					popup.show(x, y);
					event.consume();
				}
			}
		}
	}

	public void mouseScroll(float mouseX, float mouseY, float scrollX, float scrollY) {
		int x = convertMouseX(mouseX);
		int y = convertMouseY(mouseY);
		
		GSPanelResult result = getTopPanelResultAt(x, y);

		if (result.panel != null) {
			GSMouseEvent event = GSMouseEvent.createMouseScrolledEvent(result.x, result.y, scrollX, scrollY);
			distributeMouseEvent(result.panel, event, GSIMouseListener::mouseScrolled);
		}
	}
	
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if (focusedPanel != null) {
			GSKeyEvent event = GSKeyEvent.createKeyPressedEvent(keyCode, scanCode, modifiers);
			distributeKeyEvent(focusedPanel, event, GSIKeyListener::keyPressed);
		}
	}
	
	public void keyRepeated(int keyCode, int scanCode, int modifiers) {
		if (focusedPanel != null) {
			GSKeyEvent event = GSKeyEvent.createKeyRepeatedEvent(keyCode, scanCode, modifiers);
			distributeKeyEvent(focusedPanel, event, GSIKeyListener::keyPressed);
		}
	}
	
	public void keyReleased(int keyCode, int scanCode, int modifiers) {
		if (focusedPanel != null) {
			GSKeyEvent event = GSKeyEvent.createKeyReleasedEvent(keyCode, scanCode, modifiers);
			distributeKeyEvent(focusedPanel, event, GSIKeyListener::keyReleased);
		}
	}

	public void keyTyped(int codePoint) {
		if (focusedPanel != null) {
			GSKeyEvent event = GSKeyEvent.createKeyTypedEvent(codePoint);
			distributeKeyEvent(focusedPanel, event, GSIKeyListener::keyTyped);
		}
	}
	
	public void requestFocus(GSPanel panel) {
		if (panel.isFocusable() && isChildOfRoot(panel))
			setFocusedPanel(panel);
	}
	
	public void unfocus(GSPanel panel) {
		if (focusedPanel == panel)
			setFocusedPanel(null);
	}
	
	public GSPanel getFocusedPanel() {
		return focusedPanel;
	}
	
	private boolean isChildOfRoot(GSPanel panel) {
		GSPanel parent;
		while ((parent = panel.getParent()) != null)
			panel = parent;
		return (panel == rootPanel);
	}
	
	private void setFocusedPanel(GSPanel panel) {
		GSPanel oldFocusedPanel = focusedPanel;

		if (panel != oldFocusedPanel) {
			focusedPanel = panel;

			if (oldFocusedPanel != null) {
				oldFocusedPanel.setFocused(false);

				GSFocusEvent event = GSFocusEvent.createFocusLostEvent();
				invokeFocusEventListeners(oldFocusedPanel, event, GSIFocusEventListener::focusLost);
			}
			
			// The focused panel might have changed from the focus lost event
			if (panel != null && focusedPanel == panel) {
				panel.setFocused(true);

				GSFocusEvent event = GSFocusEvent.createFocusGainedEvent();
				invokeFocusEventListeners(panel, event, GSIFocusEventListener::focusGained);
			}
		}
	}
	
	private GSMouseEvent translateMouseEvent(GSPanel panel, GSMouseEvent event, int sign) {
		while (panel != null) {
			event.setX(event.getX() + sign * panel.getEventOffsetX());
			event.setY(event.getY() + sign * panel.getEventOffsetY());
			
			panel = panel.getParent();
		}
		
		return event;
	}

	private GSPanel getTopPanelAt(int x, int y) {
		return getTopPanelResultAt(x, y).panel;
	}
	
	private GSPanelResult getTopPanelResultAt(int x, int y) {
		GSPanel panel = null;
		
		if (rootPanel.isInBounds(x, y)) {
			GSPanel child = rootPanel;
			do {
				panel = child;
				
				x -= panel.getEventOffsetX();
				y -= panel.getEventOffsetY();
				child = panel.getChildAt(x, y);
			} while (child != null);
		}
		
		return new GSPanelResult(panel, x, y);
	}
	
	public void dispatchMouseEvent(GSMouseEvent event, GSPanel source, GSPanel dest) {
		if (dest == null)
			throw new IllegalArgumentException("destination is null!");
		
		if (isChildOfRoot(dest)) {
			if (source != null)
				translateMouseEvent(source, event, TRANSLATE_TO_ROOT);
	
			translateMouseEvent(dest, event, TRANSLATE_FROM_ROOT);
			
			switch (event.getType()) {
			case GSMouseEvent.MOUSE_MOVED_TYPE:
				invokeMouseEventListeners(dest, event, GSIMouseListener::mouseMoved);
				break;
			case GSMouseEvent.MOUSE_DRAGGED_TYPE:
				invokeMouseEventListeners(dest, event, GSIMouseListener::mouseDragged);
				break;
			case GSMouseEvent.MOUSE_PRESSED_TYPE:
				invokeMouseEventListeners(dest, event, GSIMouseListener::mousePressed);
				break;
			case GSMouseEvent.MOUSE_RELEASED_TYPE:
				invokeMouseEventListeners(dest, event, GSIMouseListener::mouseReleased);
				break;
			case GSMouseEvent.MOUSE_SCROLLED_TYPE:
				invokeMouseEventListeners(dest, event, GSIMouseListener::mouseScrolled);
				break;
			case GSMouseEvent.UNKNOWN_TYPE:
			default:
				break;
			}
		}
	}

	public void dispatchKeyEvent(GSKeyEvent event, GSPanel source, GSPanel dest) {
		if (dest == null)
			throw new IllegalArgumentException("destination is null!");

		if (isChildOfRoot(dest)) {
			switch (event.getType()) {
			case GSKeyEvent.KEY_PRESSED_TYPE:
			case GSKeyEvent.KEY_REPEATED_TYPE:
				invokeKeyEventListeners(dest, event, GSIKeyListener::keyPressed);
				break;
			case GSKeyEvent.KEY_RELEASED_TYPE:
				invokeKeyEventListeners(dest, event, GSIKeyListener::keyReleased);
				break;
			case GSKeyEvent.KEY_TYPED_TYPE:
				invokeKeyEventListeners(dest, event, GSIKeyListener::keyTyped);
				break;
			case GSKeyEvent.UNKNOWN_TYPE:
			default:
				break;
			}
		}
	}
	
	private void distributeMouseEvent(GSPanel panel, GSMouseEvent event, BiConsumer<GSIMouseListener, GSMouseEvent> method) {
		while (panel != null && !event.isConsumed()) {
			if (!panel.isPassingEvents())
				invokeMouseEventListeners(panel, event, method);

			event.setX(event.getX() + panel.getEventOffsetX());
			event.setY(event.getY() + panel.getEventOffsetY());
			
			panel = panel.getParent();
		}
	}
	
	private void distributeKeyEvent(GSPanel panel, GSKeyEvent event, BiConsumer<GSIKeyListener, GSKeyEvent> method) {
		while (panel != null && !event.isConsumed()) {
			if (!panel.isPassingEvents()) {
				invokeKeyEventListeners(panel, event, method);
				
				if (panel.isEditingText() && event.isPrintableKey())
					event.consume();
			}
			
			panel = panel.getParent();
		}
	}
	
	private void invokeMouseEventListeners(GSPanel panel, GSMouseEvent event, BiConsumer<GSIMouseListener, GSMouseEvent> method) {
		for (GSIMouseListener listener : panel.getMouseEventListeners())
			method.accept(listener, event);
	}
	
	private void invokeKeyEventListeners(GSPanel panel, GSKeyEvent event, BiConsumer<GSIKeyListener, GSKeyEvent> method) {
		for (GSIKeyListener listener : panel.getKeyEventListeners())
			method.accept(listener, event);
	}

	private void invokeFocusEventListeners(GSPanel source, GSFocusEvent event, BiConsumer<GSIFocusEventListener, GSFocusEvent> method) {
		for (GSIFocusEventListener listener : source.getFocusEventListeners())
			method.accept(listener, event);
	}
	
	private int convertMouseX(float mouseX) {
		return (int)mouseX;
	}

	private int convertMouseY(float mouseY) {
		return (int)mouseY;
	}
	
	private class GSPanelResult {
		
		private final GSPanel panel;
		private final int x;
		private final int y;
		
		public GSPanelResult(GSPanel panel, int x, int y) {
			this.panel = panel;
			this.x = x;
			this.y = y;
		}
	}
}
