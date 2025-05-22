package com.g4mesoft.gui.setting;

import java.util.Locale;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSIntegerSetting;
import com.g4mesoft.ui.panel.GSPanelContext;
import com.g4mesoft.ui.util.GSTextUtil;

import net.minecraft.text.Formatting;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

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
	
	private Text getFormattedValue(int value) {
		String valueText = Integer.toString(value);
		String nameTextKey = nameText.getKey();
		
		GSTranslationModule translationModule =
				GSClientController.getInstance().getTranslationModule();
		
		String key;
		
		key = nameTextKey + "." + valueText;
		if (translationModule.hasTranslation(key) || GSPanelContext.hasI18nTranslation(key))
			return GSTextUtil.translatable(key, valueText);
		
		key = nameTextKey + ".x";
		if (translationModule.hasTranslation(key) || GSPanelContext.hasI18nTranslation(key))
			return GSTextUtil.translatable(key, valueText);
		
		return GSTextUtil.literal(valueText);
	}
	
	@Override
	public Text getFormattedDefault() {
		return getFormattedValue(setting.getDefault()).setStyle(new Style().setColor(Formatting.AQUA));
	}
}
