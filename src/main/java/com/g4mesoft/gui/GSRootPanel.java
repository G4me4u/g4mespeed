package com.g4mesoft.gui;

import java.util.Collections;
import java.util.List;

import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.gui.event.GSIFocusEventListener;
import com.g4mesoft.gui.event.GSIKeyListener;
import com.g4mesoft.gui.event.GSIMouseListener;
import com.g4mesoft.gui.renderer.GSIRenderer2D;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;

public final class GSRootPanel extends Screen implements GSParentElement {

	private GSIElement content;
	
	private boolean elementFocused;
	
	GSRootPanel() {
		super(NarratorManager.EMPTY);
	}

	@Override
	protected void init() {
		super.init();
		
		content.setBounds(0, 0, width, height);
	}
	
	@Override
	@GSCoreOverride
	public void tick() {
		super.tick();
		
		update();
	}
	
	@Override
	@GSCoreOverride
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);

		GSIRenderer2D renderer = GSElementContext.getRenderer();

		renderer.beginRendering(mouseX, mouseY, partialTicks);
		
		preRender(renderer);
		render(renderer);
		postRender(renderer);

		renderer.endRendering();
	}

	@Override
	@GSCoreOverride
	public void mouseMoved(double mouseX, double mouseY) {
	}

	@Override
	@GSCoreOverride
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	@GSCoreOverride
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return false;
	}

	@Override
	@GSCoreOverride
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return false;
	}

	@Override
	@GSCoreOverride
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return false;
	}

	@Override
	@GSCoreOverride
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return false;
	}

	@Override
	@GSCoreOverride
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return false;
	}

	@Override
	@GSCoreOverride
	public boolean charTyped(char chr, int keyCode) {
		return false;
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		throw new IllegalStateException("Root panel can not have bounds set!");
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
	public boolean isAdded() {
		return (minecraft.currentScreen == this);
	}

	@Override
	public GSIElement getParent() {
		return null;
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
	public boolean isEditingText() {
		return false;
	}

	@Override
	public GSCursorType getCursor() {
		return GSCursorType.DEFAULT;
	}

	@Override
	public void setCursor(GSCursorType cursor) {
	}
	
	@Override
	public GSIElement getChildAt(int x, int y) {
		return content;
	}

	public void setContent(GSIElement element) {
		if (content != null)
			content.onRemoved(this);
		
		content = element;
		
		if (element != null) {
			element.onAdded(this);
			element.setBounds(0, 0, width, height);

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