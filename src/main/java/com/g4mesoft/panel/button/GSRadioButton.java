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
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.event.GSIKeyListener;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class GSRadioButton extends GSPanel implements GSIMouseListener, GSIKeyListener {

	private static final int DEFAULT_ICON_COLOR = 0xFFCACACA;
	private static final int DEFAULT_HOVERED_ICON_COLOR = 0xFFF0F3F5;
	private static final int DEFAULT_DISABLED_ICON_COLOR = 0xFF676768;
	
	private static final int DEFAULT_TEXT_COLOR = 0xFFCCCCCC;
	private static final int DEFAULT_HOVERED_TEXT_COLOR = 0xFFF3F6F8;
	private static final int DEFAULT_DISABLED_TEXT_COLOR = 0xFF686869;
	
	private static final int DEFAULT_ICON_SIZE = 6;
	
	private static final int DEFAULT_VERTICAL_MARGIN   = 2;
	private static final int DEFAULT_HORIZONTAL_MARGIN = 2;
	
	private static final int DEFAULT_ICON_SPACING = 2;
	
	private Text text;

	private GSIcon icon;
	private GSIcon hoveredIcon;
	private GSIcon disabledIcon;
	
	private GSIcon selectedIcon;
	private GSIcon hoveredSelectedIcon;
	private GSIcon disabledSelectedIcon;

	private final List<GSIActionListener> actionListeners;

	private GSEIconAlignment iconAlignment;
	private GSETextAlignment textAlignment;
	
	private int textColor;
	private int hoveredTextColor;
	private int disabledTextColor;
	
	private int verticalMargin;
	private int horizontalMargin;
	
	private int iconSpacing;
	
	private boolean selected;

	public GSRadioButton(Text text) {
		this.text = text;

		icon = new GSUnselectedIcon(DEFAULT_ICON_COLOR, DEFAULT_ICON_SIZE);
		hoveredIcon = new GSUnselectedIcon(DEFAULT_HOVERED_ICON_COLOR, DEFAULT_ICON_SIZE);
		disabledIcon = new GSUnselectedIcon(DEFAULT_DISABLED_ICON_COLOR, DEFAULT_ICON_SIZE);
		
		selectedIcon = new GSSelectedIcon(DEFAULT_ICON_COLOR, DEFAULT_ICON_SIZE);
		hoveredSelectedIcon = new GSSelectedIcon(DEFAULT_HOVERED_ICON_COLOR, DEFAULT_ICON_SIZE);
		disabledSelectedIcon = new GSSelectedIcon(DEFAULT_DISABLED_ICON_COLOR, DEFAULT_ICON_SIZE);
		
		actionListeners = new ArrayList<>();

		iconAlignment = GSEIconAlignment.LEFT;
		textAlignment = GSETextAlignment.CENTER;
		
		textColor = DEFAULT_TEXT_COLOR;
		hoveredTextColor = DEFAULT_HOVERED_TEXT_COLOR;
		disabledTextColor = DEFAULT_DISABLED_TEXT_COLOR;

		verticalMargin = DEFAULT_VERTICAL_MARGIN;
		horizontalMargin = DEFAULT_HORIZONTAL_MARGIN;
		
		iconSpacing = DEFAULT_ICON_SPACING;
		
		selected = false;
		
		setCursor(GSECursorType.HAND);
		
		addMouseEventListener(this);
		addKeyEventListener(this);
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);
		
		renderForeground(renderer, renderer.isMouseInside(0, 0, width, height));
	}
	
	protected void renderForeground(GSIRenderer2D renderer, boolean hovered) {
		// Available bounds for drawing text and icon.
		int x = horizontalMargin;
		int y = verticalMargin;
		int w = Math.max(0, width - 2 * x);
		int h = Math.max(0, height - 2 * y);
		
		GSIcon icn;
		if (isSelected()) {
			icn = isEnabled() ? (hovered ? hoveredSelectedIcon : selectedIcon) : disabledSelectedIcon;
		} else {
			icn = isEnabled() ? (hovered ? hoveredIcon : icon) : disabledIcon;
		}
		
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
		
		int w = labelSize.getWidth() + horizontalMargin * 2;
		int h = labelSize.getHeight() + verticalMargin * 2;
	
		return new GSDimension(w, h);
	}
	
	@Override
	public void mouseReleased(GSMouseEvent event) {
		if (isEnabled() && !isSelected() && event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			int x = event.getX();
			int y = event.getY();

			// Ensure the mouse pointer is in bounds
			if (x >= 0 && y >= 0 && x < width && y < height) {
				setSelected(true);
				dispatchActionPerformed();
				event.consume();
			}
		}
	}
	
	@Override
	public void keyPressed(GSKeyEvent event) {
		if (isEnabled() && !isSelected() && !event.isRepeating()) {
			switch (event.getKeyCode()) {
			case GSKeyEvent.KEY_ENTER:
			case GSKeyEvent.KEY_KP_ENTER:
			case GSKeyEvent.KEY_SPACE:
				setSelected(true);
				dispatchActionPerformed();
				event.consume();
				break;
			}
		}
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

	public GSIcon getIcon() {
		return icon;
	}
	
	public void setIcon(GSIcon icon) {
		if (icon == null)
			throw new IllegalArgumentException("icon is null");
		this.icon = icon;
	}
	
	public GSIcon getHoveredIcon() {
		return hoveredIcon;
	}

	public void setHoveredIcon(GSIcon hoveredIcon) {
		if (hoveredIcon == null)
			throw new IllegalArgumentException("hoveredIcon is null");
		this.hoveredIcon = hoveredIcon;
	}
	
	public GSIcon getDisabledIcon() {
		return disabledIcon;
	}
	
	public void setDisabledIcon(GSIcon disabledIcon) {
		if (disabledIcon == null)
			throw new IllegalArgumentException("disabledIcon is null");
		this.disabledIcon = disabledIcon;
	}
	
	public GSIcon getSelectedIcon() {
		return selectedIcon;
	}
	
	public void setSelectedIcon(GSIcon selectedIcon) {
		if (selectedIcon == null)
			throw new IllegalArgumentException("selectedIcon is null");
		this.selectedIcon = selectedIcon;
	}
	
	public GSIcon getHoveredSelectedIcon() {
		return hoveredSelectedIcon;
	}
	
	public void setHoveredSelectedIcon(GSIcon hoveredSelectedIcon) {
		if (hoveredSelectedIcon == null)
			throw new IllegalArgumentException("hoveredSelectedIcon is null");
		this.hoveredSelectedIcon = hoveredSelectedIcon;
	}
	
	public GSIcon getDisabledSelectedIcon() {
		return disabledSelectedIcon;
	}
	
	public void setDisabledSelectedIcon(GSIcon disabledSelectedIcon) {
		if (disabledSelectedIcon == null)
			throw new IllegalArgumentException("disabledSelectedIcon is null");
		this.disabledSelectedIcon = disabledSelectedIcon;
	}
	
	public void addActionListener(GSIActionListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null");
		actionListeners.add(listener);
	}

	public void removeActionListener(GSIActionListener listener) {
		actionListeners.remove(listener);
	}
	
	private void dispatchActionPerformed() {
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

	public int getVerticalMargin() {
		return verticalMargin;
	}

	public void setVerticalMargin(int verticalMargin) {
		this.verticalMargin = verticalMargin;
	}

	public int getHorizontalMargin() {
		return horizontalMargin;
	}
	
	public void setHorizontalMargin(int horizontalMargin) {
		this.horizontalMargin = horizontalMargin;
	}

	public int getIconSpacing() {
		return iconSpacing;
	}
	
	public void setIconSpacing(int iconSpacing) {
		this.iconSpacing = iconSpacing;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
