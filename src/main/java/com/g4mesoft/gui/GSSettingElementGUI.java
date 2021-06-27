package com.g4mesoft.gui;

import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.legend.GSButtonPanel;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public abstract class GSSettingElementGUI<T extends GSSetting<?>> extends GSParentPanel {

	public static final int HOVERED_BACKGROUND = 0x80000000;//0x66EDEDFF;
	
	protected static final int CONTENT_PADDING = 2;
	protected static final int CONTENT_MARGIN = 2;
	
	protected static final int RESET_BUTTON_WIDTH = 48;
	protected static final int RESET_BUTTON_HEIGHT = 20;
	protected static final Text RESET_TEXT = new TranslatableText("setting.button.reset");

	private static final int ENABLED_TEXT_COLOR = 0xFFFFFFFF;
	private static final int DISABLED_TEXT_COLOR = 0xFFAAAAAA;
	
	protected final GSSettingsGUI settingsGUI;
	protected final T setting;
	protected final GSSettingCategory category;
	
	protected final TranslatableText nameText;
	
	private final GSButtonPanel resetButton;
	
	public GSSettingElementGUI(GSSettingsGUI settingsGUI, T setting, GSSettingCategory category) {
		this.settingsGUI = settingsGUI;
		this.setting = setting;
		this.category = category;
		
		nameText = new TranslatableText("setting." + category.getName() + "." + setting.getName());
		
		resetButton = new GSButtonPanel(RESET_TEXT, this::resetSetting);
		add(resetButton);
	}
	
	public void setPreferredBounds(int x, int y, int width) {
		setBounds(x, y, width, getPreferredHeight());
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		if (renderer.isMouseInside(0, 0, width, height))
			renderer.fillRect(0, 0, width, height, HOVERED_BACKGROUND);
		
		super.render(renderer);
	}
	
	public int getTextColor() {
		return setting.isEnabledInGui() ? ENABLED_TEXT_COLOR : DISABLED_TEXT_COLOR;
	}
	
	@Override
	public void layout() {
		int x = width - CONTENT_PADDING - RESET_BUTTON_WIDTH;
		int y = (getSettingHeight() - RESET_BUTTON_HEIGHT) / 2;
		resetButton.setBounds(x, y, RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT);
		
		updateResetActive();
	}

	protected void resetSetting() {
		if (setting.isEnabledInGui())
			setting.reset();
	}
	
	public void onSettingChanged() {
		updateResetActive();
	}
	
	private void updateResetActive() {
		resetButton.setEnabled(!setting.isDefaultValue() && setting.isEnabledInGui());
	}

	public abstract Text getFormattedDefault();

	protected int getSettingHeight() {
		return height;
	}
	
	public int getPreferredWidth() {
		return RESET_BUTTON_WIDTH + CONTENT_PADDING * 2;
	}

	public int getPreferredHeight() {
		return RESET_BUTTON_HEIGHT + CONTENT_PADDING * 2;
	}
	
	public TranslatableText getSettingNameText() {
		return nameText;
	}
}
