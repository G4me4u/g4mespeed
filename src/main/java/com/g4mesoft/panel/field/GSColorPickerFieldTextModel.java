package com.g4mesoft.panel.field;

public class GSColorPickerFieldTextModel extends GSSingleLineTextModel {

	private final boolean alpha;
	
	public GSColorPickerFieldTextModel(boolean alpha) {
		this.alpha = alpha;
	}
	
	@Override
	protected boolean shouldDiscardCharacter(char c) {
		return !isHexCharacter(c);
	}
	
	public boolean isValidRGBColor() {
		// Check if we have the right amount of characters.
		return (!alpha && getLength() == 6) ||
		       ( alpha && getLength() == 8);
	}
	
	private boolean isHexCharacter(char c) {
		return (c >= '0' && c <= '9') ||
		       (c >= 'a' && c <= 'f') ||
		       (c >= 'A' && c <= 'F');
	}

	public boolean hasAlpha() {
		return alpha;
	}
}
