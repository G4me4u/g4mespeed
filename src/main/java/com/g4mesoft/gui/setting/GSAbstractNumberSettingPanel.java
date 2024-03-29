package com.g4mesoft.gui.setting;

import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.ui.panel.field.GSTextField;
import com.g4mesoft.ui.panel.legacy.GSButtonPanel;
import com.g4mesoft.ui.panel.legacy.GSSliderPanel;
import com.g4mesoft.ui.renderer.GSIRenderer2D;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public abstract class GSAbstractNumberSettingPanel<T extends GSSetting<?>> extends GSSettingPanel<T> {

	private static final int SETTING_HEIGHT = 16;
	private static final int TEXT_FIELD_HEIGHT = 20;
	private static final int TEXT_FIELD_MAX_WIDTH = 196;
	private static final Text SET_VALUE_TEXT = new TranslatableText("setting.button.set");
	
	private static final int TEXT_MAX_WIDTH = 96;
	
	protected GSSliderPanel slider;
	protected GSTextField textField;
	protected GSButtonPanel valueSetButton;

	protected GSButtonPanel resetButton;
	
	protected String prevTextFieldValue;
	
	public GSAbstractNumberSettingPanel(GSSettingCategory category, T setting) {
		super(category, setting);
	}
	
	@Override
	public void setPreferredBounds(int x, int y, int width) {
		int prefHeight;
		if (isSingleLine(width)) {
			prefHeight = Math.max(super.getPreferredHeight(), SETTING_HEIGHT + CONTENT_PADDING * 2);
		} else {
			prefHeight = getPreferredHeight();
		}
		
		setBounds(x, y, width, prefHeight);
	}
	
	private boolean isSingleLine(int width) {
		if (isSliderActive())
			return (width >= getPreferredWidth() + CONTENT_MARGIN * 2 + GSSliderPanel.MAX_WIDTH);
		return (width >= getPreferredWidth() + CONTENT_MARGIN * 3 + TEXT_FIELD_MAX_WIDTH + RESET_BUTTON_WIDTH);
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		int ty = (getSettingHeight() - renderer.getTextHeight()) / 2;
		renderer.drawText(nameText, CONTENT_PADDING, ty, getTextColor());
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
	public void onResized(int oldWidth, int oldHeight) {
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

		int sx, sy;
		if (isSingleLine(width)) {
			sx = width - CONTENT_PADDING - RESET_BUTTON_WIDTH - CONTENT_MARGIN - sw;
			sy = (height - sh) / 2;
		} else {
			sx = (width - sw) / 2;
			sy = height - CONTENT_PADDING - sh;
		}
		
		slider.setBounds(sx, sy, sw, sh);
		slider.setEnabled(isSettingEnabled());
	}
	
	private void updateTextFieldBounds() {
		int tw = Math.min(TEXT_FIELD_MAX_WIDTH, width - CONTENT_MARGIN - RESET_BUTTON_WIDTH - CONTENT_PADDING * 2);
		int tx, ty;
		if (isSingleLine(width)) {
			tx = width - CONTENT_PADDING - 2 * RESET_BUTTON_WIDTH - 2 * CONTENT_MARGIN - tw;
			ty = (height - TEXT_FIELD_HEIGHT) / 2;
		} else {
			tx = (width - tw - CONTENT_MARGIN - RESET_BUTTON_WIDTH) / 2;
			ty = height - CONTENT_PADDING - TEXT_FIELD_HEIGHT;
		}
		textField.setBounds(tx, ty, tw, TEXT_FIELD_HEIGHT);
		textField.setEditable(isSettingEnabled());

		int bx = textField.getX() + textField.getWidth() + CONTENT_MARGIN;
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

	public void setSliderText(Text text) {
		if (slider != null)
			slider.setText(text);
	}

	public void setTextFieldValue(String text) {
		if (textField != null)
			textField.setText(prevTextFieldValue = text);
	}
	
	@Override
	protected int getSettingHeight() {
		int h = super.getSettingHeight();
		if (!isSingleLine(width)) {
			h -= CONTENT_MARGIN;
			h -= (isSliderActive() ? GSSliderPanel.SLIDER_HEIGHT : TEXT_FIELD_HEIGHT);
		}
		return h;
	}
	
	@Override
	public void onSettingChanged() {
		super.onSettingChanged();
		
		updateFieldValue();
		
		if (slider != null)
			slider.setEnabled(isSettingEnabled());
		if (textField != null)
			textField.setEditable(isSettingEnabled());
		if (valueSetButton != null)
			valueSetButton.setEnabled(isSettingEnabled());
	}
	
	
	private boolean isSliderActive() {
		return (slider != null);
	}
}
