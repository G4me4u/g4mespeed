package com.g4mesoft.gui.setting;

import com.g4mesoft.gui.action.GSButtonPanel;
import com.g4mesoft.gui.action.GSSliderPanel;
import com.g4mesoft.gui.renderer.GSIRenderer2D;
import com.g4mesoft.gui.text.GSTextField;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;

public abstract class GSNumberSettingElementGUI<T extends GSSetting<?>> extends GSSettingElementGUI<T> {

	private static final int SETTING_HEIGHT = 16;
	private static final int TEXT_FIELD_HEIGHT = 20;
	private static final int TEXT_FIELD_MAX_WIDTH = 128;
	private static final String SET_VALUE_TEXT = "setting.button.set";
	
	private static final int TEXT_MAX_WIDTH = 96;
	
	protected GSSliderPanel slider;
	protected GSTextField textField;
	protected GSButtonPanel valueSetButton;

	protected GSButtonPanel resetButton;
	
	protected String prevTextFieldValue;
	
	public GSNumberSettingElementGUI(GSSettingsGUI settingsGUI, T setting, GSSettingCategory category) {
		super(settingsGUI, setting, category);
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
		return super.getPreferredWidth() + TEXT_MAX_WIDTH + CONTENT_MARGIN * 2;
	}

	@Override
	public int getPreferredHeight() {
		int prefHeight = Math.max(super.getPreferredHeight(), SETTING_HEIGHT + CONTENT_PADDING * 2);
		if (slider != null) {
			prefHeight += GSSliderPanel.SLIDER_HEIGHT + CONTENT_MARGIN;
		} else {
			prefHeight += TEXT_FIELD_HEIGHT + CONTENT_MARGIN;
		}
		return prefHeight;
	}
	
	@Override
	public void onBoundsChanged() {
		super.onBoundsChanged();
		
		if (shouldUseSlider()) {
			if (textField != null) {
				remove(textField);
				textField = null;
			}

			if (valueSetButton != null) {
				remove(valueSetButton);
				valueSetButton = null;
			}
			
			if (slider == null) {
				slider = new GSSliderPanel("", this::onValueChanged);
				add(slider);
			}
			
			updateSliderBounds();
		} else {
			if (slider != null) {
				remove(slider);
				slider = null;
			}
			
			if (textField == null) {
				textField = new GSTextField();
				add(textField);
			}

			if (valueSetButton == null) {
				valueSetButton = new GSButtonPanel(SET_VALUE_TEXT, this::onValueChanged);
				
				add(valueSetButton);
			}
			
			updateTextFieldBounds();
		}
		
		updateFieldValue();
	}
	
	private void updateSliderBounds() {
		int sw = Math.min(GSSliderPanel.MAX_WIDTH, width - CONTENT_PADDING * 2);
		int sh = GSSliderPanel.SLIDER_HEIGHT;
		int sx = (width - sw) / 2;
		int sy = height - CONTENT_PADDING - sh;
		
		slider.setBounds(sx, sy, sw, sh);
		slider.setEnabled(setting.isEnabledInGui());
	}
	
	private void updateTextFieldBounds() {
		int tw = Math.min(TEXT_FIELD_MAX_WIDTH, width - CONTENT_MARGIN - RESET_BUTTON_WIDTH - CONTENT_PADDING * 2);
		int ty = height - CONTENT_PADDING - TEXT_FIELD_HEIGHT;
		
		textField.setBounds(CONTENT_PADDING, ty, tw, TEXT_FIELD_HEIGHT);
		textField.setEditable(setting.isEnabledInGui());

		int bx = width - CONTENT_PADDING - RESET_BUTTON_WIDTH;
		int by = ty + (TEXT_FIELD_HEIGHT - RESET_BUTTON_HEIGHT) / 2;

		valueSetButton.setBounds(bx, by, RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT);
	}
	
	private void onValueChanged() {
		if (isSliderActive()) {
			setValueFromSlider(slider.getValue());
			updateFieldValue();
		} else {
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
		}
	}
	
	protected abstract void setValueFromSlider(float value);
	
	protected abstract boolean setValueFromTextField(String str);

	protected abstract boolean shouldUseSlider();

	protected abstract void updateFieldValue();

	public void setSliderValue(float value) {
		if (slider != null)
			slider.setValue(value);
	}
	
	public void setSliderText(String text) {
		if (slider != null)
			slider.setLiteralText(text);
	}

	public void setTextFieldValue(String text) {
		if (textField != null)
			textField.setText(prevTextFieldValue = text);
	}
	
	@Override
	protected int getSettingHeight() {
		return super.getSettingHeight() - CONTENT_MARGIN - (isSliderActive() ? GSSliderPanel.SLIDER_HEIGHT : TEXT_FIELD_HEIGHT);
	}
	
	@Override
	public void onSettingChanged() {
		super.onSettingChanged();
		
		updateFieldValue();
		
		if (slider != null)
			slider.setEnabled(setting.isEnabledInGui());
		if (textField != null)
			textField.setEditable(setting.isEnabledInGui());
		if (valueSetButton != null)
			valueSetButton.setEnabled(setting.isEnabledInGui());
	}
	
	
	private boolean isSliderActive() {
		return (slider != null);
	}
}
