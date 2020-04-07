package com.g4mesoft.gui.setting;

import com.g4mesoft.gui.widget.GSSliderWidget;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

public abstract class GSNumberSettingElementGUI<T extends GSSetting<?>> extends GSSettingElementGUI<T> {

	private static final int SETTING_HEIGHT = 16;
	private static final int TEXT_FIELD_HEIGHT = 20;
	private static final int TEXT_FIELD_MAX_WIDTH = 128;
	private static final String SET_VALUE_TEXT = "setting.button.set";
	
	private static final int TEXT_MAX_WIDTH = 96;
	
	protected GSSliderWidget slider;
	protected TextFieldWidget textField;
	
	protected String prevTextFieldValue;
	
	public GSNumberSettingElementGUI(GSSettingsGUI settingsGUI, T setting, GSSettingCategory category) {
		super(settingsGUI, setting, category);
	}

	@Override
	public void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		super.renderTranslated(mouseX, mouseY, partialTicks);

		String name = getTranslationModule().getTranslation(settingTranslationName);
		drawString(textRenderer, name, CONTENT_PADDING, (getSettingHeight() - textRenderer.fontHeight) / 2, getTextColor());
	}
	
	@Override
	public int getPreferredWidth() {
		return super.getPreferredWidth() + TEXT_MAX_WIDTH + CONTENT_MARGIN * 2;
	}

	@Override
	public int getPreferredHeight() {
		int prefHeight = Math.max(super.getPreferredHeight(), SETTING_HEIGHT + CONTENT_PADDING * 2);
		if (slider != null) {
			prefHeight += GSSliderWidget.SLIDER_HEIGHT + CONTENT_MARGIN;
		} else {
			prefHeight += TEXT_FIELD_HEIGHT + CONTENT_MARGIN;
		}
		return prefHeight;
	}
	
	@Override
	public void init() {
		super.init();
		
		if (shouldUseSlider()) {
			slider = new GSSliderWidget(0, 0, 0, 0.0, (value) -> {
				setValueFromSlider(value);
				updateFieldValue();
			}, (value) -> getSliderText());
			slider.active = setting.isEnabledInGui();
			
			slider.setWidth(Math.min(GSSliderWidget.MAX_WIDTH, width - CONTENT_PADDING * 2));

			slider.x = (width - slider.getWidth()) / 2;
			slider.y = height - CONTENT_PADDING - GSSliderWidget.SLIDER_HEIGHT;
			
			addWidget(slider);
		} else {
			int tw = Math.min(TEXT_FIELD_MAX_WIDTH, width - CONTENT_MARGIN - RESET_BUTTON_WIDTH - CONTENT_PADDING * 2);
			int ty = height - CONTENT_PADDING - TEXT_FIELD_HEIGHT;
			
			addWidget(textField = new TextFieldWidget(textRenderer, CONTENT_PADDING, ty, tw, TEXT_FIELD_HEIGHT, ""));
			textField.active = setting.isEnabledInGui();

			int bx = width - CONTENT_PADDING - RESET_BUTTON_WIDTH;
			int by = ty + (TEXT_FIELD_HEIGHT - RESET_BUTTON_HEIGHT) / 2;
			String setValueText = getTranslationModule().getTranslation(SET_VALUE_TEXT);

			addWidget(new ButtonWidget(bx, by, RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT, setValueText, (but) -> {
				String str = textField.getText();
				if (!str.equals(prevTextFieldValue)) {
					if (setValueFromTextField(str)) {
						prevTextFieldValue = str;

						// Make sure the textfield text is
						// correct (re-validate it).
						updateFieldValue();
					} else {
						textField.setText(prevTextFieldValue);
					}
				}
			}));
		}
		
		updateFieldValue();
	}
	
	protected abstract String getSliderText();

	protected abstract void setValueFromSlider(double value);
	
	protected abstract boolean setValueFromTextField(String str);

	protected abstract boolean shouldUseSlider();

	protected abstract void updateFieldValue();

	public void setSliderValue(double value) {
		if (slider != null)
			slider.setValueSilent(value);
	}

	public void setTextFieldValue(String text) {
		if (textField != null)
			textField.setText(prevTextFieldValue = text);
	}
	
	@Override
	protected int getSettingHeight() {
		return super.getSettingHeight() - CONTENT_MARGIN - (isSliderActive() ? GSSliderWidget.SLIDER_HEIGHT : TEXT_FIELD_HEIGHT);
	}
	
	@Override
	public void onSettingChanged() {
		super.onSettingChanged();
		
		updateFieldValue();
		
		if (slider != null)
			slider.active = setting.isEnabledInGui();
		if (textField != null)
			textField.active = setting.isEnabledInGui();
	}
	
	
	private boolean isSliderActive() {
		return (slider != null);
	}
}
