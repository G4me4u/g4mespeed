package com.g4mesoft.gui.action;

import com.g4mesoft.gui.GSElementContext;
import com.g4mesoft.gui.GSPanel;
import com.g4mesoft.gui.event.GSIMouseListener;
import com.g4mesoft.gui.event.GSMouseEvent;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public abstract class GSAbstractActionPanel extends GSPanel implements GSIMouseListener {

	private final GSIActionListener listener;

	protected boolean enabled;
	
	public GSAbstractActionPanel(GSIActionListener listener) {
		this.listener = listener;
	
		enabled = true;
		
		addMouseEventListener(this);
	}
	
	protected abstract void onClicked(int mouseX, int mouseY);

	protected void playClickSound() {
		GSElementContext.playSound(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
	
	@Override
	public void mousePressed(GSMouseEvent event) {
		if (isEnabled() && event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			onClicked(event.getX(), event.getY());
			event.consume();
		}
	}

	protected void dispatchActionPerformedEvent() {
		if (listener != null)
			listener.actionPerformed();
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
