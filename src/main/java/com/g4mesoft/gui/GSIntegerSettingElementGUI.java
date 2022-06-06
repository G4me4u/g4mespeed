package com.g4mesoft.gui;

import java.util.Locale;

import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSIntegerSetting;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GSIntegerSettingElementGUI extends GSNumberSettingElementGUI<GSIntegerSetting> {

	private static final int MAX_DEF_INTERVAL_FOR_SLIDER = 100;

	public GSIntegerSettingElementGUI(GSSettingsGUI settingsGUI, GSIntegerSetting setting, GSSettingCategory category) {
		super(settingsGUI, setting, category);
	}

	@Override
	protected void setValueFromSlider(float value) {
		setting.setValue(Math.round(value * (setting.getMaxValue() - setting.getMinValue()) + setting.getMinValue()));
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
			setSliderValue((float)(setting.getValue() - setting.getMinValue()) / (setting.getMaxValue() - setting.getMinValue()));
			setSliderText(getFormattedValue(setting.getValue()));
		} else {
			setTextFieldValue(String.format(Locale.ENGLISH, "%d", setting.getValue()));
		}
	}
	
	private MutableText getFormattedValue(int value) {
		String valueText = String.format(Locale.ENGLISH, "%d", value);
		
		String key;
		if (hasI18nTranslation(key = nameTextKey + "." + valueText))
			return Text.translatable(key, valueText);
		if (hasI18nTranslation(key = nameTextKey + ".x"))
			return Text.translatable(key, valueText);
		
		return Text.literal(valueText);
	}
	
	@Override
	public Text getFormattedDefault() {
		return getFormattedValue(setting.getDefaultValue()).formatted(Formatting.AQUA);
	}
}
