package com.g4mesoft.gui.setting;

import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.legend.GSButtonPanel;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

/* TODO: rewrite this using the modern panel API */
public abstract class GSSettingPanel<T extends GSSetting<?>> extends GSParentPanel {

	public static final int HOVERED_BACKGROUND = 0x80000000;//0x66EDEDFF;
	
	protected static final int CONTENT_PADDING = 2;
	protected static final int CONTENT_MARGIN = 2;
	
	protected static final int RESET_BUTTON_WIDTH = 48;
	protected static final int RESET_BUTTON_HEIGHT = 20;
	protected static final Text RESET_TEXT = new TranslatableText("setting.button.reset");

	private static final int ENABLED_TEXT_COLOR = 0xFFFFFFFF;
	private static final int DISABLED_TEXT_COLOR = 0xFFAAAAAA;
	
	protected final GSSettingCategory category;
	protected final T setting;
	
	protected final TranslatableText nameText;
	
	private final GSButtonPanel resetButton;
	
	public GSSettingPanel(GSSettingCategory category, T setting) {
		this.category = category;
		this.setting = setting;
		
		nameText = new TranslatableText("setting." + category.getName() + "." + setting.getName());
		
		resetButton = new GSButtonPanel(RESET_TEXT, this::resetSetting);
		add(resetButton);
	}
	
	public void setPreferredBounds(int x, int y, int width) {
		setBounds(x, y, width, getPreferredHeight());
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		if (renderer.isMouseInside(0, 0, width, height))
			renderer.fillRect(0, 0, width, height, HOVERED_BACKGROUND);
		
		super.render(renderer);
	}
	
	public int getTextColor() {
		return isSettingEnabled() ? ENABLED_TEXT_COLOR : DISABLED_TEXT_COLOR;
	}
	
	@Override
	public void layout() {
		int x = width - CONTENT_PADDING - RESET_BUTTON_WIDTH;
		int y = (getSettingHeight() - RESET_BUTTON_HEIGHT) / 2;
		resetButton.setBounds(x, y, RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT);
		
		updateResetActive();
	}

	protected void resetSetting() {
		if (isSettingEnabled())
			setting.reset();
	}
	
	public void onSettingChanged() {
		updateResetActive();
	}
	
	private void updateResetActive() {
		resetButton.setEnabled(!setting.isDefaultValue() && isSettingEnabled());
	}

	public abstract Text getFormattedDefault();

	protected boolean isSettingEnabled() {
		return setting.isEnabledInGui() && setting.isAllowedChange();
	}
	
	protected int getSettingHeight() {
		return height;
	}
	
	public int getPreferredWidth() {
		return RESET_BUTTON_WIDTH + CONTENT_PADDING * 2;
	}

	public int getPreferredHeight() {
		return RESET_BUTTON_HEIGHT + CONTENT_PADDING * 2;
	}
	
	public TranslatableText getSettingNameText() {
		return nameText;
	}

	public GSSetting<?> getSetting() {
		return setting;
	}
}
