package com.g4mesoft.gui.hotkey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.g4mesoft.gui.GSScrollableParentGUI;
import com.g4mesoft.hotkey.GSIKeyRegisterListener;
import com.g4mesoft.hotkey.GSKeyBinding;
import com.g4mesoft.hotkey.GSKeyManager;

import net.minecraft.client.util.NarratorManager;

public class GSHotkeyGUI extends GSScrollableParentGUI implements GSIKeyRegisterListener {

	private static final int HOTKEY_MARGIN = 1;

	private static final int CATEGORY_TITLE_MARGIN = 2;
	private static final int CATEGORY_TITLE_COLOR = 0xFFFFFFFF;
	
	private final Map<String, GSHotkeyCategoryGUI> hotkeyCategories;
	
	private int scrollableHeight;
	private boolean needsRelayout;
	
	public GSHotkeyGUI(GSKeyManager keyManager) {
		super(NarratorManager.EMPTY);
		
		hotkeyCategories = new LinkedHashMap<String, GSHotkeyCategoryGUI>();
		
		for (GSKeyBinding keyBinding : keyManager.getKeyBindings())
			addKeyEntry(keyBinding);

		keyManager.setKeyRegisterListener(this);
	}

	private void layoutHotkeys() {
		int w = 0;
		for (GSHotkeyCategoryGUI hotkeyCategory : hotkeyCategories.values()) {
			int prefWidth = hotkeyCategory.getPreferredWidth();
			if (prefWidth > w)
				w = prefWidth;
		}
		
		if (this.width < w)
			w = this.width;

		int y = HOTKEY_MARGIN;
		for (GSHotkeyCategoryGUI hotkeyCategory : hotkeyCategories.values())
			y = hotkeyCategory.layoutHotkeys(HOTKEY_MARGIN, y, w);
		
		scrollableHeight = y;
	}
	
	@Override
	public void init() {
		super.init();
	
		needsRelayout = true;
	}
	
	@Override
	protected void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		if (needsRelayout) {
			layoutHotkeys();
			needsRelayout = false;
		}
		
		super.renderTranslated(mouseX, mouseY, partialTicks);
		
		for (GSHotkeyCategoryGUI hotkeyCategory : hotkeyCategories.values())
			hotkeyCategory.render(mouseX, mouseY, partialTicks);
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
		needsRelayout = true;
	}

	@Override
	protected int getScrollableHeight() {
		return scrollableHeight;
	}
	
	private class GSHotkeyCategoryGUI {
		
		private final List<GSHotkeyElementGUI> hotkeyElements;
		private final String categoryName;
		
		private int x;
		private int y;
		private int w;
		
		public GSHotkeyCategoryGUI(String name) {
			hotkeyElements = new ArrayList<GSHotkeyElementGUI>();
			categoryName = "hotkey." + name + ".title";
		}
		
		public void addKeyBinding(GSKeyBinding keyBinding) {
			hotkeyElements.add(new GSHotkeyElementGUI(keyBinding));
		}

		public int getPreferredWidth() {
			int prefWidth = 0;
			for (GSHotkeyElementGUI hotkeyElement : hotkeyElements) {
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
			
			y += font.fontHeight + CATEGORY_TITLE_MARGIN * 2;
			
			for (GSHotkeyElementGUI hotkeyElement : hotkeyElements) {
				int h = hotkeyElement.getPreferredHeight();
				hotkeyElement.initBounds(minecraft, x, y, w, h);
				y += h + HOTKEY_MARGIN * 2;
			}
			
			
			return y;
		}
		
		public void render(int mouseX, int mouseY, float partialTicks) {
			String title = getTranslationModule().getTranslation(categoryName);
			drawCenteredString(font, title, x + w / 2, y + CATEGORY_TITLE_MARGIN, CATEGORY_TITLE_COLOR);
			
			for (GSHotkeyElementGUI hotkeyElement : hotkeyElements)
				hotkeyElement.render(mouseX, mouseY, partialTicks);
		}
	}
}
