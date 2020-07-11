package com.g4mesoft.gui.action;

import com.g4mesoft.gui.event.GSIKeyListener;
import com.g4mesoft.gui.event.GSKeyEvent;
import com.g4mesoft.gui.renderer.GSIRenderer2D;
import com.g4mesoft.gui.renderer.GSTexture;

import net.minecraft.util.Identifier;

public class GSButtonPanel extends GSAbstractTextActionPanel implements GSIKeyListener {

	private static final Identifier TEXTURE_IDENTIFIER = new Identifier("textures/gui/widgets.png");
	private static final GSTexture BUTTON_TEXTURE = new GSTexture(TEXTURE_IDENTIFIER, 256, 256);
	
	public static final int BUTTON_HEIGHT = 20;
	
	public GSButtonPanel(String translationKey, GSIActionListener listener) {
		this(translationKey, false, listener);
	}

	public GSButtonPanel(String translationKey, boolean literalText, GSIActionListener listener) {
		super(translationKey, literalText, listener);
	
		addKeyEventListener(this);
	}
	
	public void setPreferredBounds(int x, int y, int width) {
		setBounds(x, y, width, BUTTON_HEIGHT);
	}

	@Override
	protected void renderBackground(GSIRenderer2D renderer, boolean hovered) {
		// Taken from AbstractButtonWidget#renderButton
		int sy = enabled ? (hovered ? 86 : 66) : 46;

		renderer.drawTexture(BUTTON_TEXTURE, 0, 0, width / 2, height, 0, sy);
		renderer.drawTexture(BUTTON_TEXTURE, width / 2, 0, width / 2, height, 200 - width / 2, sy);
	}

	@Override
	protected void onClicked(int mouseX, int mouseY) {
		dispatchActionPerformedEvent();
		playClickSound();
	}
	
	@Override
	public void keyPressed(GSKeyEvent event) {
		if (isEnabled() && !event.isRepeating()) {
			switch (event.getKeyCode()) {
			case GSKeyEvent.KEY_ENTER:
			case GSKeyEvent.KEY_KP_ENTER:
			case GSKeyEvent.KEY_SPACE:
				dispatchActionPerformedEvent();
				playClickSound();
				event.consume();
				break;
			}
		}
	}
}
