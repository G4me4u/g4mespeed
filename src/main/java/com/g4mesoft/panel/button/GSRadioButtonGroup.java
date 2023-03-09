package com.g4mesoft.panel.button;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.g4mesoft.panel.GSIActionListener;

public class GSRadioButtonGroup {

	private final Set<GSRadioButton> buttons;
	private final Map<GSRadioButton, GSIActionListener> actionListeners;

	public GSRadioButtonGroup() {
		buttons = Collections.newSetFromMap(new IdentityHashMap<>());
		actionListeners = new IdentityHashMap<>();
	}
	
	public void addRadioButton(GSRadioButton button) {
		if (buttons.add(button)) {
			GSIActionListener listener = new GSIActionListener() {
				@Override
				public void actionPerformed() {
					if (button.isSelected())
						onButtonSelected(button);
				}
			};
			
			button.addActionListener(listener);
			actionListeners.put(button, listener);
		}
	}

	public void removeRadioButton(GSRadioButton button) {
		if (buttons.remove(button)) {
			GSIActionListener listener = actionListeners.remove(button);
			if (listener != null)
				button.removeActionListener(listener);
		}
	}
	
	private void onButtonSelected(GSRadioButton button) {
		for (GSRadioButton b : buttons) {
			if (b != button)
				b.setSelected(false);
		}
	}
}
