package com.g4mesoft.gui.setting;

import java.util.Locale;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSIntegerSetting;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class GSIntegerSettingPanel extends GSAbstractNumberSettingPanel<GSIntegerSetting> {

	private static final int MAX_DEF_INTERVAL_FOR_SLIDER = 100;

	public GSIntegerSettingPanel(GSSettingCategory category, GSIntegerSetting setting) {
		super(category, setting);
	}

	@Override
	protected void setValueFromSlider(float value) {
		setting.set(Math.round(value * (setting.getMax() - setting.getMin()) + setting.getMin()));
	}
	
	@Override
	protected boolean setValueFromTextField(String str) {
		try {
			setting.set(Integer.parseInt(str));
		} catch (NumberFormatException e) {
			return false;
		}
		
		return true;
	}

	@Override
	protected boolean shouldUseSlider() {
		return setting.getMax() - setting.getMin() < MAX_DEF_INTERVAL_FOR_SLIDER;
	}

	@Override
	protected void updateFieldValue() {
		if (shouldUseSlider()) {
			setSliderValue((float)(setting.get() - setting.getMin()) / (setting.getMax() - setting.getMin()));
			setSliderText(getFormattedValue(setting.get()));
		} else {
			setTextFieldValue(String.format(Locale.ENGLISH, "%d", setting.get()));
		}
	}
	
	private MutableText getFormattedValue(int value) {
		String valueText = Integer.toString(value);
		String nameTranslationKey = nameText.getKey();
		
		GSTranslationModule translationModule =
				GSClientController.getInstance().getTranslationModule();
		
		String key;
		if (translationModule.hasTranslation(key = nameTranslationKey + "." + valueText))
			return new TranslatableText(key, valueText);
		if (translationModule.hasTranslation(key = nameTranslationKey + ".x"))
			return new TranslatableText(key, valueText);
		
		return new LiteralText(valueText);
	}
	
	@Override
	public Text getFormattedDefault() {
		return getFormattedValue(setting.getDefault()).formatted(Formatting.AQUA);
	}
}
