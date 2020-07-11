package com.g4mesoft.gui.setting;

import com.g4mesoft.gui.action.GSToggleSwitchPanel;
import com.g4mesoft.gui.renderer.GSIRenderer2D;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSBooleanSetting;

import net.minecraft.util.Formatting;

public class GSBooleanSettingElementGUI extends GSSettingElementGUI<GSBooleanSetting> {

	private static final int SETTING_HEIGHT = Math.max(16, GSToggleSwitchPanel.SWITCH_HEIGHT);

	private static final int TEXT_MAX_WIDTH = 140;
	
	private static final int TOGGLE_WIDTH = GSToggleSwitchPanel.SWITCH_WIDTH;
	
	private final GSToggleSwitchPanel switchWidget;
	
	public GSBooleanSettingElementGUI(GSSettingsGUI settingsGUI, GSBooleanSetting setting, GSSettingCategory category) {
		super(settingsGUI, setting, category);
		
		switchWidget = new GSToggleSwitchPanel(this::updateSettingValue, setting.getValue());
		add(switchWidget);
	}
	
	public void updateSettingValue() {
		setting.setValue(switchWidget.isToggled());
	}

	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);
		
		String name = i18nTranslate(nameTranslationKey);
		int ty = (getSettingHeight() - renderer.getFontHeight()) / 2;
		
		renderer.drawString(name, CONTENT_PADDING, ty, getTextColor());
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
	public void onBoundsChanged() {
		super.onBoundsChanged();

		int sx = width - TOGGLE_WIDTH - CONTENT_MARGIN - RESET_BUTTON_WIDTH - CONTENT_PADDING;
		int sy = (height - GSToggleSwitchPanel.SWITCH_HEIGHT) / 2;
		switchWidget.setPreferredBounds(sx, sy);
		switchWidget.setEnabled(setting.isEnabledInGui());
	}
	
	@Override
	public void onSettingChanged() {
		super.onSettingChanged();
		
		switchWidget.setToggled(setting.getValue());
		switchWidget.setEnabled(setting.isEnabledInGui());
	}

	@Override
	public String getFormattedDefault() {
		return (setting.getDefaultValue() ? (Formatting.GREEN + "enabled") : (Formatting.RED + "disabled")) + Formatting.RESET;
	}
}
