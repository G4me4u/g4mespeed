package com.g4mesoft.gui.setting;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.g4mesoft.gui.GSScrollablePanel;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.g4mesoft.setting.GSISettingChangeListener;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.GSSettingManager;
import com.g4mesoft.setting.GSSettingMap;
import com.g4mesoft.setting.types.GSBooleanSetting;
import com.g4mesoft.setting.types.GSFloatSetting;
import com.g4mesoft.setting.types.GSIntegerSetting;
import com.g4mesoft.util.GSMathUtils;
import com.mojang.blaze3d.platform.GlStateManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class GSSettingsGUI extends GSScrollablePanel implements GSISettingChangeListener {

	private static final int SETTING_CATEGORY_MARGIN = 5;
	private static final int CATEGORY_TITLE_MARGIN_BOTTOM = 2;
	private static final int CATEGORY_TITLE_COLOR = 0xFFFFFFFF;
	
	private static final int DESC_BACKGROUND_COLOR = GSSettingElementGUI.HOVERED_BACKGROUND;
	private static final int DESC_LINE_SPACING = 5;
	private static final int DESC_LINE_MARGIN = 10;
	private static final int DESC_TEXT_COLOR = 0xFFFFFFFF;
	
	private static final float DESC_ANIMATION_TIME = 500.0f;
	
	private final Map<GSSettingCategory, GSSettingCategoryElement> settingCategories;
	private int settingsWidth;
	private int contentHeight;
	private boolean layoutChanged;
	
	private GSSettingElementGUI<?> hoveredElement;
	private List<String> descLines;
	private int startDescHeight;
	private int targetDescHeight;

	private long descAnimStart;
	
	public GSSettingsGUI(GSSettingManager settingManager) {
		this.settingCategories = new LinkedHashMap<GSSettingCategory, GSSettingCategoryElement>();

		for (GSSettingMap settingCategory : settingManager.getSettings()) {
			for (GSSetting<?> setting : settingCategory.getSettings()) {
				addSettingElement(settingCategory.getCategory(), setting);
			}
		}
		
		settingManager.addChangeListener(this);
	}

	private void addSettingElement(GSSettingCategory category, GSSetting<?> setting) {
		if (setting.isActive() && setting.isVisibleInGUI()) {
			GSSettingCategoryElement categoryElement = settingCategories.get(category);
			if (categoryElement == null) {
				categoryElement = new GSSettingCategoryElement(category);
				settingCategories.put(category, categoryElement);
			}
			
			categoryElement.addSetting(setting);
			
			layoutChanged = true;
		}
	}
	
	@Override
	public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
		GSSettingCategoryElement categoryElement = settingCategories.get(category);
		if (categoryElement != null)
			categoryElement.onSettingChanged(setting);
	}
	
	@Override
	public void onSettingAdded(GSSettingCategory category, GSSetting<?> setting) {
		addSettingElement(category, setting);
	}

	@Override
	public void onSettingRemoved(GSSettingCategory category, GSSetting<?> setting) {
		GSSettingCategoryElement categoryElement = settingCategories.get(category);
		if (categoryElement != null) {
			categoryElement.removeSetting(setting);
			
			if (categoryElement.isEmpty())
				settingCategories.remove(category);
		}

		layoutChanged = true;
	}

	private void layoutSettingElements() {
		clearChildren();
		
		settingsWidth = width / 2;
		for (GSSettingCategoryElement element : settingCategories.values()) {
			int minElementWidth = element.getMinimumWidth();
			if (minElementWidth > settingsWidth)
				settingsWidth = minElementWidth;
		}
		
		int y = 0;
		for (GSSettingCategoryElement element : settingCategories.values()) {
			y += SETTING_CATEGORY_MARGIN;
			y = element.layoutElements(0, y, settingsWidth);
			y += SETTING_CATEGORY_MARGIN;
		}
		
		contentHeight = y;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		for (GSSettingCategoryElement element : settingCategories.values())
			element.tick();
	}
	
	@Override
	public void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		if (layoutChanged) {
			layoutSettingElements();
			layoutChanged = false;
		}
		
		GSSettingElementGUI<?> hoveredElement = null;
		for (GSSettingCategoryElement element : settingCategories.values()) {
			if (hoveredElement == null)
				hoveredElement = element.getHoveredElement(mouseX, mouseY);
			
			element.render(mouseX, mouseY, partialTicks);
		}
		
		super.renderTranslated(mouseX, mouseY, partialTicks);
		
		if (hoveredElement != this.hoveredElement) {
			this.hoveredElement = hoveredElement;
			
			if (hoveredElement != null) {
				int descTextWidth = width - settingsWidth - DESC_LINE_MARGIN * 2;
				
				GSTranslationModule translationModule = getTranslationModule();
				String desc = translationModule.getTranslation(hoveredElement.getSettingTranslationName() + ".desc");
				String def = translationModule.getFormattedTranslation("setting.default", hoveredElement.getFormattedDefault());
				descLines = splitToLines(desc + " " + def, descTextWidth);
				
				int numLines = descLines.size();
				int minimumDescHeight = numLines * font.fontHeight + (numLines - 1) * DESC_LINE_SPACING + DESC_LINE_MARGIN * 2;
				
				targetDescHeight = Math.max(minimumDescHeight, hoveredElement.height);
				startDescHeight = hoveredElement.height;
				
				descAnimStart = Util.getMeasuringTimeMs();
			} else {
				descLines.clear();
				descLines = null;
			}
		}
		
		if (this.hoveredElement != null)
			renderHoveredDesc(this.hoveredElement, partialTicks);
	}
	
	private void renderHoveredDesc(GSSettingElementGUI<?> hoveredElement, float partialTicks) {
		long delta = Util.getMeasuringTimeMs() - descAnimStart;

		float progress = Math.min(1.0f, delta / DESC_ANIMATION_TIME);
		
		progress = 1.0f - (float)Math.pow(1.0 - progress, 3.0);
		
		int descHeight = startDescHeight + Math.round((targetDescHeight - startDescHeight) * progress);
		int descWidth = width - settingsWidth;
		
		int descX = settingsWidth;
		
		int scrollOffset = getScrollOffset();
		int descY = GSMathUtils.clamp(hoveredElement.y, scrollOffset, height + scrollOffset - descHeight);
		
		if (descWidth > 0 && descHeight > 0 && targetDescHeight != 0) {
			fill(descX, descY, descX + descWidth, descY + descHeight, DESC_BACKGROUND_COLOR);
			
			int alpha = GSMathUtils.clamp((int)(progress * 128.0f + 127.0f), 0, 255) << 24;
			
			int y = descY + DESC_LINE_MARGIN;
			for (String line : descLines) {
				if (y + font.fontHeight > descY + descHeight)
					break;
				
				GlStateManager.enableBlend();
				GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				drawString(font, line, descX + DESC_LINE_MARGIN, y, (DESC_TEXT_COLOR & 0xFFFFFF) | alpha);
				GlStateManager.disableBlend();
				y += font.fontHeight + DESC_LINE_SPACING;
			}
		}
	}

	@Override
	public void init() {
		super.init();
		layoutChanged = true;
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		layoutChanged = true;
	}
	
	@Override
	protected int getScrollableHeight() {
		return contentHeight;
	}
	
	private class GSSettingCategoryElement {
		
		private final GSSettingCategory category;
		private final String title;
		
		private final List<GSSettingElementGUI<?>> settings;
		
		private int x;
		private int y;
		private int width;
		private int height;
		
		public GSSettingCategoryElement(GSSettingCategory category) {
			this.category = category;
			
			title = "setting." + category.getName();
			
			settings = new LinkedList<GSSettingElementGUI<?>>();
		}
		
		public int getMinimumWidth() {
			int minimumWidth = 0;
			for (GSSettingElementGUI<?> element : settings) {
				if (element.getPreferredWidth() > minimumWidth)
					minimumWidth = element.getPreferredWidth();
			}
			return minimumWidth;
		}

		public void addSetting(GSSetting<?> setting) {
			if (setting instanceof GSBooleanSetting) {
				settings.add(new GSBooleanSettingElementGUI(GSSettingsGUI.this, (GSBooleanSetting)setting, category));
			} else if (setting instanceof GSFloatSetting) {
				settings.add(new GSFloatSettingElementGUI(GSSettingsGUI.this, (GSFloatSetting)setting, category));
			} else if (setting instanceof GSIntegerSetting) {
				settings.add(new GSIntegerSettingElementGUI(GSSettingsGUI.this, (GSIntegerSetting)setting, category));
			}
		}

		public void removeSetting(GSSetting<?> setting) {
			Iterator<GSSettingElementGUI<?>> settingItr = settings.iterator();
			while (settingItr.hasNext()) {
				if (settingItr.next().setting == setting)
					settingItr.remove();
			}
		}
		
		public void onSettingChanged(GSSetting<?> setting) {
			for (GSSettingElementGUI<?> element : settings) {
				if (element.setting.getName().equals(setting.getName())) {
					element.onSettingChanged();
					break;
				}
			}
		}
		
		public int layoutElements(int x, int y, int width) {
			this.x = x;
			this.y = y;

			this.width = width;
			
			y += font.fontHeight;
			y += CATEGORY_TITLE_MARGIN_BOTTOM;

			for (GSSettingElementGUI<?> element : settings) {
				element.initBounds(client, x, y, width, element.getPreferredHeight());
				addPanel(element);
			
				y += element.height;
			}
			
			this.height = y - this.y;
			
			return y;
		}
		
		public GSSettingElementGUI<?> getHoveredElement(int mouseX, int mouseY) {
			if (!isHovered(mouseX, mouseY))
				return null;

			for (GSSettingElementGUI<?> element : settings) {
				if (element.isMouseOver(mouseX, mouseY))
					return element;
			}
			
			return null;
		}
		
		public boolean isHovered(int mouseX, int mouseY) {
			return mouseX >= this.x && mouseX < this.x + this.width &&
			       mouseY >= this.y && mouseY < this.y + this.height;
		}
		
		public void tick() {
			for (GSSettingElementGUI<?> element : settings)
				element.tick();
		}

		public void render(int mouseX, int mouseY, float partialTicks) {
			String title = getTranslationModule().getTranslation(this.title);
			drawCenteredString(GSSettingsGUI.this.font, title, x + width / 2, y, CATEGORY_TITLE_COLOR);
		}
		
		public boolean isEmpty() {
			return settings.isEmpty();
		}
	}
}
