package com.g4mesoft.gui;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;

public interface GSParentElement extends GSElement, ParentElement {

	@Override
	default public void onMouseMovedGS(double mouseX, double mouseY) {
		for (Element element : children())
			element.mouseMoved(mouseX, mouseY);
	}

	@Override
	default public boolean onMouseClickedGS(double mouseX, double mouseY, int button, int mods) {
		return hoveredElement(mouseX, mouseY).filter((element) -> {
			if (element.mouseClicked(mouseX, mouseY, button)) {
				setFocused(element);

				if (button == GLFW.GLFW_MOUSE_BUTTON_1)
					setDragging(true);
				
				return true;
			}

			return false;
		}).isPresent();
	}

	@Override
	default public boolean onMouseReleasedGS(double mouseX, double mouseY, int button, int mods) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1)
			setDragging(false);

		for (Element element : children()) {
			if (element.mouseReleased(mouseX, mouseY, button))
				return true;
		}
		
		return false;
	}
	
	@Override
	default public boolean onMouseDraggedGS(double mouseX, double mouseY, int button, double dragX, double dragY) {
		Element focusedElement = getFocused();
		if (focusedElement == null || !isDragging() || button != GLFW.GLFW_MOUSE_BUTTON_1)
			return false;
		
		return focusedElement.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	default public boolean onMouseScrolledGS(double mouseX, double mouseY, double scrollX, double scrollY) {
		for (Element element : children()) {
			if (element.mouseScrolled(mouseX, mouseY, scrollY))
				return true;
		}
		
		return false;
	}
	
	@Override
	default public boolean onKeyPressedGS(int key, int scancode, int mods, boolean repeating) {
		Element focusedElement = getFocused();
		return focusedElement != null && focusedElement.keyPressed(key, scancode, mods);
	}

	@Override
	default public boolean onKeyReleasedGS(int key, int scancode, int mods) {
		Element focusedElement = getFocused();
		return focusedElement != null && focusedElement.keyReleased(key, scancode, mods);
	}

	@Override
	default public boolean onCharTypedGS(char c, int mods) {
		Element focusedElement = getFocused();
		return focusedElement != null && focusedElement.charTyped(c, mods);
	}
	
	default public boolean isChildEditingText() {
		Element focused = getFocused();
		while (focused != null) {
			if (focused instanceof GSParentElement) {
				GSParentElement element = ((GSParentElement)focused);
				return element.isChildEditingText();
			}

			if (focused instanceof GSElement) {
				GSElement element = ((GSElement)focused);
				return element.isEditingText();
			}

			Element nextFocused = null;
			if (focused instanceof ParentElement)
				nextFocused = ((ParentElement)focused).getFocused();
			
			focused = nextFocused;
		}
		
		return false;
	}
}
