package com.g4mesoft.panel.event;

import com.g4mesoft.hotkey.GSKeyBinding;

import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.client.util.InputUtil.Type;

public class GSKeyBindingButtonStroke implements GSIButtonStroke {

	private final GSKeyBinding keyBinding;
	
	public GSKeyBindingButtonStroke(GSKeyBinding keyBinding) {
		if (keyBinding == null)
			throw new IllegalArgumentException("keyBinding is null!");
		
		this.keyBinding = keyBinding;
	}
	
	@Override
	public boolean isMatching(GSEvent event) {
		Key keyCode = keyBinding.getKeyCode();
		
		switch (keyCode.getCategory()) {
		case MOUSE:
			if (event instanceof GSMouseEvent)
				return isMouseEventMatching((GSMouseEvent)event);
			return false;
		case KEYSYM:
		case SCANCODE:
			if (event instanceof GSKeyEvent)
				return isKeyEventMatching((GSKeyEvent)event);
			return false;
		default:
			break;
		}
		
		return false;
	}
	
	private boolean isMouseEventMatching(GSMouseEvent event) {
		if (event.getType() != GSMouseEvent.MOUSE_DRAGGED_TYPE &&
		    event.getType() != GSMouseEvent.MOUSE_PRESSED_TYPE &&
		    event.getType() != GSMouseEvent.MOUSE_RELEASED_TYPE) {
			
			// Only support pressed and released.
			return false;
		}
		
		Key keyCode = keyBinding.getKeyCode();
		
		return (event.getButton() == keyCode.getCode());
	}
	
	private boolean isKeyEventMatching(GSKeyEvent event) {
		if (event.getType() != GSKeyEvent.KEY_PRESSED_TYPE &&
		    event.getType() != GSKeyEvent.KEY_REPEATED_TYPE &&
		    event.getType() != GSKeyEvent.KEY_RELEASED_TYPE) {
			
			// Only support pressed, repeat and released events.
			return false;
		}
		
		Key keyCode = keyBinding.getKeyCode();
		
		if (keyCode.getCategory() == Type.SCANCODE) {
			// Make sure the key event is actually an unknown key. This
			// ensures that we have to fall back to the scan code.
			if (event.getKeyCode() != GSKeyEvent.UNKNOWN_KEY)
				return false;
			
			return (event.getScanCode() == keyCode.getCode());
		}
		
		return (event.getKeyCode() == keyCode.getCode());
	}
}
