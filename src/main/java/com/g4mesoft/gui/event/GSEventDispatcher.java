package com.g4mesoft.gui.event;

import java.util.function.BiConsumer;

import com.g4mesoft.gui.GSCursorType;
import com.g4mesoft.gui.GSIElement;
import com.g4mesoft.gui.GSElementContext;
import com.g4mesoft.gui.GSRootPanel;

public class GSEventDispatcher {

	private static final int BACKSPACE_CONTROL_CHARACTER = 0x08;
	private static final int TAB_CONTROL_CHARACTER       = 0x09;
	private static final int NEW_LINE_CONTROL_CHARACTER  = 0x0A;
	private static final int CONTROL_Z_CONTROL_CHARACTER = 0x1A;
	private static final int ESCAPE_CONTROL_CHARACTER    = 0x1B;
	private static final int DELETE_CONTROL_CHARACTER    = 0x7F;
	
	private final GSRootPanel rootPanel;
	
	private GSIElement focusedElement;
	private GSCursorType cursor;

	public GSEventDispatcher(GSRootPanel rootPanel) {
		this.rootPanel = rootPanel;
		
		focusedElement = null;
		cursor = GSCursorType.DEFAULT;
	}
	
	public void reset() {
		setFocusedElement(null);
		setCurrentCursor(GSCursorType.DEFAULT);
	}
	
	public void mouseMoved(float mouseX, float mouseY) {
		int x = convertMouseX(mouseX);
		int y = convertMouseY(mouseY);

		GSIElementResult result = getTopElementResultAt(x, y);
		if (result.element != null) {
			setCurrentCursor(result.element.getCursor());

			GSMouseEvent event = GSMouseEvent.createMouseMovedEvent(result.x, result.y);
			dispatchMouseEvent(result.element, event, GSIMouseListener::mouseMoved);
		} else {
			setCurrentCursor(GSCursorType.DEFAULT);
		}
	}
	
	private void setCurrentCursor(GSCursorType cursor) {
		if (cursor != this.cursor) {
			this.cursor = cursor;
			
			GSElementContext.setCursor(cursor);
		}
	}

	public void mouseDragged(int button, float mouseX, float mouseY, float dragX, float dragY) {
		if (focusedElement != null && button == GSMouseEvent.BUTTON_LEFT) {
			int x = convertMouseX(mouseX);
			int y = convertMouseY(mouseY);
			
			GSMouseEvent event = GSMouseEvent.createMouseDraggedEvent(x, y, button, dragX, dragY);
			event = translateMouseEvent(focusedElement, event);
			
			dispatchMouseEvent(focusedElement, event, GSIMouseListener::mouseDragged);
		}
	}

	public void mousePressed(int button, float mouseX, float mouseY, int modifiers) {
		int x = convertMouseX(mouseX);
		int y = convertMouseY(mouseY);
		
		if (button == GSMouseEvent.BUTTON_LEFT)
			setFocusedElement(getTopElementAt(x, y));
		
		if (focusedElement != null) {
			GSMouseEvent event = GSMouseEvent.createMousePressedEvent(x, y, button, modifiers);
			event = translateMouseEvent(focusedElement, event);
			
			dispatchMouseEvent(focusedElement, event, GSIMouseListener::mousePressed);
		}
	}

	public void mouseReleased(int button, float mouseX, float mouseY, int modifiers) {
		if (focusedElement != null) {
			int x = convertMouseX(mouseX);
			int y = convertMouseY(mouseY);
			
			GSMouseEvent event = GSMouseEvent.createMouseReleasedEvent(x, y, button, modifiers);
			event = translateMouseEvent(focusedElement, event);
			
			dispatchMouseEvent(focusedElement, event, GSIMouseListener::mouseReleased);
		}
	}

	public void mouseScroll(float mouseX, float mouseY, float scrollX, float scrollY) {
		int x = convertMouseX(mouseX);
		int y = convertMouseY(mouseY);
		
		GSIElementResult result = getTopElementResultAt(x, y);

		if (result.element != null) {
			GSMouseEvent event = GSMouseEvent.createMouseScrolledEvent(result.x, result.y, scrollX, scrollY);
			dispatchMouseEvent(result.element, event, GSIMouseListener::mouseScrolled);
		}
	}
	
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if (focusedElement != null) {
			GSKeyEvent event = GSKeyEvent.createKeyPressedEvent(keyCode, scanCode, modifiers);
			dispatchKeyEvent(focusedElement, event, GSIKeyListener::keyPressed);
		}
		
