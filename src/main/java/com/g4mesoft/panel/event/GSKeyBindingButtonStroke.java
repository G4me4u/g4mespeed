package com.g4mesoft.panel.event;

import com.g4mesoft.hotkey.GSKeyBinding;

import net.minecraft.client.util.InputUtil;

public class GSKeyBindingButtonStroke implements GSIButtonStroke {

	private final GSKeyBinding keyBinding;
	
	public GSKeyBindingButtonStroke(GSKeyBinding keyBinding) {
		if (keyBinding == null)
			throw new IllegalArgumentException("keyBinding is null!");
		
		this.keyBinding = keyBinding;
	}
	
	@Override
	public boolean isMatching(GSEvent event) {
		return keyBinding.isPressed() && isEventMatching(event);
	}

	private boolean isEventMatching(GSEvent event) {
		if (event instanceof GSMouseEvent) {
			return isMouseEventMatching((GSMouseEvent)event);
		} else if (event instanceof GSKeyEvent) {
			return isKeyEventMatching((GSKeyEvent)event);
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
		
		return isKeyMatching(InputUtil.Type.MOUSE.createFromCode(event.getButton()));
	}
	
	private boolean isKeyEventMatching(GSKeyEvent event) {
		if (event.getType() != GSKeyEvent.KEY_PRESSED_TYPE &&
		    event.getType() != GSKeyEvent.KEY_REPEATED_TYPE &&
		    event.getType() != GSKeyEvent.KEY_RELEASED_TYPE) {
			
			// Only support pressed, repeat and released events.
			return false;
		}

		return isKeyMatching(InputUtil.getKeyCode(event.getKeyCode(),
		                                          event.getScanCode()));
	}
	
	private boolean isKeyMatching(InputUtil.KeyCode key) {
		return (keyBinding.getKeyCode().indexOf(key) != -1);
	}
}
