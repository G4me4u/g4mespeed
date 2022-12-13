package com.g4mesoft.gui.setting;

import com.g4mesoft.panel.legend.GSToggleSwitchPanel;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSBooleanSetting;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GSBooleanSettingPanel extends GSSettingPanel<GSBooleanSetting> {

	private static final int SETTING_HEIGHT = Math.max(16, GSToggleSwitchPanel.SWITCH_HEIGHT);
	private static final int TEXT_MAX_WIDTH = 140;
	private static final int TOGGLE_WIDTH = GSToggleSwitchPanel.SWITCH_WIDTH;

	private static final Text ENABLED_TEXT = Text.literal("enabled").formatted(Formatting.GREEN);
	private static final Text DISABLED_TEXT = Text.literal("disabled").formatted(Formatting.RED);
	
	private final GSToggleSwitchPanel switchWidget;
	
	public GSBooleanSettingPanel(GSSettingCategory category, GSBooleanSetting setting) {
		super(category, setting);
		
		switchWidget = new GSToggleSwitchPanel(this::updateSettingValue, setting.getValue());
		add(switchWidget);
	}
	
	public void updateSettingValue() {
		setting.setValue(switchWidget.isToggled());
	}

	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);
		
		int ty = (getSettingHeight() - renderer.getTextHeight()) / 2;
		renderer.drawText(nameText, CONTENT_PADDING, ty, getTextColor());
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
	public void onResized(int oldWidth, int oldHeight) {
		int sx = width - TOGGLE_WIDTH - CONTENT_MARGIN - RESET_BUTTON_WIDTH - CONTENT_PADDING;
		int sy = (height - GSToggleSwitchPanel.SWITCH_HEIGHT) / 2;
		switchWidget.setPreferredBounds(sx, sy);
		switchWidget.setEnabled(isSettingEnabled());
	}
	
	@Override
	public void onSettingChanged() {
		super.onSettingChanged();
		
		switchWidget.setToggled(setting.getValue());
		switchWidget.setEnabled(isSettingEnabled());
	}

	@Override
	public Text getFormattedDefault() {
		return (setting.getDefaultValue() ? ENABLED_TEXT : DISABLED_TEXT);
	}
}
