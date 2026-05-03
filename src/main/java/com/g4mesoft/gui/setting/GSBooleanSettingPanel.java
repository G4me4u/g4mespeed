package com.g4mesoft.gui.setting;

import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSBooleanSetting;
import com.g4mesoft.ui.panel.legacy.GSToggleSwitchPanel;
import com.g4mesoft.ui.renderer.GSIRenderer2D;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class GSBooleanSettingPanel extends GSSettingPanel<GSBooleanSetting> {

	private static final int SETTING_HEIGHT = Math.max(16, GSToggleSwitchPanel.SWITCH_HEIGHT);
	private static final int TEXT_MAX_WIDTH = 140;
	private static final int TOGGLE_WIDTH = GSToggleSwitchPanel.SWITCH_WIDTH;

	private static final Component ENABLED_TEXT = Component.translatable("setting.boolean.enabled").withStyle(ChatFormatting.GREEN);
	private static final Component DISABLED_TEXT = Component.translatable("setting.boolean.disabled").withStyle(ChatFormatting.RED);
	
	private final GSToggleSwitchPanel switchWidget;
	
	public GSBooleanSettingPanel(GSSettingCategory category, GSBooleanSetting setting) {
		super(category, setting);
		
		switchWidget = new GSToggleSwitchPanel(this::updateSettingValue, setting.get());
		add(switchWidget);
	}
	
	public void updateSettingValue() {
		setting.set(switchWidget.isToggled());
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
		
		switchWidget.setToggled(setting.get());
		switchWidget.setEnabled(isSettingEnabled());
	}

	@Override
	public Component getFormattedDefault() {
		return (setting.getDefault() ? ENABLED_TEXT : DISABLED_TEXT);
	}
}
