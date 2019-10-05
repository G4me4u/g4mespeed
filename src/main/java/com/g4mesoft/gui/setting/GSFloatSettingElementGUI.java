package com.g4mesoft.gui.setting;

import java.text.DecimalFormat;

import com.g4mesoft.gui.widget.GSSliderWidget;
import com.g4mesoft.gui.widget.GSToggleSwitchWidget;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSFloatSetting;

import net.minecraft.util.Formatting;

public class GSFloatSettingElementGUI extends GSSettingElementGUI<GSFloatSetting> {

	private static final int SETTING_HEIGHT = Math.max(16, GSToggleSwitchWidget.TOGGLE_SWITCH_HEIGHT);

	private static final int TEXT_MAX_WIDTH = 96;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	
	private static final DecimalFormat FORMATTER = new DecimalFormat("#0.00");
	
	private GSSliderWidget slider;
	
	public GSFloatSettingElementGUI(GSSettingsGUI settingsGUI, GSFloatSetting setting, GSSettingCategory category) {
		super(settingsGUI, setting, category);
	
		slider = new GSSliderWidget(0, 0, 0, setting.getValue(), (value) -> {
			float delta = this.setting.getMaxValue() - this.setting.getMinValue();
			this.setting.setValue((float)(this.setting.getMinValue() + delta * value));
		}, (value) -> {
			return FORMATTER.format(value);
		});
	}

	@Override
	public void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		super.renderTranslated(mouseX, mouseY, partialTicks);

		String name = getTranslationModule().getTranslation(settingTranslationName);
		drawString(font, name, CONTENT_PADDING, (getSettingHeight() - font.fontHeight) / 2, TEXT_COLOR);
	}
	
	@Override
	public int getPreferredWidth() {
		return super.getPreferredWidth() + TEXT_MAX_WIDTH + CONTENT_MARGIN * 2;
	}

	@Override
	public int getPreferredHeight() {
		int prefHeight = Math.max(super.getPreferredHeight(), SETTING_HEIGHT + CONTENT_PADDING * 2);
		if (slider != null)
			prefHeight += GSSliderWidget.SLIDER_HEIGHT + CONTENT_MARGIN;
		return prefHeight;
	}
	
	@Override
	public void init() {
		super.init();
		
		if (slider != null) {
			slider.x = CONTENT_PADDING;
			slider.y = height - CONTENT_PADDING - GSSliderWidget.SLIDER_HEIGHT;
			slider.setWidth(width - CONTENT_PADDING * 2);
			
			addWidget(slider);
		}
	}
	
	@Override
	protected int getSettingHeight() {
		int settingHeight = super.getSettingHeight();
		if (slider != null)
			settingHeight -= GSSliderWidget.SLIDER_HEIGHT + CONTENT_MARGIN;
		return settingHeight;
	}
	
	@Override
	public void onSettingChanged() {
		if (slider != null) {
			float delta = this.setting.getMaxValue() - this.setting.getMinValue();
			slider.setValueSilent((this.setting.getValue() - this.setting.getMinValue()) / delta);
		}
	}
	
	@Override
	public String getFormattedDefault() {
		return Formatting.AQUA + FORMATTER.format(setting.getDefaultValue().doubleValue()) + Formatting.RESET;
	}
}
