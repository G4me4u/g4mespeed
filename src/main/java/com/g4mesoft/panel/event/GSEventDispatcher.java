package com.g4mesoft.panel.event;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.BiConsumer;

import com.g4mesoft.panel.GSECursorType;
import com.g4mesoft.panel.GSEPopupPlacement;
import com.g4mesoft.panel.GSLocation;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.GSPopup;
import com.g4mesoft.panel.GSRootPanel;
import com.g4mesoft.panel.dropdown.GSDropdown;

public class GSEventDispatcher {

	private static final int TRANSLATE_TO_ROOT   =  1;
	private static final int TRANSLATE_FROM_ROOT = -1;
	
	private final GSRootPanel rootPanel;
	
	private GSPanel focusedPanel;
	private GSPanel draggedPanel;
	private GSECursorType cursor;

	private GSPanel topFocusStealingPopup;
	private GSILayoutEventListener topPopupLayoutListener;
	private final Deque<GSPopup> topPopupStack;
	
	public GSEventDispatcher(GSRootPanel rootPanel) {
		this.rootPanel = rootPanel;
		
		focusedPanel = draggedPanel = null;
		cursor = GSECursorType.DEFAULT;
	
		topFocusStealingPopup = rootPanel;
		topPopupLayoutListener = null;
		topPopupStack = new ArrayDeque<>();
	}
	
	public void reset() {
		setFocusedPanel(null);
		setCurrentCursor(GSECursorType.DEFAULT);
	}
	
