package com.g4mesoft.gui.setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.g4mesoft.gui.GSScreen;
import com.g4mesoft.setting.GSISettingChangeListener;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.GSSettingManager;
import com.g4mesoft.setting.GSSettingMap;
import com.g4mesoft.setting.types.GSBooleanSetting;
import com.g4mesoft.util.GSMathUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.NarratorManager;

@Environment(EnvType.CLIENT)
public class GSSettingsGUI extends GSScreen implements GSISettingChangeListener {

	private static final int SETTING_CATEGORY_MARGIN = 10;
	private static final int CATEGORY_TITLE_MARGIN_BOTTOM = 5;
	private static final int CATEGORY_TITLE_COLOR = 0xFFFFFFFF;
	
	private static final int DESC_BACKGROUND_COLOR = GSSettingElementGUI.HOVERED_BACKGROUND;
	private static final float DESC_HEIGHT_EASING = 0.5f;
	private static final int DESC_LINE_SPACING = 5;
	private static final int DESC_LINE_MARGIN = 10;
	private static final int DESC_TEXT_COLOR = 0xFFFFFFFF;
	
	private final Map<GSSettingCategory, GSSettingCategoryElement> settingCategories;
	private int settingsWidth;
	private boolean layoutChanged;
	
	private GSSettingElementGUI<?> hoveredElement;
	private List<String> descLines;
	private int descHeight;
	private int prevDescHeight;
	private int targetDescHeight;
	
	public GSSettingsGUI(GSSettingManager settingManager) {
		super(NarratorManager.EMPTY);
		
		this.settingCategories = new HashMap<GSSettingCategory, GSSettingCategoryElement>();

		for (GSSettingMap settingCategory : settingManager.getSettings()) {
			for (GSSetting<?> setting : settingCategory.getSettings()) {
				addSettingElement(settingCategory.getCategory(), setting);
			}
		}
		
		settingManager.addChangeListener(this);
	}

	private void addSettingElement(GSSettingCategory category, GSSetting<?> setting) {
		GSSettingCategoryElement categoryElement = settingCategories.get(category);
		if (categoryElement == null) {
			categoryElement = new GSSettingCategoryElement(category);
			settingCategories.put(category, categoryElement);
		}
		
		categoryElement.addSetting(setting);
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

	private void layoutSettingElements() {
		children.clear();

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
	}
	
	@Override
	public void tick() {
		super.tick();
		
		for (GSSettingCategoryElement element : settingCategories.values())
			element.tick();
		
		if (hoveredElement != null) {
			prevDescHeight = descHeight;
			int newDescHeight = descHeight + (int)((targetDescHeight - descHeight) * DESC_HEIGHT_EASING);
			if (newDescHeight == descHeight) {
				descHeight = targetDescHeight;
			} else {
				descHeight = newDescHeight;
			}
		}
	}
	
	@Override
	public void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		super.renderTranslated(mouseX, mouseY, partialTicks);
	
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
		
		if (hoveredElement != this.hoveredElement) {
			this.hoveredElement = hoveredElement;
			
			if (hoveredElement != null) {
				int descTextWidth = width - settingsWidth - DESC_LINE_MARGIN * 2;
				
				String desc = "Some very very long description that will in most cases probably need to be split to multiple lines. But I guess in some cases it has to be longer...";
				descLines = splitToLines(desc, descTextWidth);
				
				int numLines = descLines.size();
				int minimumDescHeight = numLines * font.fontHeight + (numLines - 1) * DESC_LINE_SPACING + DESC_LINE_MARGIN * 2;
				
				targetDescHeight = Math.max(minimumDescHeight, hoveredElement.height);
				descHeight = prevDescHeight = hoveredElement.height;
			} else {
				descLines.clear();
				descLines = null;
			}
		}
		
		if (this.hoveredElement != null)
			renderHoveredDesc(this.hoveredElement, partialTicks);
	}
	
	private void renderHoveredDesc(GSSettingElementGUI<?> hoveredElement, float partialTicks) {
		int descHeight = prevDescHeight + (int)((this.descHeight - prevDescHeight) * partialTicks);
		int descWidth = width - settingsWidth;
		
		int descX = settingsWidth;
		int descY = GSMathUtils.clamp(hoveredElement.getY(), 0, height);
		
		if (descWidth > 0 && descHeight > 0 && targetDescHeight != 0) {
			fill(descX, descY, descX + descWidth, descY + descHeight, DESC_BACKGROUND_COLOR);
			
			int alpha = GSMathUtils.clamp((int)((float)descHeight / targetDescHeight * 255.0f), 0, 255) << 24;
			
			int y = descY + DESC_LINE_MARGIN;
			for (String line : descLines) {
				if (y + font.fontHeight > descY + descHeight)
					break;
				
				GlStateManager.enableBlend();
				GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
				GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
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
	
	private class GSSettingCategoryElement {
		
		private final GSSettingCategory category;
		
		private final List<GSSettingElementGUI<?>> settings;
		
		private int x;
		private int y;
		private int width;
		private int height;
		
		public GSSettingCategoryElement(GSSettingCategory category) {
			this.category = category;
			
			settings = new ArrayList<GSSettingElementGUI<?>>();
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
				settings.add(new GSBooleanSettingElementGUI(GSSettingsGUI.this, (GSBooleanSetting)setting));
			}
		}
		
		public void onSettingChanged(GSSetting<?> setting) {
			for (GSSettingElementGUI<?> element : settings) {
				if (element.setting.getIdentifier() == setting.getIdentifier()) {
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
				element.initBounds(minecraft, x, y, width, element.getPreferredHeight());
				children.add(element);
			
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
			drawCenteredString(GSSettingsGUI.this.font, category.getName(), x + width / 2, y, CATEGORY_TITLE_COLOR);
			
			for (GSSettingElementGUI<?> element : settings)
				element.render(mouseX, mouseY, partialTicks);
		}
	}
}
