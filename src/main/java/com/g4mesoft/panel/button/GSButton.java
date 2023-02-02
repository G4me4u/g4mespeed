package com.g4mesoft.panel.button;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSECursorType;
import com.g4mesoft.panel.GSEIconAlignment;
import com.g4mesoft.panel.GSETextAlignment;
import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.GSIcon;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.event.GSIKeyListener;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class GSButton extends GSPanel implements GSIMouseListener, GSIKeyListener {

	private static final int DEFAULT_BACKGROUND_COLOR = 0xFF202020;
	private static final int DEFAULT_HOVERED_BACKGROUND_COLOR = 0xFF2E2E2E;
	private static final int DEFAULT_DISABLED_BACKGROUND_COLOR = 0xFF0A0A0A;
	
	private static final int DEFAULT_TEXT_COLOR = 0xFFE0E0E0;
	private static final int DEFAULT_HOVERED_TEXT_COLOR = 0xFFFFFFFF;
	private static final int DEFAULT_DISABLED_TEXT_COLOR = 0xFF707070;
 
	private static final int DEFAULT_BORDER_WIDTH = 1;
	private static final int DEFAULT_BORDER_COLOR = 0xFF171717;
	private static final int DEFAULT_HOVERED_BORDER_COLOR = 0xFF262626;
	private static final int DEFAULT_DISABLED_BORDER_COLOR = 0xFF060606;
	
	private static final int DEFAULT_VERTICAL_MARGIN   = 2;
	private static final int DEFAULT_HORIZONTAL_MARGIN = 2;

	private static final int DEFAULT_ICON_SPACING = 2;
	private static final int VERTICAL_PADDING = 2;
	
	private GSIcon icon;
	private Text text;

	private GSIcon hoveredIcon;
	private GSIcon disabledIcon;

	private final List<GSIActionListener> actionListeners;

	private GSEIconAlignment iconAlignment;
	private GSETextAlignment textAlignment;
	
	private int backgroundColor;
	private int hoveredBackgroundColor;
	private int disabledBackgroundColor;

	private int textColor;
	private int hoveredTextColor;
	private int disabledTextColor;
	
	private int borderWidth;
	private int borderColor;
	private int hoveredBorderColor;
	private int disabledBorderColor;

	private int verticalMargin;
	private int horizontalMargin;
	
	private int iconSpacing;
	
	private SoundEvent clickSound;
	
	public GSButton(String text) {
		this(null, text);
	}

	public GSButton(GSIcon icon, String text) {
		this(icon, new LiteralText(text));
	}

	public GSButton(Text text) {
		this(null, text);
	}

	public GSButton(GSIcon icon) {
		this(icon, (Text)null);
	}
	
	public GSButton(GSIcon icon, Text text) {
		this.icon = icon;
		this.text = text;
		
		disabledIcon = icon;
		hoveredIcon = icon;

		actionListeners = new ArrayList<>();
	
		iconAlignment = GSEIconAlignment.LEFT;
		textAlignment = GSETextAlignment.CENTER;
		
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		hoveredBackgroundColor = DEFAULT_HOVERED_BACKGROUND_COLOR;
		disabledBackgroundColor = DEFAULT_DISABLED_BACKGROUND_COLOR;
		
		textColor = DEFAULT_TEXT_COLOR;
		hoveredTextColor = DEFAULT_HOVERED_TEXT_COLOR;
		disabledTextColor = DEFAULT_DISABLED_TEXT_COLOR;

		borderWidth = DEFAULT_BORDER_WIDTH;
		borderColor = DEFAULT_BORDER_COLOR;
		hoveredBorderColor = DEFAULT_HOVERED_BORDER_COLOR;
		disabledBorderColor = DEFAULT_DISABLED_BORDER_COLOR;
		
		verticalMargin = DEFAULT_VERTICAL_MARGIN;
		horizontalMargin = DEFAULT_HORIZONTAL_MARGIN;
		
		iconSpacing = DEFAULT_ICON_SPACING;
		
		clickSound = SoundEvents.UI_BUTTON_CLICK;
		
		addMouseEventListener(this);
		addKeyEventListener(this);
	}

	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		boolean hovered = renderer.isMouseInside(0, 0, width, height);

		drawBorderAndBackground(renderer, hovered);
		drawForeground(renderer, hovered);
	}
	
	protected void drawBorderAndBackground(GSIRenderer2D renderer, boolean hovered) {
		int bgc = isEnabled() ? (hovered ? hoveredBackgroundColor : backgroundColor) : disabledBackgroundColor;
		int bc  = isEnabled() ? (hovered ? hoveredBorderColor : borderColor) : disabledBorderColor;

		int bw2 = borderWidth * 2;
		if (borderWidth != 0) {
			// Top, Bottom, Left, Right
			renderer.fillRect(0, 0, width - borderWidth, borderWidth, bc);
			renderer.fillRect(borderWidth, height - borderWidth, width - borderWidth, borderWidth, bc);
			renderer.fillRect(0, borderWidth, borderWidth, height - borderWidth, bc);
			renderer.fillRect(width - borderWidth, 0, borderWidth, height - borderWidth, bc);
		}
		
		if (GSColorUtil.unpackA(bgc) != 0x00)
			renderer.fillRect(borderWidth, borderWidth, width - bw2, height - bw2, bgc);
	}

	protected void drawForeground(GSIRenderer2D renderer, boolean hovered) {
		// Available bounds for drawing text and icon.
		int x = borderWidth + horizontalMargin;
		int y = borderWidth + verticalMargin;
		int w = Math.max(0, width - 2 * x);
		int h = Math.max(0, height - 2 * y);
		
		GSIcon icn = isEnabled() ? (hovered ? hoveredIcon : icon) : disabledIcon;
		int txtClr = isEnabled() ? (hovered ? hoveredTextColor : textColor) : disabledTextColor;
		
		GSPanelUtil.drawLabel(renderer, icn, iconSpacing, text,
				txtClr, isEnabled(), iconAlignment, textAlignment, x, y, w, h);
	}

	@Override
	public GSECursorType getCursor() {
		return isEnabled() ? super.getCursor() : GSECursorType.DEFAULT;
	}
	
	@Override
	protected GSDimension calculatePreferredSize() {
		GSDimension labelSize = GSPanelUtil.labelPreferredSize(icon, text, iconSpacing);
		
		// Add borders, margin, and padding
		int w = labelSize.getWidth() + (borderWidth + horizontalMargin) * 2;
		int h = labelSize.getHeight() + (borderWidth + verticalMargin + VERTICAL_PADDING) * 2;
	
		return new GSDimension(w, h);
	}
	
	@Override
	public void mouseReleased(GSMouseEvent event) {
		if (isEnabled() && event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			int x = event.getX();
			int y = event.getY();

			// Ensure the mouse pointer is in bounds
			if (x >= 0 && y >= 0 && x < width && y < height) {
				dispatchActionEvent();
				playClickSound();
				event.consume();
			}
		}
	}
	
	@Override
	public void keyPressed(GSKeyEvent event) {
		if (isEnabled() && !event.isRepeating()) {
			switch (event.getKeyCode()) {
			case GSKeyEvent.KEY_ENTER:
			case GSKeyEvent.KEY_KP_ENTER:
			case GSKeyEvent.KEY_SPACE:
				dispatchActionEvent();
				playClickSound();
				event.consume();
				break;
			}
		}
	}
	
	protected void playClickSound() {
		if (clickSound != null)
			GSPanelContext.playSound(PositionedSoundInstance.master(clickSound, 1.0F));
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
		setText((text != null) ? new LiteralText(text) : null);
	}
	
	public void setText(Text text) {
		this.text = text;
	}

	public GSIcon getHoveredIcon() {
		return hoveredIcon;
	}

	public void setHoveredIcon(GSIcon hoveredIcon) {
		this.hoveredIcon = hoveredIcon;
	}
	
	public GSIcon getDisabledIcon() {
		return disabledIcon;
	}
	
	public void setDisabledIcon(GSIcon disabledIcon) {
		this.disabledIcon = disabledIcon;
	}
	
	public void addActionListener(GSIActionListener listener) {
		actionListeners.add(listener);
	}

	public void removeActionListener(GSIActionListener listener) {
		actionListeners.remove(listener);
	}
	
	private void dispatchActionEvent() {
		actionListeners.forEach(GSIActionListener::actionPerformed);
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
	
	public int getBackgroundColor() {
		return backgroundColor;
	}
	
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getHoveredBackgroundColor() {
		return hoveredBackgroundColor;
	}
	
	public void setHoveredBackgroundColor(int hoveredBackgroundColor) {
		this.hoveredBackgroundColor = hoveredBackgroundColor;
	}

	public int getDisabledBackgroundColor() {
		return disabledBackgroundColor;
	}
	
	public void setDisabledBackgroundColor(int disabledBackgroundColor) {
		this.disabledBackgroundColor = disabledBackgroundColor;
	}

	public int getTextColor() {
		return textColor;
	}
	
	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}
	
	public int getHoveredTextColor() {
		return hoveredTextColor;
	}
	
	public void setHoveredTextColor(int hoveredTextColor) {
		this.hoveredTextColor = hoveredTextColor;
	}
	
	public int getDisabledTextColor() {
		return disabledTextColor;
	}
	
	public void setDisabledTextColor(int disabledTextColor) {
		this.disabledTextColor = disabledTextColor;
	}

	public int getBorderWidth() {
		return borderWidth;
	}
	
	public void setBorderWidth(int borderWidth) {
		if (borderWidth < 0)
			throw new IllegalArgumentException("borderWidth must be non-negative!");
		this.borderWidth = borderWidth;
	}
	
	public int getBorderColor() {
		return borderColor;
	}
	
	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
	}
	
	public int getHoveredBorderColor() {
		return hoveredBorderColor;
	}
	
	public void setHoveredBorderColor(int hoveredBorderColor) {
		this.hoveredBorderColor = hoveredBorderColor;
	}
	
	public int getDisabledBorderColor() {
		return disabledBorderColor;
	}
	
	public void setDisabledBorderColor(int disabledBorderColor) {
		this.disabledBorderColor = disabledBorderColor;
	}
	
	public int getVerticalMargin() {
		return verticalMargin;
	}

	public void setVerticalMargin(int verticalMargin) {
		if (verticalMargin < 0)
			throw new IllegalArgumentException("verticalMargin must be non-negative!");
		this.verticalMargin = verticalMargin;
	}

	public int getHorizontalMargin() {
		return horizontalMargin;
	}
	
	public void setHorizontalMargin(int horizontalMargin) {
		if (horizontalMargin < 0)
			throw new IllegalArgumentException("horizontalMargin must be non-negative!");
		this.horizontalMargin = horizontalMargin;
	}

	public int getIconSpacing() {
		return iconSpacing;
	}
	
	public void setIconSpacing(int iconSpacing) {
		if (iconSpacing < 0)
			throw new IllegalArgumentException("iconSpacing must be non-negative!");
		this.iconSpacing = iconSpacing;
	}
	
	public SoundEvent getClickSound() {
		return clickSound;
	}

	public void setClickSound(SoundEvent clickSound) {
		this.clickSound = clickSound;
	}
}
