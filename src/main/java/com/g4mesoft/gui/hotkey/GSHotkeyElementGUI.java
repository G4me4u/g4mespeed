package com.g4mesoft.gui.hotkey;

import com.g4mesoft.gui.GSParentGUI;
import com.g4mesoft.hotkey.GSKeyBinding;

import net.minecraft.client.util.NarratorManager;

public class GSHotkeyElementGUI extends GSParentGUI {

	private static final int HOTKEY_HEIGHT = 20;
	private static final int HOTKEY_PADDING = 2;
	
	private static final int FONT_COLOR = 0xFFFFFFFF;
	
	private final GSKeyBinding keyBinding;
	private final String keyName;
	
	public GSHotkeyElementGUI(GSKeyBinding keyBinding) {
		super(NarratorManager.EMPTY);
		
		this.keyBinding = keyBinding;
		this.keyName = "hotkey." + keyBinding.getCategory() + "." + keyBinding.getName();
	}

	@Override
	public void init() {
		super.init();
		
		
	}
	
	@Override
	protected void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		fill(0, 0, width, height, 0xFF000000);

		super.renderTranslated(mouseX, mouseY, partialTicks);

		String name = getTranslationModule().getTranslation(keyName);
		drawString(font, name, HOTKEY_PADDING, (height - font.fontHeight) / 2, FONT_COLOR);
	}
	
	public int getPreferredHeight() {
		return HOTKEY_HEIGHT + HOTKEY_PADDING * 2;
	}

	public int getPreferredWidth() {
		return 200 + HOTKEY_PADDING * 2;
	}
}
