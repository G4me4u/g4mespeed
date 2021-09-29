package com.g4mesoft.panel.field;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSEIconAlignment;
import com.g4mesoft.panel.GSETextAlignment;
import com.g4mesoft.panel.GSIcon;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class GSTextLabel extends GSPanel {

	private static final int DEFAULT_BACKGROUND_COLOR = 0x00000000;
	private static final int DEFAULT_TEXT_COLOR = 0xFFE0E0E0;
	private static final int DEFAULT_ICON_SPACING = 2;
	
	private GSIcon icon;
	private Text text;

	private GSEIconAlignment iconAlignment;
	private GSETextAlignment textAlignment;
	
	private int textColor;
	private int iconSpacing;
	
	public GSTextLabel(String text) {
		this(null, text);
	}

	public GSTextLabel(GSIcon icon, String text) {
		this(icon, new LiteralText(text));
	}

	public GSTextLabel(Text text) {
		this(null, text);
	}

	public GSTextLabel(GSIcon icon) {
		this(icon, (Text)null);
	}
	
	public GSTextLabel(GSIcon icon, Text text) {
		this.icon = icon;
		this.text = text;
		
		iconAlignment = GSEIconAlignment.LEFT;
		textAlignment = GSETextAlignment.CENTER;
		
		setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
		textColor = DEFAULT_TEXT_COLOR;
		iconSpacing = DEFAULT_ICON_SPACING;
	}
	
	@Override
	protected void renderForeground(GSIRenderer2D renderer) {
		super.renderForeground(renderer);

		GSPanelUtil.drawLabel(renderer, icon, iconSpacing, text,
				textColor, true, iconAlignment, textAlignment, 0, 0, innerWidth, innerHeight);
	}
	
	@Override
	protected GSDimension calculatePreferredInnerSize() {
		return GSPanelUtil.labelPreferredSize(icon, text, DEFAULT_ICON_SPACING);
	}

	public GSIcon getIcon() {
		return icon;
	}
	
	public void setIcon(GSIcon icon) {
		this.icon = icon;
	}
	
	public Text getText() {
		return text;
	}

	public void setText(String text) {
		setText(new LiteralText(text));
	}
	
	public void setText(Text text) {
		this.text = text;
	}

	public GSEIconAlignment getIconAlignment() {
		return iconAlignment;
	}
	
	public void setIconAlignment(GSEIconAlignment iconAlignment) {
		if (iconAlignment == null)
			throw new IllegalArgumentException("iconAlignment is null");
		this.iconAlignment = iconAlignment;
	}

	public GSETextAlignment getTextAlignment() {
		return textAlignment;
	}
	
	public void setTextAlignment(GSETextAlignment textAlignment) {
		if (textAlignment == null)
			throw new IllegalArgumentException("textAlignment is null");
		this.textAlignment = textAlignment;
	}
	
	public int getTextColor() {
		return textColor;
	}
	
	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public int getIconSpacing() {
		return iconSpacing;
	}
	
	public void setIconSpacing(int iconSpacing) {
		this.iconSpacing = iconSpacing;
	}
}
