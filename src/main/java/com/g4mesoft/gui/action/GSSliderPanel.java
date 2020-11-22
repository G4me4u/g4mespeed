package com.g4mesoft.gui.action;

import com.g4mesoft.gui.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.renderer.GSTexture;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GSSliderPanel extends GSAbstractTextActionPanel {

	private static final Identifier TEXTURE_IDENTIFIER = new Identifier("textures/gui/widgets.png");
	private static final GSTexture SLIDER_TEXTURE = new GSTexture(TEXTURE_IDENTIFIER, 256, 256);
	
	public static final int SLIDER_HEIGHT = 20;
	public static final int MAX_WIDTH = 200;
	
	private float value;
	
	public GSSliderPanel(String text, GSIActionListener listener) {
		this(new LiteralText(text), listener);
	}

	public GSSliderPanel(Text text, GSIActionListener listener) {
		super(text, listener);
		
		this.value = 0.0f;
	}

	@Override
	protected void renderBackground(GSIRenderer2D renderer, boolean hovered) {
		renderer.drawTexture(SLIDER_TEXTURE, 0, 0, width / 2, height, 0, 46);
		renderer.drawTexture(SLIDER_TEXTURE, width / 2, 0, width / 2, height, 200 - width / 2, 46);

		int vx = Math.round(value * (width - 8));
		int sy = (isEnabled() && renderer.isMouseInside(0, 0, width, height)) ? 86 : 66;
		renderer.drawTexture(SLIDER_TEXTURE, vx    , 0, 4, height,   0, sy);
		renderer.drawTexture(SLIDER_TEXTURE, vx + 4, 0, 4, height, 196, sy);
	}

	@Override
	protected void onClicked(int mouseX, int mouseY) {
		setValue((float)(mouseX - 4) / (width - 8));
		dispatchActionPerformedEvent();
	}
	
	@Override
	public void mouseDragged(GSMouseEvent event) {
		super.mouseDragged(event);
		
		if (isEnabled() && !event.isConsumed()) {
			onClicked(event.getX(), event.getY());
			event.consume();
		}
	}
	
	@Override
	public void mouseReleased(GSMouseEvent event) {
		super.mouseReleased(event);

		if (isEnabled() && !event.isConsumed()) {
			playClickSound();
			event.consume();
		}
	}
	
	public float getValue() {
		return value;
	}
	
	public void setValue(float value) {
		this.value = GSMathUtils.clamp(value, 0.0f, 1.0f);
	}
}
