package com.g4mesoft.gui.setting;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.gui.GSParentGUI;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;

public abstract class GSSettingElementGUI<T extends GSSetting<?>> extends GSParentGUI {

	public static final int HOVERED_BACKGROUND = 0x80000000;//0x66EDEDFF;
	
	protected static final int CONTENT_PADDING = 2;
	protected static final int CONTENT_MARGIN = 2;
	
	protected static final int RESET_BUTTON_WIDTH = 48;
	protected static final int RESET_BUTTON_HEIGHT = 20;
	protected static final String RESET_TEXT = "setting.button.reset";

	private static final int ENABLED_TEXT_COLOR = 0xFFFFFFFF;
	private static final int DISABLED_TEXT_COLOR = 0xFFAAAAAA;
	
	protected final GSSettingsGUI settingsGUI;
	protected final T setting;
	protected final GSSettingCategory category;
	
	protected final String settingTranslationName;
	
	protected final List<Drawable> drawableChildren;
	
	private ButtonWidget resetButton;
	
	public GSSettingElementGUI(GSSettingsGUI settingsGUI, T setting, GSSettingCategory category) {
		super(NarratorManager.EMPTY);
		
		this.settingsGUI = settingsGUI;
		this.setting = setting;
		this.category = category;
		
		settingTranslationName = "setting." + category.getName() + "." + setting.getName();
		
		drawableChildren = new ArrayList<Drawable>();
	
		resetButton = null;
	}
	
	@Override
	public void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		if (mouseX >= 0 && mouseY >= 0 && mouseX < width && mouseY < height)
			fill(0, 0, width, height, HOVERED_BACKGROUND);
		
		super.renderTranslated(mouseX, mouseY, partialTicks);
	
		for (Drawable child : drawableChildren)
			child.render(mouseX, mouseY, partialTicks);
	}
	
	public int getTextColor() {
		return setting.isEnabledInGui() ? ENABLED_TEXT_COLOR : DISABLED_TEXT_COLOR;
	}
	
	public void addWidget(Element element) {
		children.add(element);
		
		if (element instanceof Drawable)
			drawableChildren.add((Drawable)element);
	}
	
	@Override
	public void init() {
		super.init();
		
		drawableChildren.clear();
		
		resetButton = createResetButton();
		updateResetActive();
		addWidget(resetButton);
	}

	protected void resetSetting() {
		if (setting.isEnabledInGui())
			setting.reset();
	}
	
	public void onSettingChanged() {
		updateResetActive();
	}
	
	private void updateResetActive() {
		if (resetButton != null)
			resetButton.active = !setting.isDefaultValue() && setting.isEnabledInGui();
	}

	public abstract String getFormattedDefault();

	@Override
	protected boolean mouseScrolledTranslated(double mouseX, double mouseY, double scroll) {
		return false;
	}
	
	protected int getSettingHeight() {
		return height;
	}
	
	public ButtonWidget createResetButton() {
		int x = width - CONTENT_PADDING - RESET_BUTTON_WIDTH;
		int y = (getSettingHeight() - RESET_BUTTON_HEIGHT) / 2;
		
		String resetText = getTranslationModule().getTranslation(RESET_TEXT);
		return new ButtonWidget(x, y, RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT, resetText, (button) -> {
			resetSetting();
		});
	}
	
	public int getPreferredWidth() {
		return RESET_BUTTON_WIDTH + CONTENT_PADDING * 2;
	}

	public int getPreferredHeight() {
		return RESET_BUTTON_HEIGHT + CONTENT_PADDING * 2;
	}
	
	public String getSettingTranslationName() {
		return settingTranslationName;
	}
}
