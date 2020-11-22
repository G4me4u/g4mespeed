package com.g4mesoft.gui.text;

import com.g4mesoft.gui.GSPanel;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class GSTextLabel extends GSPanel {

	private static final int DEFAULT_BACKGROUND_COLOR = 0x00000000;
	private static final int DEFAULT_TEXT_COLOR       = 0xFFE0E0E0;
	
	private Text text;

	private int backgroundColor;
	private int textColor;
	private GSETextAlignment textAlignment;
	
	public GSTextLabel() {
		this("");
	}

	public GSTextLabel(String text) {
		this(new LiteralText(text));
	}

	public GSTextLabel(Text text) {
		if (text == null)
			throw new IllegalArgumentException("text is null");
		
		this.text = text;
		
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		textColor = DEFAULT_TEXT_COLOR;
		textAlignment = GSETextAlignment.LEFT;;
	}

	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		drawBackground(renderer);
		drawForeground(renderer);
	}
	
	private void drawBackground(GSIRenderer2D renderer) {
		if (((backgroundColor >>> 24) & 0xFF) != 0x00)
			renderer.fillRect(0, 0, width, height, backgroundColor);
	}

	private void drawForeground(GSIRenderer2D renderer) {
		int tx = 0;
		switch (textAlignment) {
		case RIGHT:
			tx += width - renderer.getTextWidth(text);
		case CENTER:
			tx += (width - renderer.getTextWidth(text)) / 2;
		case LEFT:
		default:
			break;
		}
		
		int ty = (height - renderer.getTextHeight()) / 2;
		renderer.drawText(text, tx, ty, textColor);
	}
	
	public void setText(String text) {
		setText(new LiteralText(text));
	}
	
	public void setText(Text text) {
		if (text == null)
			throw new IllegalArgumentException("text is null");
		
		this.text = text;
	}

	public Text getText() {
		return text;
	}
	
	public int getBackgroundColor() {
		return backgroundColor;
	}
	
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public int getTextColor() {
		return textColor;
	}
	
	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public GSETextAlignment getTextAlignment() {
		return textAlignment;
	}

	public void setTextAlignment(GSETextAlignment textAlignment) {
		if (textAlignment == null)
			throw new IllegalArgumentException("textAlignment is null!");
		
		this.textAlignment = textAlignment;
	}
}
