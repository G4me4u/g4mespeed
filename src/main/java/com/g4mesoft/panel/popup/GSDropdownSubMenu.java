package com.g4mesoft.panel.popup;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSECursorType;
import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.GSIcon;
import com.g4mesoft.panel.GSLocation;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.renderer.GSTexture;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GSDropdownSubMenu extends GSDropdownItem {

	private static final Identifier ARROW_TEXTURE_IDENTIFIER = new Identifier("g4mespeed/textures/rightarrow.png");
	private static final GSTexture ARROW_TEXTURE = new GSTexture(ARROW_TEXTURE_IDENTIFIER, 30, 10);
	
	private final GSIcon icon;
	private final Text title;
	private final GSDropdown dropdown;

	private GSPopup popup;
	private final GSIMouseListener mouseListener;
	private final GSIActionListener actionListener;
	
	private boolean enabled;
	
	public GSDropdownSubMenu(Text text, GSDropdown dropdown) {
		this(null, text, dropdown);
	}
	
	public GSDropdownSubMenu(GSIcon icon, Text title, GSDropdown dropdown) {
		if (dropdown == null)
			throw new IllegalArgumentException("dropdown is null");
		
		this.icon = icon;
		this.title = title;
		this.dropdown = dropdown;
		
		popup = null;
		mouseListener = new GSSubMenuMouseListener();
		actionListener = new GSSubMenuActionListener();
		
		enabled = true;
	}

	@Override
	public void onAdded(GSPanel parent) {
		super.onAdded(parent);
	
		parent.addMouseEventListener(mouseListener);
	}
	
	@Override
	public void onRemoved(GSPanel parent) {
		super.onRemoved(parent);

		parent.removeMouseEventListener(mouseListener);
		
		if (popup != null)
			hideSubMenu();
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		// Layout of a sub-menu item is:
		// -------------------------
		// | I | TEXT          | > |
		// -------------------------
		
		boolean selected = (renderer.isMouseInside(0, 0, width, height) || popup != null);
		
		int backgroundColor = (enabled && selected) ? HOVERED_BACKGROUND_COLOR : BACKGROUND_COLOR;
		renderer.fillRect(0, 0, width, height, backgroundColor);

		// Force allowed icon size to that predefined in GSDropdownItem
		int iy = Math.max((height - ICON_SIZE) / 2, 0);
		if (icon != null)
			renderIcon(renderer, PADDING, iy, ICON_SIZE, ICON_SIZE);
	
		// Center drawn text on y-axis
		int ty = Math.max((height - renderer.getTextHeight() + 1) / 2, 0);
		int textColor = enabled ? (selected ? HOVERED_TEXT_COLOR : TEXT_COLOR) : DISABLED_TEXT_COLOR;
		renderTitle(renderer, PADDING + ICON_SIZE + ICON_MARGIN, ty, textColor);

		// Render right arrow at same height as icon
		renderRightArrow(renderer, width - ICON_SIZE - PADDING, iy, selected);
	}
	
	protected void renderIcon(GSIRenderer2D renderer, int x, int y, int w, int h) {
		icon.render(renderer, new GSRectangle(x, y, w, h));
	}
	
	protected void renderTitle(GSIRenderer2D renderer, int tx, int ty, int textColor) {
		renderer.drawText(title, tx, ty, textColor, false);
	}
	
	protected void renderRightArrow(GSIRenderer2D renderer, int x, int y, boolean selected) {
		int sx = enabled ? (selected ? 10 : 0) : 20;
		renderer.drawTexture(ARROW_TEXTURE, x, y, ICON_SIZE, ICON_SIZE, sx, 0);
	}

	@Override
	public GSDimension calculatePreferredSize() {
		GSIRenderer2D renderer = GSPanelContext.getRenderer();

		int w = 0;
		// Icon size and margin (leftmost element)
		w += ICON_SIZE + ICON_MARGIN;
		// Add estimated title width
		w += Math.round(renderer.getTextWidth(title));
		// Add equal amount of padding to the right for balance
		w += ICON_MARGIN + ICON_SIZE;
		
		int h = Math.max(ICON_SIZE, renderer.getTextHeight());
		
		return new GSDimension(w + 2 * PADDING, h + 2 * PADDING);
	}
	
	public Text getText() {
		return title;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if (enabled != this.enabled) {
			this.enabled = enabled;
			
			if (!enabled && popup != null)
				hideSubMenu();
		}
	}
	
	private void showSubMenu() {
		GSDropdown parent = getParentDropdown();
		if (parent != null) {
			// When the new pop-up is shown, the parent
			// drop-down will lose focus.
			parent.setHideOnFocusLost(false);
		}
		
		popup = new GSPopup(dropdown);
		popup.show(getPopupLocation());
		dropdown.addActionListener(actionListener);
	}
	
	private GSLocation getPopupLocation() {
		GSLocation location = getAbsoluteLocation(width, 0);
		int x = location.getX();
		
		GSDimension pref = popup.getPreferredSize();
		GSPanel rootPanel = GSPanelContext.getRootPanel();
		if (x + pref.getWidth() >= rootPanel.getWidth()) {
			// Move pop-up to the left side
			x = Math.max(x - width - pref.getWidth(), 0);
		}
		
		return new GSLocation(x, location.getY());
	}

	private void hideSubMenu() {
		dropdown.removeActionListener(actionListener);
		popup.hide();
		popup = null;
		
		GSDropdown parent = getParentDropdown();
		if (parent != null) {
			// Request focus on parent drop-down before
			// setting hide on focus lost to true.
			parent.requestFocus();
			parent.setHideOnFocusLost(true);
		}
	}
	
	private GSLocation getAbsoluteLocation(int x, int y) {
		GSPanel panel = this;
		while (panel != null) {
			x += panel.getX();
			y += panel.getY();
			panel = panel.getParent();
		}
		
		return new GSLocation(x, y);
	}
	
	@Override
	public GSECursorType getCursor() {
		return isEnabled() ? GSECursorType.HAND : super.getCursor();
	}
	
	private class GSSubMenuMouseListener implements GSIMouseListener {
		
		@Override
		public void mouseMoved(GSMouseEvent event) {
			// Since coordinates are in parent space, we can use
			// the #isInBounds(x, y) method for checking bounds.
			if (enabled && isInBounds(event.getX(), event.getY())) {
				if (popup == null)
					showSubMenu();
			} else {
				if (popup != null)
					hideSubMenu();
			}
		}
	}
	
	private class GSSubMenuActionListener implements GSIActionListener {
		
		@Override
		public void actionPerformed() {
			GSDropdown parent = getParentDropdown();
			if (parent != null)
				parent.onActionPerformed();
		}
	}
}