	public void mouseMoved(float mouseX, float mouseY) {
		int x = convertMouseX(mouseX);
		int y = convertMouseY(mouseY);

		GSPanel panel = getTopPanelAt(x, y);
		if (panel != null) {
			setCurrentCursor(panel.getCursor());

			GSMouseEvent event = GSMouseEvent.createMouseMovedEvent(x, y);
			translateMouseEvent(panel, event, TRANSLATE_FROM_ROOT);
			distributeMouseEvent(panel, event, GSIMouseListener::mouseMoved);
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
		if (draggedPanel != null && draggedPanel.isVisible()) {
			int x = convertMouseX(mouseX);
			int y = convertMouseY(mouseY);
			
			GSMouseEvent event = GSMouseEvent.createMouseDraggedEvent(x, y, button, dragX, dragY);
			translateMouseEvent(draggedPanel, event, TRANSLATE_FROM_ROOT);
			
			distributeMouseEvent(draggedPanel, event, GSIMouseListener::mouseDragged);
		}
	}

	public void mousePressed(int button, float mouseX, float mouseY, int modifiers) {
		int x = convertMouseX(mouseX);
		int y = convertMouseY(mouseY);
		
		GSPanel panel = getTopPanelAt(x, y);

		if (button != GSMouseEvent.UNKNOWN_BUTTON) {
			// Focus the top-most focusable ancestor of the panel
			// that was clicked (or the panel itself if focusable).
			setFocusedPanel(getTopFocusableAncestor(panel));
			// Set dragged panel for dragging events.
			draggedPanel = panel;
		}
		
		if (panel != null) {
			GSMouseEvent event = GSMouseEvent.createMousePressedEvent(x, y, button, modifiers);
			translateMouseEvent(panel, event, TRANSLATE_FROM_ROOT);
			
			distributeMouseEvent(panel, event, GSIMouseListener::mousePressed);
		}
	}
	
	private GSPanel getTopFocusableAncestor(GSPanel panel) {
		while (panel != null && !panel.isFocusable()) {
			if (isBottomMostFocusablePanel(panel)) {
				// Note: panel is not focusable, and is stealing
				//       focus from root panel. Return null.
				return null;
			}
			panel = panel.getParent();
		}
		return panel;
	}
	
	/*
	 * Note: should only be invoked with a panel which is a child of a popup in
	 *       {@code topPopupStack}, or root panel (i.e. it should be a panel that
	 *       is focusable according to topFocusStealingPopup).
	 */
	private boolean isBottomMostFocusablePanel(GSPanel panel) {
		return topFocusStealingPopup != null && (panel instanceof GSPopup);
	}

	public void mouseReleased(int button, float mouseX, float mouseY, int modifiers) {
		int x = convertMouseX(mouseX);
		int y = convertMouseY(mouseY);
		
		GSPanel hoveredPanel = getTopPanelAt(x, y);
		GSPanel panel = isChildOf(hoveredPanel, focusedPanel) ? hoveredPanel : focusedPanel;
		
		if (panel != null) {
			GSMouseEvent event = GSMouseEvent.createMouseReleasedEvent(x, y, button, modifiers);
			translateMouseEvent(panel, event, TRANSLATE_FROM_ROOT);

			int panelX = event.getX();
			int panelY = event.getY();
			
			distributeMouseEvent(panel, event, GSIMouseListener::mouseReleased);

			if (!event.isConsumed() && event.getButton() == GSMouseEvent.BUTTON_RIGHT) {
				// Only create right click menu if the hovered panel is a 
				// child of the focused panel (i.e. if panel = hoveredPanel).
				if (panel == hoveredPanel) {
					showRightClickMenu(panel, panelX, panelY);
					event.consume();
				}
			}
		}
	}
	
	private void showRightClickMenu(GSPanel panel, int x, int y) {
		GSDropdown dropdown = new GSDropdown();
		
		populateRightClickMenu(dropdown, panel, x, y);
		
		if (!dropdown.isEmpty()) {
			GSPopup popup = new GSPopup(dropdown, true);
			popup.setHiddenOnFocusLost(true);
			// The location is relative to the panel
			popup.show(panel, x, y, GSEPopupPlacement.RELATIVE);
		}
	}
	
	private void populateRightClickMenu(GSDropdown dropdown, GSPanel panel, int x, int y) {
		do {
			panel.populateRightClickMenu(dropdown, x, y);
			dropdown.separate();
			x += panel.getX();
			y += panel.getY();
			if (isBottomMostFocusablePanel(panel)) {
				// Do not go beyond bottom-most focusable panel.
				break;
			}
			panel = panel.getParent();
		} while (panel != null);
	}

	public void mouseScroll(float mouseX, float mouseY, float scrollX, float scrollY) {
		int x = convertMouseX(mouseX);
		int y = convertMouseY(mouseY);
		
		GSPanel panel = getTopPanelAt(x, y);

		if (panel != null) {
			GSMouseEvent event = GSMouseEvent.createMouseScrolledEvent(x, y, scrollX, scrollY);
			translateMouseEvent(panel, event, TRANSLATE_FROM_ROOT);
			distributeMouseEvent(panel, event, GSIMouseListener::mouseScrolled);
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
		
			// Support for accessibility menu button
			if (!event.isConsumed() && event.getKeyCode() == GSKeyEvent.KEY_MENU) {
				int x = focusedPanel.getWidth() / 2;
				int y = focusedPanel.getHeight() / 2;
				showRightClickMenu(focusedPanel, x, y);
			}
		}
	}

	public void keyTyped(int codePoint) {
		if (focusedPanel != null) {
			GSKeyEvent event = GSKeyEvent.createKeyTypedEvent(codePoint);
			distributeKeyEvent(focusedPanel, event, GSIKeyListener::keyTyped);
		}
	}
	
	public void requestFocus(GSPanel panel) {
		if (isFocusable(panel))
			setFocusedPanel(panel);
	}
	
	private boolean isFocusable(GSPanel panel) {
		if (!panel.isFocusable())
			return false;
		// Check if panel is child of popups that are stealing focus.
		if (topFocusStealingPopup != null && !topPopupStack.isEmpty()) {
			Iterator<GSPopup> itr = topPopupStack.iterator();
			GSPopup popup;
			do {
				popup = itr.next();
				if (isChildOf(panel, popup))
					return true;
			} while (itr.hasNext() && !popup.isStealingFocus());
			// Also check root panel if stealing focus.
			if (popup.isStealingFocus())
				return false;
		}
		return isChildOf(panel, rootPanel);
	}
	
	public void unfocus(GSPanel panel) {
		if (focusedPanel == panel)
			setFocusedPanel(null);
	}
	
	public GSPanel getFocusedPanel() {
		return focusedPanel;
	}
	
	private boolean isChildOf(GSPanel child, GSPanel parent) {
		while (child != null && child != parent)
			child = child.getParent();
		return (child == parent);
	}
	
	private void setFocusedPanel(GSPanel panel) {
		GSPanel oldFocusedPanel = focusedPanel;

		if (panel != oldFocusedPanel) {
			focusedPanel = panel;

			// Invoke panels with focus lost and gained events.
			if (oldFocusedPanel != null) {
				oldFocusedPanel.setFocused(false);

				GSFocusEvent event = GSFocusEvent.createFocusLostEvent();
				invokeFocusEventListeners(oldFocusedPanel, event, GSIFocusEventListener::focusLost);
			}
			
			// Iterate through popups, and hide those that should be hidden due
			// to change of focus. Note that we only hide the top of the stack.
			GSPopup popup;
			while (focusedPanel == panel && (popup = topPopupStack.peek()) != null) {
				if (!popup.isHiddenOnFocusLost() || isChildOf(panel, popup))
					break;
				popup.hide();
			}
			
			// The focused panel might have changed from the focus lost event, or due
			// to the popup focusing its source after being hidden.
			if (panel != null && focusedPanel == panel && !panel.isFocused()) {
				panel.setFocused(true);

				GSFocusEvent event = GSFocusEvent.createFocusGainedEvent();
				invokeFocusEventListeners(panel, event, GSIFocusEventListener::focusGained);
			}
		}
	}

	private void translateMouseEvent(GSPanel panel, GSMouseEvent event, int sign) {
		event.setLocation(translateLocation(panel, event.getLocation(), sign));
	}
	
	private GSLocation translateLocation(GSPanel panel, GSLocation location, int sign) {
		GSLocation viewLocation = GSPanelUtil.getViewLocation(panel);
		return new GSLocation(location.getX() + sign * viewLocation.getX(),
		                      location.getY() + sign * viewLocation.getY());
	}

	private GSPanel getTopPanelAt(int x, int y) {
		if (!topPopupStack.isEmpty()) {
			Iterator<GSPopup> itr = topPopupStack.iterator();
			GSPopup popup;
			do {
				popup = itr.next();
				// Note: popups are children of rootPanel, so the location
				//       is adjusted correctly.
				GSPanel panel = getTopPanelAt(popup, x, y);
				if (panel != null)
					return panel;
			} while (itr.hasNext() && !popup.isStealingFocus());
			// Also check root panel if not stealing focus
			if (popup.isStealingFocus())
				return null;
		}
		return getTopPanelAt(rootPanel, x, y);
	}
	
	private GSPanel getTopPanelAt(GSPanel bottomMostPanel, int x, int y) {
		GSPanel panel = null;
		
		if (bottomMostPanel.isInBounds(x, y)) {
			GSPanel child = bottomMostPanel;
			do {
				panel = child;
				
				x -= panel.getX();
				y -= panel.getY();
				child = panel.getChildAt(x, y);
			} while (child != null);
		}
		
		return panel;
	}
	
	public void dispatchMouseEvent(GSMouseEvent event, GSPanel source, GSPanel dest) {
		if (dest == null)
			throw new IllegalArgumentException("destination is null!");
		
		if (isChildOf(dest, rootPanel)) {
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

		if (isChildOf(dest, rootPanel)) {
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
	
	public void dispatchLayoutEvent(GSLayoutEvent event, GSPanel source, GSPanel dest) {
		if (dest == null)
			throw new IllegalArgumentException("destination is null!");
		
		switch (event.getType()) {
		case GSLayoutEvent.ADDED_TYPE:
			invokeLayoutEventListeners(dest, event, GSILayoutEventListener::panelAdded);
			break;
		case GSLayoutEvent.REMOVED_TYPE:
			invokeLayoutEventListeners(dest, event, GSILayoutEventListener::panelRemoved);
			break;
		case GSLayoutEvent.RESIZED_TYPE:
			invokeLayoutEventListeners(dest, event, GSILayoutEventListener::panelResized);
			break;
		case GSLayoutEvent.MOVED_TYPE:
			invokeLayoutEventListeners(dest, event, GSILayoutEventListener::panelMoved);
			break;
		case GSLayoutEvent.SHOWN_TYPE:
			invokeLayoutEventListeners(dest, event, GSILayoutEventListener::panelShown);
			break;
		case GSLayoutEvent.HIDDEN_TYPE:
			invokeLayoutEventListeners(dest, event, GSILayoutEventListener::panelHidden);
			break;
		case GSLayoutEvent.INVALIDATED_TYPE:
			invokeLayoutEventListeners(dest, event, GSILayoutEventListener::panelInvalidated);
			break;
		case GSLayoutEvent.VALIDATED_TYPE:
			invokeLayoutEventListeners(dest, event, GSILayoutEventListener::panelValidated);
			break;
		case GSLayoutEvent.UNKNOWN_TYPE:
		default:
			break;
		}
	}
	
	public void pushTopPopup(GSPopup popup) {
		if (popup != null && isChildOf(popup, rootPanel)) {
			topPopupStack.push(popup);
			if (popup.isStealingFocus()) {
				setTopFocusStealingPopup(popup);
				// Ensure focused and dragged panel are children.
				if (!isChildOf(focusedPanel, popup))
					setFocusedPanel(popup.isFocusable() ? popup : null);
				if (!isChildOf(draggedPanel, popup))
					draggedPanel = null;
			}
		}
	}

	public void popTopPopup(GSPopup popup) {
		if (popup == null)
			throw new IllegalStateException("popup is null!");
		
		if (topPopupStack.peek() != popup) {
			topPopupStack.remove(popup);
			if (popup != topFocusStealingPopup) {
				// No need to do anything else
				return;
			} else {
				// Top focus stealing popup updated below.
			}
		}
		
		// Find first popup in top-most popup stack that
		// is also visible (i.e. child of root panel).
		GSPopup next = topPopupStack.peek();
		while (next != null && (popup == next || !isChildOf(next, rootPanel))) {
			topPopupStack.poll();
			next = topPopupStack.peek();
		}
		// Note: next is head of stack.
		popup = next;
		
		if (popup != null && !popup.isStealingFocus()) {
			Iterator<GSPopup> itr = topPopupStack.iterator();
			while (itr.hasNext() && !popup.isStealingFocus())
				popup = itr.next();
			if (!popup.isStealingFocus()) {
				// No popup is stealing focus.
				popup = null;
			}
		}
		
		if (popup != topFocusStealingPopup)
			setTopFocusStealingPopup(popup);
	}

	private void setTopFocusStealingPopup(GSPopup popup) {
		if (topPopupLayoutListener != null) {
			topFocusStealingPopup.removeLayoutEventListener(topPopupLayoutListener);
			topPopupLayoutListener = null;
		}
		
		topFocusStealingPopup = popup;
		
		if (popup != null) {
			topPopupLayoutListener = new GSILayoutEventListener() {
				@Override
				public void panelRemoved(GSLayoutEvent event) {
					popTopPopup(popup);
				}
			};
			popup.addLayoutEventListener(topPopupLayoutListener);
		}
	}
	
	private void distributeMouseEvent(GSPanel panel, GSMouseEvent event, BiConsumer<GSIMouseListener, GSMouseEvent> method) {
		while (panel != null && !event.isConsumed()) {
			if (!panel.isPassingEvents())
				invokeMouseEventListeners(panel, event, method);

			event.setX(event.getX() + panel.getX());
			event.setY(event.getY() + panel.getY());
			
			if (isBottomMostFocusablePanel(panel)) {
				// Do not go beyond bottom-most focusable panel.
				break;
			}
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
			
			if (isBottomMostFocusablePanel(panel)) {
				// Do not go beyond bottom-most focusable panel.
				break;
			}
			panel = panel.getParent();
		}
	}
	
	private void invokeMouseEventListeners(GSPanel panel, GSMouseEvent event, BiConsumer<GSIMouseListener, GSMouseEvent> method) {
		if (panel.isEnabled()) {
			event.setPanel(panel);
			panel.iterateMouseEventListeners((listener) -> {
				method.accept(listener, event);
			});
		}
	}
	
	private void invokeKeyEventListeners(GSPanel panel, GSKeyEvent event, BiConsumer<GSIKeyListener, GSKeyEvent> method) {
		if (panel.isEnabled()) {
			event.setPanel(panel);
			panel.iterateKeyEventListeners((listener) -> {
				method.accept(listener, event);
			});
		}
	}

	private void invokeFocusEventListeners(GSPanel panel, GSFocusEvent event, BiConsumer<GSIFocusEventListener, GSFocusEvent> method) {
		event.setPanel(panel);
		panel.iterateFocusEventListeners((listener) -> {
			method.accept(listener, event);
		});
	}

	private void invokeLayoutEventListeners(GSPanel panel, GSLayoutEvent event, BiConsumer<GSILayoutEventListener, GSLayoutEvent> method) {
		event.setPanel(panel);
		panel.iterateLayoutEventListeners((listener) -> {
			method.accept(listener, event);
		});
	}
	
	private int convertMouseX(float mouseX) {
		return (int)mouseX;
	}

	private int convertMouseY(float mouseY) {
		return (int)mouseY;
	}
}
