package com.g4mesoft.panel.legend;

import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.event.GSIKeyListener;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.renderer.GSITextureRegion;

public class GSToggleSwitchPanel extends GSAbstractActionPanel implements GSIKeyListener {

	private static final GSITextureRegion SWITCH_TEXTURE = GSPanelContext.getTexture(0, 0, 90, 32);

	public static final int SWITCH_WIDTH = 30;
	public static final int SWITCH_HEIGHT = 16;
	
	private boolean toggled;

	public GSToggleSwitchPanel(GSIActionListener listener) {
		this(listener, false);
	}
	
	public GSToggleSwitchPanel(GSIActionListener listener, boolean toggled) {
		super(listener);
		
		this.toggled = toggled;
	
		addKeyEventListener(this);
	}

	public void setPreferredBounds(int x, int y) {
		setBounds(x, y, SWITCH_WIDTH, SWITCH_HEIGHT);
	}

	/**
	 * @see #setPreferredBounds(int, int)
	 */
	@Deprecated
	public final void setBounds(int x, int y, int width, int height) {
		super.setOuterBounds(x, y, width, height);
	}
	
	@Override
	protected void renderForeground(GSIRenderer2D renderer) {
		super.renderForeground(renderer);
		
		int sx = isEnabled() ? (renderer.isMouseInside(0, 0, innerWidth, innerHeight) ? 30 : 0) : 60;
		int sy = toggled ? 16 : 0;
		renderer.drawTexture(SWITCH_TEXTURE, 0, 0, SWITCH_WIDTH, SWITCH_HEIGHT, sx, sy);
	}

	@Override
	protected void onClicked(int mouseX, int mouseY) {
		toggle();
	}
	
	@Override
	public void keyPressed(GSKeyEvent event) {
		if (isEnabled() && !event.isRepeating()) {
			boolean shouldToggle = false;
			
			switch (event.getKeyCode()) {
			case GSKeyEvent.KEY_ENTER:
			case GSKeyEvent.KEY_KP_ENTER:
			case GSKeyEvent.KEY_SPACE:
				shouldToggle = true;
				break;
			case GSKeyEvent.KEY_LEFT:
				shouldToggle = isToggled();
				break;
			case GSKeyEvent.KEY_RIGHT:
				shouldToggle = !isToggled();
				break;
			}
			
			if (shouldToggle) {
				toggle();
				event.consume();
			}
		}
	}
	
	public void toggle() {
		setToggled(!toggled);
		playClickSound();
		dispatchActionPerformedEvent();
	}
	
	public boolean isToggled() {
		return this.toggled;
	}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
	}
}
