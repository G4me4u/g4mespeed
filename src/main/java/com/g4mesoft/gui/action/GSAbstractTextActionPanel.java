package com.g4mesoft.gui.action;

import com.g4mesoft.gui.renderer.GSIRenderer2D;

public abstract class GSAbstractTextActionPanel extends GSAbstractActionPanel {

	private static final int TEXT_COLOR          = 0xFFFCFCFC;
	private static final int HOVERED_TEXT_COLOR  = 0xFFFFFFA0;
	private static final int DISABLED_TEXT_COLOR = 0xFFA0A0A0;
	
	private String translationKey;
	private boolean literalText;
	
	private String displayText;

	protected GSAbstractTextActionPanel(String translationKey, boolean literalText, GSIActionListener listener) {
		super(listener);
	
		this.translationKey = translationKey;
		this.literalText = literalText;
		
		displayText = null;
	}
	
	@Override
	protected void onBoundsChanged() {
		super.onBoundsChanged();
		
		displayText = null;
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
		if (displayText == null) {
			String text = literalText ? translationKey : i18nTranslate(translationKey);
			displayText = renderer.trimString(text, width);
		}
		
		int color = enabled ? (hovered ? HOVERED_TEXT_COLOR : TEXT_COLOR) : DISABLED_TEXT_COLOR;
		
		int tx = width / 2;
		int ty = (height - renderer.getFontHeight() + 1) / 2;
		renderer.drawCenteredString(displayText, tx, ty, color, true);
	}
	
	public String getTranslationKey() {
		return translationKey;
	}

	public void setTranslationKey(String translationKey) {
		this.translationKey = translationKey;
		
		displayText = null;
	}

	public void setLiteralText(String text) {
		translationKey = text;
		literalText = true;
		
		displayText = null;
	}
	
	public boolean isLiteralText() {
		return literalText;
	}
}
