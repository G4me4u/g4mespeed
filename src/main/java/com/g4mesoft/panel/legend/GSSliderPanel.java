package com.g4mesoft.panel.legend;

import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.renderer.GSTexture;
import com.g4mesoft.util.GSMathUtil;

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
		renderer.drawTexture(SLIDER_TEXTURE, 0, 0, innerWidth / 2, innerHeight, 0, 46);
		renderer.drawTexture(SLIDER_TEXTURE, innerWidth / 2, 0, innerWidth / 2, innerHeight, 200 - innerWidth / 2, 46);

		int vx = Math.round(value * (innerWidth - 8));
		int sy = (isEnabled() && renderer.isMouseInside(0, 0, innerWidth, innerHeight)) ? 86 : 66;
		renderer.drawTexture(SLIDER_TEXTURE, vx    , 0, 4, innerHeight,   0, sy);
		renderer.drawTexture(SLIDER_TEXTURE, vx + 4, 0, 4, innerHeight, 196, sy);
	}

	@Override
	protected void onClicked(int mouseX, int mouseY) {
		setValue((float)(mouseX - 4) / (innerWidth - 8));
		dispatchActionPerformedEvent();
	}
	
	@Override
	public void mouseDragged(GSMouseEvent event) {
		super.mouseDragged(event);
		
		if (isEnabled() && !event.isConsumed() && event.getButton() == GSMouseEvent.BUTTON_LEFT) {
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
		this.value = GSMathUtil.clamp(value, 0.0f, 1.0f);
	}
}
