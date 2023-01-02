package com.g4mesoft.gui.setting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSFloatSetting;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GSFloatSettingPanel extends GSAbstractNumberSettingPanel<GSFloatSetting> {

	private static final float MAX_DEF_INTERVAL_FOR_SLIDER = 100.0f;

	private static final DecimalFormat FORMATTER = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.ENGLISH));
	
	public GSFloatSettingPanel(GSSettingCategory category, GSFloatSetting setting) {
		super(category, setting);
	}

	@Override
	protected void setValueFromSlider(float value) {
		setting.set(value * (setting.getMax() - setting.getMin()) + setting.getMin());
	}
	
	@Override
	protected boolean setValueFromTextField(String str) {
		try {
			setting.set(Float.parseFloat(str));
		} catch (NumberFormatException e) {
			return false;
		}
		
		return true;
	}

	@Override
	protected boolean shouldUseSlider() {
		return (setting.getMax() - setting.getMin() < MAX_DEF_INTERVAL_FOR_SLIDER);
	}

	@Override
	protected void updateFieldValue() {
		if (shouldUseSlider()) {
			setSliderValue((setting.get() - setting.getMin()) / (setting.getMax() - setting.getMin()));
			setSliderText(Text.literal(FORMATTER.format(setting.get().doubleValue())));
		} else {
			setTextFieldValue(String.format(Locale.ENGLISH, "%.3f", setting.get()));
		}
	}
	
	@Override
	public Text getFormattedDefault() {
		return Text.literal(FORMATTER.format(setting.getDefault().doubleValue())).formatted(Formatting.AQUA);
	}
}
