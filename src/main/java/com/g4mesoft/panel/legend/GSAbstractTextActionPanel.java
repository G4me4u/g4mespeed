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
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		boolean hovered = renderer.isMouseInside(0, 0, width, height);
		renderBackground(renderer, hovered);
		renderForeground(renderer, hovered);
	}
	
	protected abstract void renderBackground(GSIRenderer2D renderer, boolean hovered);

	protected void renderForeground(GSIRenderer2D renderer, boolean hovered) {
		int color = enabled ? TEXT_COLOR : DISABLED_TEXT_COLOR;
		
		int tx = width / 2;
		int ty = (height - renderer.getTextHeight() + 1) / 2;
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
