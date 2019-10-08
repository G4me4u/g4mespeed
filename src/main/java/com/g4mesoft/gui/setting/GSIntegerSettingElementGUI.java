package com.g4mesoft.gui.setting;

import java.util.Locale;

import com.g4mesoft.module.translation.GSTranslationModule;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSIntegerSetting;

import net.minecraft.util.Formatting;

public class GSIntegerSettingElementGUI extends GSNumberSettingElementGUI<GSIntegerSetting> {

	private static final int MAX_DEF_INTERVAL_FOR_SLIDER = 100;

	public GSIntegerSettingElementGUI(GSSettingsGUI settingsGUI, GSIntegerSetting setting, GSSettingCategory category) {
		super(settingsGUI, setting, category);
	}

	@Override
	protected String getSliderText() {
		return getFormattedValue(setting.getValue());
	}
	
	@Override
	protected void setValueFromSlider(double value) {
		setting.setValue((int)Math.round(value * (setting.getMaxValue() - setting.getMinValue()) + setting.getMinValue()));
	}
	
	@Override
	protected boolean setValueFromTextField(String str) {
		try {
			setting.setValue(Integer.parseInt(str));
		} catch (NumberFormatException e) {
			return false;
		}
		
		return true;
	}

	@Override
	protected boolean shouldUseSlider() {
		return setting.getMaxValue() - setting.getMinValue() < MAX_DEF_INTERVAL_FOR_SLIDER;
	}

	@Override
	protected void updateFieldValue() {
		if (shouldUseSlider()) {
			setSliderValue((double)(setting.getValue() - setting.getMinValue()) / (setting.getMaxValue() - setting.getMinValue()));
		} else {
			setTextFieldValue(String.format(Locale.ENGLISH, "%d", setting.getValue()));
		}
	}
	
	private String getFormattedValue(int value) {
		String valueText = String.format(Locale.ENGLISH, "%d", value);
		
		GSTranslationModule translationModule = getTranslationModule();

		String key;
		if (translationModule.hasTranslation(key = settingTranslationName + "." + valueText))
			return translationModule.getFormattedTranslation(key, valueText);
		if (translationModule.hasTranslation(key = settingTranslationName + ".x"))
			return translationModule.getFormattedTranslation(key, valueText);
		
		return valueText;
	}
	
	@Override
	public String getFormattedDefault() {
		return Formatting.AQUA + getFormattedValue(setting.getDefaultValue()) + Formatting.RESET;
	}
}
