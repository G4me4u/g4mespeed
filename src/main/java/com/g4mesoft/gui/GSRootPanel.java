package com.g4mesoft.gui;

import java.util.Collections;
import java.util.List;

import com.g4mesoft.access.GSIKeyboardAccess;
import com.g4mesoft.access.GSIMouseAccess;
import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.gui.event.GSEventDispatcher;
import com.g4mesoft.gui.event.GSIFocusEventListener;
import com.g4mesoft.gui.event.GSIKeyListener;
import com.g4mesoft.gui.event.GSIMouseListener;
import com.g4mesoft.gui.event.GSKeyEvent;
import com.g4mesoft.gui.event.GSMouseEvent;
import com.g4mesoft.gui.renderer.GSIRenderer2D;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;

public final class GSRootPanel extends Screen implements GSIParentElement {

	private GSIElement content;

	private boolean visible;
	private boolean elementFocused;
	
	GSRootPanel() {
		super(NarratorManager.EMPTY);
	
		content = null;
		
		visible = false;
		elementFocused = false;
	}

	@Override
	@GSCoreOverride
	protected void init() {
		super.init();
	
		client.keyboard.enableRepeatEvents(true);
		
		if (content != null)
			content.setBounds(0, 0, width, height);
		
		setVisibleImpl(true);
	}
	
	@Override
	@GSCoreOverride
	public void removed() {
		super.removed();

		client.keyboard.enableRepeatEvents(false);

		setVisibleImpl(false);
	}
	
	@Override
	@GSCoreOverride
	public void tick() {
		super.tick();
		
		update();
	}
	
	@Override
	@GSCoreOverride
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);

		GSIRenderer2D renderer = GSElementContext.getRenderer();

		renderer.beginRendering(matrixStack, mouseX, mouseY, partialTicks);
		
		preRender(renderer);
		render(renderer);
		postRender(renderer);

		renderer.endRendering();
	}

	@Override
	@GSCoreOverride
	public void mouseMoved(double mouseX, double mouseY) {
		GSElementContext.getEventDispatcher().mouseMoved((float)mouseX, (float)mouseY);
	}

	@Override
	@GSCoreOverride
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int modifiers = ((GSIMouseAccess)client.mouse).getPreviousEventModifiers();
		GSElementContext.getEventDispatcher().mousePressed(button, (float)mouseX, (float)mouseY, modifiers);
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		int modifiers = ((GSIMouseAccess)client.mouse).getPreviousEventModifiers();
		GSElementContext.getEventDispatcher().mouseReleased(button, (float)mouseX, (float)mouseY, modifiers);
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		GSElementContext.getEventDispatcher().mouseDragged(button, (float)mouseX, (float)mouseY, (float)deltaX, (float)deltaY);
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
		float scrollX = (float)((GSIMouseAccess)client.mouse).getPreviousEventScrollX();
		GSElementContext.getEventDispatcher().mouseScroll((float)mouseX, (float)mouseY, scrollX, (float)scrollY);
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (((GSIKeyboardAccess)client.keyboard).isPreviousEventRepeating()) {
			GSElementContext.getEventDispatcher().keyRepeated(keyCode, scanCode, modifiers);
		} else {
			GSElementContext.getEventDispatcher().keyPressed(keyCode, scanCode, modifiers);
		}
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		GSElementContext.getEventDispatcher().keyReleased(keyCode, scanCode, modifiers);
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean charTyped(char chr, int keyCode) {
		GSElementContext.getEventDispatcher().keyTyped((int)chr);
		return true;
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		throw new IllegalStateException("Root panel can not have bounds set!");
	}
	
	@Override
	public boolean isAdded() {
		return (client.currentScreen == this);
	}

	@Override
	public GSIElement getParent() {
		return null;
	}
	
	@Override
	public void onAdded(GSIElement parent) {
		throw new IllegalStateException("Root panel can not have a parent!");
	}

	@Override
	public void onRemoved(GSIElement parent) {
		throw new IllegalStateException("Root panel does not have a parent!");
	}
	
	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		throw new UnsupportedOperationException();
	}
	
	private void setVisibleImpl(boolean visible) {
		// Make sure it is not possible to call this function from
		// other client code, since it would break certain states.
		
		if (visible != this.visible) {
			this.visible = visible;
		
			if (content != null)
				content.setVisible(visible);
		}
	}

	@Override
	public void update() {
		if (content != null)
			content.update();
	}

	@Override
	public void preRender(GSIRenderer2D renderer) {
	}

	@Override
	public void render(GSIRenderer2D renderer) {
		if (content != null) {
			content.preRender(renderer);
			content.render(renderer);
			content.postRender(renderer);
		}
	}

	@Override
	public void postRender(GSIRenderer2D renderer) {
	}

	@Override
	public boolean isInBounds(int x, int y) {
		return (x >= 0 && y >= 0 && x < width && y < height);
	}

	@Override
	public int getX() {
		return 0;
	}

	@Override
	public int getY() {
		return 0;
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
	}

	@Override
	public void removeMouseEventListener(GSIMouseListener eventListener) {
	}

	@Override
	public List<GSIMouseListener> getMouseEventListeners() {
		return Collections.emptyList();
	}

	@Override
	public void addKeyEventListener(GSIKeyListener eventListener) {
	}

	@Override
	public void removeKeyEventListener(GSIKeyListener eventListener) {
	}

	@Override
	public List<GSIKeyListener> getKeyEventListeners() {
		return Collections.emptyList();
	}

	@Override
	public void addFocusEventListener(GSIFocusEventListener eventListener) {
	}

	@Override
	public void removeFocusEventListener(GSIFocusEventListener eventListener) {
	}

	@Override
	public List<GSIFocusEventListener> getFocusEventListeners() {
		return Collections.emptyList();
	}

	@Override
	public int getEventOffsetX() {
		return 0;
	}

	@Override
	public int getEventOffsetY() {
		return 0;
	}
	
	@Override
	public boolean isPassingEvents() {
		return false;
	}
	
	@Override
	public void setPassingEvents(boolean passingEvents) {
	}
	
	@Override
	public void dispatchMouseEvent(GSMouseEvent event, GSIElement source) {
	}

	@Override
	public void dispatchKeyEvent(GSKeyEvent event, GSIElement source) {
	}

	@Override
	public boolean isFocused() {
		return elementFocused;
	}

	@Override
	public void setFocused(boolean focused) {
		elementFocused = focused;
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
	public GSECursorType getCursor() {
		return GSECursorType.DEFAULT;
	}

	@Override
	public void setCursor(GSECursorType cursor) {
	}
	
	@Override
	public GSIElement getChildAt(int x, int y) {
		return content;
	}

	public void setContent(GSIElement element) {
		if (content != null) {
			content.setVisible(false);
			content.onRemoved(this);
		}
		
		content = element;
		
		if (element != null) {
			element.onAdded(this);
			element.setBounds(0, 0, width, height);

			element.setVisible(visible);
			
			GSEventDispatcher eventDispatcher = GSElementContext.getEventDispatcher();
			
			// Only request focus if elements have not requested
			// focus when they were added to the root panel.
			GSIElement focusedElement = eventDispatcher.getFocusedElement();
			if (focusedElement == this || focusedElement == null)
				element.requestFocus();
		} else {
			requestFocus();
		}
	}
	
	@Override
	public void add(GSIElement element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(GSIElement element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAll() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<GSIElement> getChildren() {
		return Collections.singletonList(content);
	}
}
