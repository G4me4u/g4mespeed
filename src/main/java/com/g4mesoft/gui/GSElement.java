package com.g4mesoft.gui;

import net.minecraft.client.gui.Element;

public interface GSElement extends Element {

	default public void onMouseMovedGS(double mouseX, double mouseY) {
	}

	default public boolean onMouseClickedGS(double mouseX, double mouseY, int button) {
		return false;
	}

	default public boolean onMouseReleasedGS(double mouseX, double mouseY, int button) {
		return false;
	}

	default public boolean onMouseDraggedGS(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return false;
	}

	default public boolean onKeyPressedGS(int key, int scancode, int mods, boolean repeating) {
		return false;
	}

	default public boolean onKeyReleasedGS(int key, int scancode, int mods) {
		return false;
	}

	default public boolean onCharTypedGS(char c, int mods) {
		return false;
	}

	default public boolean onMouseScrolledGS(double mouseX, double mouseY, double scrollX, double scrollY) {
		return false;
	}
	
	public boolean isAdded();
	
	public boolean isElementFocused();

	public void setElementFocused(boolean focused);
	
	public boolean isEditingText();
	
}
