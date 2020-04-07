package com.g4mesoft.gui.setting;

import com.g4mesoft.gui.widget.GSToggleSwitchWidget;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSBooleanSetting;

import net.minecraft.util.Formatting;

public class GSBooleanSettingElementGUI extends GSSettingElementGUI<GSBooleanSetting> {

	private static final int SETTING_HEIGHT = Math.max(16, GSToggleSwitchWidget.TOGGLE_SWITCH_HEIGHT);

	private static final int TEXT_MAX_WIDTH = 140;
	
	private static final int TOGGLE_WIDTH = GSToggleSwitchWidget.TOGGLE_SWITCH_WIDTH;
	
	private final GSToggleSwitchWidget switchWidget;
	
	public GSBooleanSettingElementGUI(GSSettingsGUI settingsGUI, GSBooleanSetting setting, GSSettingCategory category) {
		super(settingsGUI, setting, category);
		
		switchWidget = new GSToggleSwitchWidget(0, 0, setting.getValue(), (state) -> {
			this.setting.setValue(state);
		});
	}

	@Override
	public void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		super.renderTranslated(mouseX, mouseY, partialTicks);
		
		String name = getTranslationModule().getTranslation(settingTranslationName);
		drawString(textRenderer, name, CONTENT_PADDING, (getSettingHeight() - textRenderer.fontHeight) / 2, getTextColor());
	}
	
	@Override
	public int getPreferredWidth() {
		return super.getPreferredWidth() + TEXT_MAX_WIDTH + TOGGLE_WIDTH + CONTENT_MARGIN * 2;
	}

	@Override
	public int getPreferredHeight() {
		return Math.max(super.getPreferredHeight(), SETTING_HEIGHT + CONTENT_PADDING * 2);
	}
	
	@Override
	public void init() {
		super.init();

		switchWidget.x = width - GSToggleSwitchWidget.TOGGLE_SWITCH_WIDTH - CONTENT_MARGIN - RESET_BUTTON_WIDTH - CONTENT_PADDING;
		switchWidget.y = (height - GSToggleSwitchWidget.TOGGLE_SWITCH_HEIGHT) / 2;
		switchWidget.active = setting.isEnabledInGui();
		
		addWidget(switchWidget);
	}
	
	@Override
	public void onSettingChanged() {
		super.onSettingChanged();
		
		switchWidget.setValueSilent(setting.getValue());
		switchWidget.active = setting.isEnabledInGui();
	}

	@Override
	public String getFormattedDefault() {
		return (setting.getDefaultValue() ? (Formatting.GREEN + "enabled") : (Formatting.RED + "disabled")) + Formatting.RESET;
	}
}
