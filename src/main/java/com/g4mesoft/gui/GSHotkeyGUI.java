package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.g4mesoft.hotkey.GSIKeyBindingRegisterListener;
import com.g4mesoft.hotkey.GSKeyBinding;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.legend.GSButtonPanel;
import com.g4mesoft.panel.scroll.GSIScrollable;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GSHotkeyGUI extends GSParentPanel implements GSIScrollable, GSIKeyBindingRegisterListener {

	private static final int HOTKEY_MARGIN = 1;

	private static final int CATEGORY_MARGIN = 5;
	private static final int CATEGORY_TITLE_BOTTOM_MARGIN = 2;
	private static final int CATEGORY_TITLE_COLOR = 0xFFFFFFFF;

	private static final int BUTTON_WIDTH = 96;
	
	private static final Text RESET_ALL_TEXT = new TranslatableText("gui.hotkey.resetAll");
	private static final Text UNBIND_ALL_TEXT = new TranslatableText("gui.hotkey.unbindAll");
	
	private final Map<String, GSHotkeyCategoryGUI> hotkeyCategories;
	
	private final GSButtonPanel resetAllButton;
	private final GSButtonPanel unbindAllButton;
	
	private int contentHeight;
	
	private GSHotkeyElementGUI changingElement;
	
	public GSHotkeyGUI(GSKeyManager keyManager) {
		hotkeyCategories = new LinkedHashMap<>();
		
		resetAllButton = new GSButtonPanel(RESET_ALL_TEXT, () -> {
			for (GSHotkeyCategoryGUI category : hotkeyCategories.values())
				category.resetAll();
		});
		
		unbindAllButton = new GSButtonPanel(UNBIND_ALL_TEXT, () -> {
			for (GSHotkeyCategoryGUI category : hotkeyCategories.values())
				category.unbindAll();
		});
		
		add(resetAllButton);
		add(unbindAllButton);
		
		keyManager.getKeyBindings().forEach(this::addKeyEntry);
		keyManager.setKeyRegisterListener(this);
	}

	@Override
	public void layout() {
		int w = 0;
		for (GSHotkeyCategoryGUI hotkeyCategory : hotkeyCategories.values()) {
			int prefWidth = hotkeyCategory.getPreferredWidth();
			if (prefWidth > w)
				w = prefWidth;
		}
		
		if (this.width < w)
			w = this.width;

		int y = 0;
		for (GSHotkeyCategoryGUI hotkeyCategory : hotkeyCategories.values())
			y = hotkeyCategory.layoutHotkeys(0, y, w);
		
		y += CATEGORY_MARGIN;

		int buttonMargin = w - BUTTON_WIDTH * 2;
		int bw = Math.min(BUTTON_WIDTH, BUTTON_WIDTH + buttonMargin / 2);
		
		resetAllButton.setPreferredBounds(0, y, bw);
		unbindAllButton.setPreferredBounds(bw + buttonMargin, y, bw);
		
		y += GSButtonPanel.BUTTON_HEIGHT;
		
		contentHeight = y;
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);
		
		for (GSHotkeyCategoryGUI hotkeyCategory : hotkeyCategories.values())
			hotkeyCategory.renderTitle(renderer);
	}

	@Override
	public void onKeyRegistered(GSKeyBinding keyBinding) {
		addKeyEntry(keyBinding);
	}
	
	private void addKeyEntry(GSKeyBinding keyBinding) {
		GSHotkeyCategoryGUI category = hotkeyCategories.get(keyBinding.getCategory());
		if (category == null) {
			category = new GSHotkeyCategoryGUI(keyBinding.getCategory());
			hotkeyCategories.put(keyBinding.getCategory(), category);
		}
		
		category.addKeyBinding(keyBinding);

		if (isVisible())
			requestLayout();
	}

	@Override
	public int getContentWidth() {
		return width;
	}

	@Override
	public int getContentHeight() {
		return contentHeight;
	}

	public void setChangingElement(GSHotkeyElementGUI element) {
		if (changingElement != null && element != null)
			throw new IllegalStateException("Can not change multiple elements at once!");
		changingElement = element;
	}
	
	public GSHotkeyElementGUI getChangingElement() {
		return changingElement;
	}
	
	private class GSHotkeyCategoryGUI {
		
		private final Text categoryNameText;

		private final List<GSHotkeyElementGUI> elements;
		
		private int x;
		private int y;
		private int w;
		
		public GSHotkeyCategoryGUI(String name) {
			categoryNameText = new TranslatableText("hotkey." + name + ".title");
			
			elements = new ArrayList<>();
		}
		
		public void addKeyBinding(GSKeyBinding keyBinding) {
			GSHotkeyElementGUI hotkeyElement = new GSHotkeyElementGUI(GSHotkeyGUI.this, keyBinding);
			
			elements.add(hotkeyElement);
			
			GSHotkeyGUI.this.add(hotkeyElement);
		}

		public int getPreferredWidth() {
			int prefWidth = 0;
			for (GSHotkeyElementGUI hotkeyElement : elements) {
				int pw = hotkeyElement.getPreferredWidth();
				if (pw > prefWidth)
					prefWidth = pw;
			}
			
			return prefWidth;
		}

		public int layoutHotkeys(int x, int y, int w) {
			this.x = x;
			this.y = y;
			this.w = w;
			
			GSIRenderer2D renderer = GSPanelContext.getRenderer();
			y += CATEGORY_MARGIN + renderer.getTextHeight() + CATEGORY_TITLE_BOTTOM_MARGIN;
			
			for (GSHotkeyElementGUI hotkeyElement : elements) {
				int h = hotkeyElement.getPreferredHeight();
				hotkeyElement.setBounds(x, y, w, h);
				y += h + HOTKEY_MARGIN * 2;
			}
			
			return y;
		}
		
		public void renderTitle(GSIRenderer2D renderer) {
			int tx = x + w / 2;
			int ty = y + CATEGORY_MARGIN;

			renderer.drawCenteredText(categoryNameText, tx, ty, CATEGORY_TITLE_COLOR);
		}
		
		public void resetAll() {
			elements.forEach(GSHotkeyElementGUI::resetKeyCode);
		}

		public void unbindAll() {
			elements.forEach(GSHotkeyElementGUI::unbindKeyCode);
		}
	}
}
