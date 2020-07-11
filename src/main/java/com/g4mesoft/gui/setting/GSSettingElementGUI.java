package com.g4mesoft.gui.setting;

import com.g4mesoft.gui.GSParentPanel;
import com.g4mesoft.gui.action.GSButtonPanel;
import com.g4mesoft.gui.renderer.GSIRenderer2D;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;

public abstract class GSSettingElementGUI<T extends GSSetting<?>> extends GSParentPanel {

	public static final int HOVERED_BACKGROUND = 0x80000000;//0x66EDEDFF;
	
	protected static final int CONTENT_PADDING = 2;
	protected static final int CONTENT_MARGIN = 2;
	
	protected static final int RESET_BUTTON_WIDTH = 48;
	protected static final int RESET_BUTTON_HEIGHT = 20;
	protected static final String RESET_TEXT = "setting.button.reset";

	private static final int ENABLED_TEXT_COLOR = 0xFFFFFFFF;
	private static final int DISABLED_TEXT_COLOR = 0xFFAAAAAA;
	
	protected final GSSettingsGUI settingsGUI;
	protected final T setting;
	protected final GSSettingCategory category;
	
	protected final String nameTranslationKey;
	
	private final GSButtonPanel resetButton;
	
	public GSSettingElementGUI(GSSettingsGUI settingsGUI, T setting, GSSettingCategory category) {
		this.settingsGUI = settingsGUI;
		this.setting = setting;
		this.category = category;
		
		nameTranslationKey = "setting." + category.getName() + "." + setting.getName();
		
		resetButton = new GSButtonPanel(RESET_TEXT, false, this::resetSetting);
		add(resetButton);
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
	public void onBoundsChanged() {
		super.onBoundsChanged();
		
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

	public abstract String getFormattedDefault();

	protected int getSettingHeight() {
		return height;
	}
	
	public int getPreferredWidth() {
		return RESET_BUTTON_WIDTH + CONTENT_PADDING * 2;
	}

	public int getPreferredHeight() {
		return RESET_BUTTON_HEIGHT + CONTENT_PADDING * 2;
	}
	
	public String getSettingTranslationName() {
		return nameTranslationKey;
	}
}
