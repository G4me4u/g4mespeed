package com.g4mesoft.gui.setting;

import com.g4mesoft.gui.widget.GSToggleSwitchWidget;
import com.g4mesoft.setting.types.GSBooleanSetting;

public class GSBooleanSettingElementGUI extends GSSettingElementGUI<GSBooleanSetting> {

	private static final int SETTING_HEIGHT = Math.max(16, GSToggleSwitchWidget.TOGGLE_SWITCH_HEIGHT);

	private static final int TEXT_MAX_WIDTH = 96;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	
	private static final int TOGGLE_WIDTH = GSToggleSwitchWidget.TOGGLE_SWITCH_WIDTH;
	
	private GSToggleSwitchWidget switchWidget;
	
	public GSBooleanSettingElementGUI(GSSettingsGUI settingsGUI, GSBooleanSetting setting) {
		super(settingsGUI, setting);
		
		switchWidget = new GSToggleSwitchWidget(0, 0, setting.getValue(), (state) -> {
			setting.setValue(state);
		});
	}

	@Override
	public void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		super.renderTranslated(mouseX, mouseY, partialTicks);
		
		drawString(font, setting.getName(), CONTENT_PADDING, (height - font.fontHeight) / 2, TEXT_COLOR);
	}
	
	@Override
	public int getPreferredWidth() {
		return super.getPreferredWidth() + TEXT_MAX_WIDTH + TOGGLE_WIDTH + CONTENT_MARGIN * 2;
	}

	@Override
	public int getPreferredHeight() {
		return Math.max(super.getPreferredHeight(), SETTING_HEIGHT + CONTENT_PADDING * 2);
	}
	
	@Override
	public void init() {
		super.init();

		switchWidget.x = width - GSToggleSwitchWidget.TOGGLE_SWITCH_WIDTH - CONTENT_MARGIN - RESET_BUTTON_WIDTH - CONTENT_PADDING;
		switchWidget.y = (height - GSToggleSwitchWidget.TOGGLE_SWITCH_HEIGHT) / 2;
		
		addWidget(switchWidget);
	}
	
	@Override
	public void onSettingChanged() {
		switchWidget.setEnabledSilent(setting.getValue());
	}

	@Override
	protected void resetSetting() {
		setting.reset();
	}
}
