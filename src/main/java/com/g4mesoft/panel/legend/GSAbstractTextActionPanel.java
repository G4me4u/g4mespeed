package com.g4mesoft.panel.legend;

import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public abstract class GSAbstractTextActionPanel extends GSAbstractActionPanel {

	private static final int TEXT_COLOR          = 0xFFFCFCFC;
	private static final int DISABLED_TEXT_COLOR = 0xFFA0A0A0;
	
	private Text text;

	protected GSAbstractTextActionPanel(Text text, GSIActionListener listener) {
		super(listener);
	
		this.text = text;
	}
	
	@Override
	protected void renderBackground(GSIRenderer2D renderer, int x, int y, int width, int height) {
		// Handled by #renderBackground(renderer, hovered)
	}
	
	@Override
	protected void renderForeground(GSIRenderer2D renderer) {
		super.renderForeground(renderer);

		boolean hovered = renderer.isMouseInside(0, 0, innerWidth, innerHeight);
		renderBackground(renderer, hovered);
		renderForeground(renderer, hovered);
	}
	
	protected abstract void renderBackground(GSIRenderer2D renderer, boolean hovered);

	protected void renderForeground(GSIRenderer2D renderer, boolean hovered) {
		int color = isEnabled() ? TEXT_COLOR : DISABLED_TEXT_COLOR;
		
		int tx = innerWidth / 2;
		int ty = (innerHeight - renderer.getTextHeight() + 1) / 2;
		renderer.drawCenteredText(text, tx, ty, color, true);
	}
	
	public Text getText() {
		return text;
	}

	public void setText(String text) {
		if (text == null)
			throw new IllegalArgumentException("text is null!");

		setText(new LiteralText(text));
	}

	public void setText(Text text) {
		if (text == null)
			throw new IllegalArgumentException("text is null!");
		
		this.text = text;
	}
}
