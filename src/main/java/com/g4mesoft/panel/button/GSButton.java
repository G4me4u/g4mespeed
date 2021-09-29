package com.g4mesoft.panel.button;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSBiBorder;
import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSECursorType;
import com.g4mesoft.panel.GSEIconAlignment;
import com.g4mesoft.panel.GSETextAlignment;
import com.g4mesoft.panel.GSEmptyBorder;
import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.GSIBorder;
import com.g4mesoft.panel.GSIcon;
import com.g4mesoft.panel.GSLineBorder;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.event.GSIKeyListener;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;

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
 
	private static final GSIBorder INNER_BORDER            = new GSEmptyBorder(2);
	private static final GSIBorder DEFAULT_BORDER          = new GSBiBorder(INNER_BORDER, new GSLineBorder(0xFF171717, 1));
	private static final GSIBorder DEFAULT_HOVERED_BORDER  = new GSBiBorder(INNER_BORDER, new GSLineBorder(0xFF262626, 1));
	private static final GSIBorder DEFAULT_DISABLED_BORDER = new GSBiBorder(INNER_BORDER, new GSLineBorder(0xFF060606, 1));

	private static final int DEFAULT_ICON_SPACING = 2;
	private static final int VERTICAL_PADDING = 2;
	
	private GSIcon icon;
	private Text text;

	private GSIcon hoveredIcon;
	private GSIcon disabledIcon;

	private final List<GSIActionListener> actionListeners;

	private GSEIconAlignment iconAlignment;
	private GSETextAlignment textAlignment;
	
	private int hoveredBackgroundColor;
	
	private GSIBorder hoveredBorder;

	private int textColor;
	private int hoveredTextColor;
	private int disabledTextColor;
	
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
		
		setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
		hoveredBackgroundColor = DEFAULT_HOVERED_BACKGROUND_COLOR;
		setDisabledBackgroundColor(DEFAULT_DISABLED_BACKGROUND_COLOR);
		
		textColor = DEFAULT_TEXT_COLOR;
		hoveredTextColor = DEFAULT_HOVERED_TEXT_COLOR;
		disabledTextColor = DEFAULT_DISABLED_TEXT_COLOR;

		setBorder(DEFAULT_BORDER);
		hoveredBorder = DEFAULT_HOVERED_BORDER;
		setDisabledBorder(DEFAULT_DISABLED_BORDER);
		
		iconSpacing = DEFAULT_ICON_SPACING;
		
		clickSound = SoundEvents.UI_BUTTON_CLICK;
		
		addMouseEventListener(this);
		addKeyEventListener(this);
	}

	@Override
	protected GSIBor getVisibleBorder() {
		GSIBorder b;
		if (isEnabled()) {
			int rx = innerX - outerX, ry = innerY - outerY;
			GSIRenderer2D renderer = GSPanelContext.getRenderer();
			if (renderer.isMouseInside(rx, ry, innerWidth, innerHeight)) {
				b = hoveredBorder;
			} else {
				b = border;
			}
		} else {
			b = disabledBorder;
		}

		return (b != null) ? b : border;
	}
	
	@Override
	protected void renderBackground(GSIRenderer2D renderer, int x, int y, int width, int height) {
		int color;
		if (isEnabled()) {
			int rx = innerX - outerX, ry = innerY - outerY;
			if (renderer.isMouseInside(rx, ry, innerWidth, innerHeight)) {
				color = hoveredBackgroundColor;
			} else {
				color = backgroundColor;
			}
		} else {
			color = disabledBackgroundColor;
		}

		// Fallback to standard background color
		if (color == UNSPECIFIED_COLOR)
			color = backgroundColor;

		if ((color & 0xFF000000) != 0x00)
			renderer.fillRect(x, y, width, height, color);
	}

	@Override
	protected void renderForeground(GSIRenderer2D renderer) {
		GSIcon icn;
		int txtClr;
		if (isEnabled()) {
			if (renderer.isMouseInside(0, 0, innerWidth, innerHeight)) {
				icn = hoveredIcon;
				txtClr = hoveredTextColor;
			} else {
				icn = icon;
				txtClr = textColor;
			}
		} else {
			icn = disabledIcon;
			txtClr = disabledTextColor;
		}
		
		// Fallback to standard icon and text color
		if (icn == null)
			icn = icon;
		if (txtClr == UNSPECIFIED_COLOR)
			txtClr = textColor;
		
		GSPanelUtil.drawLabel(renderer, icn, iconSpacing, text, txtClr,
				isEnabled(), iconAlignment, textAlignment, 0, 0, innerWidth, innerHeight);
	}

	@Override
	public GSECursorType getCursor() {
		return isEnabled() ? super.getCursor() : GSECursorType.DEFAULT;
	}
	
	@Override
	protected GSDimension calculatePreferredInnerSize() {
		GSDimension labelSize = GSPanelUtil.labelPreferredSize(icon, text, iconSpacing);
		// Add button vertical padding
		int h = labelSize.getHeight() + VERTICAL_PADDING * 2;
		return new GSDimension(labelSize.getWidth(), h);
	}
	
	@Override
	public void mouseReleased(GSMouseEvent event) {
		if (isEnabled() && event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			int x = event.getX();
			int y = event.getY();

			// Ensure the mouse pointer is in bounds
			if (x >= 0 && y >= 0 && x < innerWidth && y < innerHeight) {
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
		setText(new LiteralText(text));
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
	
	public int getHoveredBackgroundColor() {
		return hoveredBackgroundColor;
	}
	
	public void setHoveredBackgroundColor(int hoveredBackgroundColor) {
		this.hoveredBackgroundColor = hoveredBackgroundColor;
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

	public int getIconSpacing() {
		return iconSpacing;
	}
	
	public void setIconSpacing(int iconSpacing) {
		this.iconSpacing = iconSpacing;
	}
	
	public SoundEvent getClickSound() {
		return clickSound;
	}

	public void setClickSound(SoundEvent clickSound) {
		this.clickSound = clickSound;
	}
}
