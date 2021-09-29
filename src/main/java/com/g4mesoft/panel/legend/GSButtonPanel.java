package com.g4mesoft.panel.legend;

import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.event.GSIKeyListener;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.renderer.GSTexture;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GSButtonPanel extends GSAbstractTextActionPanel implements GSIKeyListener {

	private static final Identifier TEXTURE_IDENTIFIER = new Identifier("textures/gui/widgets.png");
	private static final GSTexture BUTTON_TEXTURE = new GSTexture(TEXTURE_IDENTIFIER, 256, 256);
	
	public static final int BUTTON_HEIGHT = 20;

	public GSButtonPanel(String text, GSIActionListener listener) {
		this(new LiteralText(text), listener);
	}
	
	public GSButtonPanel(Text text, GSIActionListener listener) {
		super(text, listener);
	
		addKeyEventListener(this);
	}
	
	public void setPreferredBounds(int x, int y, int width) {
		setOuterBounds(x, y, width, BUTTON_HEIGHT);
	}

	@Override
	protected void renderBackground(GSIRenderer2D renderer, boolean hovered) {
		// Taken from AbstractButtonWidget#renderButton
		int sy = isEnabled() ? (hovered ? 86 : 66) : 46;

		renderer.drawTexture(BUTTON_TEXTURE, 0, 0, innerWidth / 2, innerHeight, 0, sy);
		renderer.drawTexture(BUTTON_TEXTURE, innerWidth / 2, 0, innerWidth / 2, innerHeight, 200 - innerWidth / 2, sy);
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