		checkAndDispatchControlCharacter(keyCode, modifiers);
	}
	
	private void checkAndDispatchControlCharacter(int key, int mods) {
		switch (key) {
		case GSKeyEvent.KEY_BACKSPACE:
			keyTyped(BACKSPACE_CONTROL_CHARACTER);
			break;
		case GSKeyEvent.KEY_TAB:
			keyTyped(TAB_CONTROL_CHARACTER);
			break;
		case GSKeyEvent.KEY_ENTER:
			keyTyped(NEW_LINE_CONTROL_CHARACTER);
			break;
		case GSKeyEvent.KEY_Z:
			if ((mods & GSEvent.MODIFIER_CONTROL) != 0)
				keyTyped(CONTROL_Z_CONTROL_CHARACTER);
			break;
		case GSKeyEvent.KEY_ESCAPE:
			keyTyped(ESCAPE_CONTROL_CHARACTER);
			break;
		case GSKeyEvent.KEY_DELETE:
			keyTyped(DELETE_CONTROL_CHARACTER);
			break;
		}
	}

	public void keyRepeated(int keyCode, int scanCode, int modifiers) {
		if (focusedElement != null) {
			GSKeyEvent event = GSKeyEvent.createKeyRepeatedEvent(keyCode, scanCode, modifiers);
			dispatchKeyEvent(focusedElement, event, GSIKeyListener::keyPressed);
		}
	}

	public void keyReleased(int keyCode, int scanCode, int modifiers) {
		if (focusedElement != null) {
			GSKeyEvent event = GSKeyEvent.createKeyReleasedEvent(keyCode, scanCode, modifiers);
			dispatchKeyEvent(focusedElement, event, GSIKeyListener::keyReleased);
		}
	}

	public void keyTyped(int codePoint) {
		if (focusedElement != null) {
			GSKeyEvent event = GSKeyEvent.createKeyTypedEvent(codePoint);
			dispatchKeyEvent(focusedElement, event, GSIKeyListener::keyTyped);
		}
	}
	
	public void requestFocus(GSIElement element) {
		if (isChildOfRoot(element))
			setFocusedElement(element);
	}
	
	private boolean isChildOfRoot(GSIElement element) {
		GSIElement parent;
		while ((parent = element.getParent()) != null)
			element = parent;
		return (element == rootPanel);
	}
	
	private void setFocusedElement(GSIElement element) {
		if (element != focusedElement) {
			if (focusedElement != null) {
				focusedElement.setFocused(false);

				GSFocusEvent event = GSFocusEvent.createFocusLostEvent();
				dispatchFocusEvent(focusedElement, event, GSIFocusEventListener::focusLost);
			}
			
			if (element != null) {
				element.setFocused(true);

				GSFocusEvent event = GSFocusEvent.createFocusGainedEvent();
				dispatchFocusEvent(element, event, GSIFocusEventListener::focusGained);
			}
			
			focusedElement = element;
		}
	}
	
	private GSMouseEvent translateMouseEvent(GSIElement element, GSMouseEvent event) {
		while (element != null) {
			event.setX(event.getX() - element.getEventOffsetX());
			event.setY(event.getY() - element.getEventOffsetY());
			
			element = element.getParent();
		}
		
		return event;
	}

	private GSIElement getTopElementAt(int x, int y) {
		return getTopElementResultAt(x, y).element;
	}
	
	private GSIElementResult getTopElementResultAt(int x, int y) {
		GSIElement element = null;
		
		if (rootPanel.isInBounds(x, y)) {
			GSIElement child = rootPanel;
			do {
				element = child;
				
				x -= element.getEventOffsetX();
				y -= element.getEventOffsetY();
				child = element.getChildAt(x, y);
			} while (child != null);
		}
		
		return new GSIElementResult(element, x, y);
	}
	
	private void dispatchMouseEvent(GSIElement panel, GSMouseEvent event, BiConsumer<GSIMouseListener, GSMouseEvent> method) {
		while (panel != null && !event.isConsumed()) {
			if (!panel.isPassingEvents()) {
				for (GSIMouseListener listener : panel.getMouseEventListeners())
					method.accept(listener, event);
			}

			event.setX(event.getX() + panel.getEventOffsetX());
			event.setY(event.getY() + panel.getEventOffsetY());
			
			panel = panel.getParent();
		}
	}
	
	private void dispatchKeyEvent(GSIElement panel, GSKeyEvent event, BiConsumer<GSIKeyListener, GSKeyEvent> method) {
		while (panel != null && !event.isConsumed()) {
			if (!panel.isPassingEvents()) {
				for (GSIKeyListener listener : panel.getKeyEventListeners())
					method.accept(listener, event);
				
				if (panel.isEditingText() && event.isPrintableKey())
					event.consume();
			}
			
			panel = panel.getParent();
		}
	}
	
	private void dispatchFocusEvent(GSIElement source, GSFocusEvent event, BiConsumer<GSIFocusEventListener, GSFocusEvent> method) {
		// Unlike mouse and key events, the focus events do not
		// dispatch throughout the element's parent-hierarchy.
		for (GSIFocusEventListener listener : source.getFocusEventListeners())
			method.accept(listener, event);
	}
	
	private int convertMouseX(float mouseX) {
		return (int)mouseX;
	}

	private int convertMouseY(float mouseY) {
		return (int)mouseY;
	}
	
	private class GSIElementResult {
		
		private final GSIElement element;
		private final int x;
		private final int y;
		
		public GSIElementResult(GSIElement element, int x, int y) {
			this.element = element;
			this.x = x;
			this.y = y;
		}
		
	}
}
