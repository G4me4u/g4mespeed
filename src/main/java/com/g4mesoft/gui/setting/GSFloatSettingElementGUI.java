package com.g4mesoft.gui.setting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSFloatSetting;

import net.minecraft.util.Formatting;

public class GSFloatSettingElementGUI extends GSNumberSettingElementGUI<GSFloatSetting> {

	private static final float MAX_DEF_INTERVAL_FOR_SLIDER = 100.0f;

	private static final DecimalFormat FORMATTER = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.ENGLISH));
	
	public GSFloatSettingElementGUI(GSSettingsGUI settingsGUI, GSFloatSetting setting, GSSettingCategory category) {
		super(settingsGUI, setting, category);
	}

	@Override
	protected void setValueFromSlider(float value) {
		setting.setValue(value * (setting.getMaxValue() - setting.getMinValue()) + setting.getMinValue());
	}
	
	@Override
	protected boolean setValueFromTextField(String str) {
		try {
			setting.setValue(Float.parseFloat(str));
		} catch (NumberFormatException e) {
			return false;
		}
		
		return true;
	}

	@Override
	protected boolean shouldUseSlider() {
		return (setting.getMaxValue() - setting.getMinValue() < MAX_DEF_INTERVAL_FOR_SLIDER);
	}

	@Override
	protected void updateFieldValue() {
		if (shouldUseSlider()) {
			setSliderValue((setting.getValue() - setting.getMinValue()) / (setting.getMaxValue() - setting.getMinValue()));
			setSliderText(FORMATTER.format(setting.getValue().doubleValue()));
		} else {
			setTextFieldValue(String.format(Locale.ENGLISH, "%.3f", setting.getValue()));
		}
	}
	
	@Override
	public String getFormattedDefault() {
		return Formatting.AQUA + FORMATTER.format(setting.getDefaultValue().doubleValue()) + Formatting.RESET;
	}
}
