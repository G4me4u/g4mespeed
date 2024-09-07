package com.g4mesoft.gui.setting;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.g4mesoft.setting.GSISettingChangeListener;
import com.g4mesoft.setting.GSISettingPanelSupplier;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.GSSettingManager;
import com.g4mesoft.setting.GSSettingMap;
import com.g4mesoft.ui.panel.GSDimension;
import com.g4mesoft.ui.panel.GSPanelContext;
import com.g4mesoft.ui.panel.GSPanelUtil;
import com.g4mesoft.ui.panel.GSParentPanel;
import com.g4mesoft.ui.panel.scroll.GSIScrollable;
import com.g4mesoft.ui.renderer.GSIRenderer2D;
import com.g4mesoft.ui.util.GSMathUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class GSSettingsGUI extends GSParentPanel implements GSIScrollable, GSISettingChangeListener {

	private static final int SETTING_CATEGORY_MARGIN = 5;
	private static final int CATEGORY_TITLE_MARGIN_BOTTOM = 2;
	private static final int CATEGORY_TITLE_COLOR = 0xFFFFFFFF;
	
	private static final int DESC_BACKGROUND_COLOR = GSSettingPanel.HOVERED_BACKGROUND;
	private static final int DESC_LINE_SPACING = 5;
	private static final int DESC_LINE_MARGIN = 10;
	private static final int DESC_TEXT_COLOR = 0xFFFFFFFF;
	
	private static final float DESC_ANIMATION_TIME = 500.0f;
	
	private final Map<GSSettingCategory, GSSettingCategoryElement> settingCategories;
	private int settingsWidth;
	
	private GSSettingPanel<?> hoveredElement;
	private List<OrderedText> descLines;
	private int startDescHeight;
	private int targetDescHeight;

	private long descAnimStart;
	
	public GSSettingsGUI(GSSettingManager settingManager) {
		this.settingCategories = new LinkedHashMap<>();

		for (GSSettingMap settingCategory : settingManager.getSettings()) {
			for (GSSetting<?> setting : settingCategory.getSettings()) {
				addSettingElement(settingCategory.getCategory(), setting);
			}
		}
		
		settingManager.addChangeListener(this);
	}

	private <T extends GSSetting<?>> void addSettingElement(GSSettingCategory category, T setting) {
		if (setting.isActive() && setting.isVisibleInGui()) {
			GSISettingPanelSupplier<T> supplier = GSSettingManager.getPanelSupplier(setting);

			if (supplier != null) {
				GSSettingCategoryElement categoryElement = settingCategories.get(category);
				if (categoryElement == null) {
					categoryElement = new GSSettingCategoryElement(category);
					settingCategories.put(category, categoryElement);
				}
				
				categoryElement.addSettingPanel(supplier.create(category, setting));
				invalidate();
			}
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

		invalidate();
	}

	@Override
	public void layout() {
		settingsWidth = width / 2;
		for (GSSettingCategoryElement element : settingCategories.values()) {
			int minElementWidth = element.getPreferredWidth();
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
	public void render(GSIRenderer2D renderer) {
		int mouseX = renderer.getMouseX();
		int mouseY = renderer.getMouseY();
		
		GSSettingPanel<?> hoveredElement = null;
		for (GSSettingCategoryElement element : settingCategories.values()) {
			if (hoveredElement == null)
				hoveredElement = element.getHoveredElement(mouseX, mouseY);
			
			element.render(renderer);
		}
		
		super.render(renderer);
		
		if (hoveredElement != this.hoveredElement) {
			this.hoveredElement = hoveredElement;
			
			if (hoveredElement != null) {
				int descTextWidth = width - settingsWidth - DESC_LINE_MARGIN * 2;
				
				MutableText desc = new TranslatableText(hoveredElement.getSettingNameText().getKey() + ".desc");
				Text def = new TranslatableText("setting.default", hoveredElement.getFormattedDefault());
				descLines = renderer.splitToLines(desc.append(" ").append(def), descTextWidth);
				
				int lineCount = descLines.size();
				int minimumDescHeight = lineCount * renderer.getTextHeight() + (lineCount - 1) * DESC_LINE_SPACING + DESC_LINE_MARGIN * 2;
				
				targetDescHeight = Math.max(minimumDescHeight, hoveredElement.height);
				startDescHeight = hoveredElement.height;
				
				descAnimStart = Util.getMeasuringTimeMs();
			} else {
				descLines = null;
			}
		}
		
		if (this.hoveredElement != null)
			renderHoveredDesc(renderer, this.hoveredElement);
	}
	
	private void renderHoveredDesc(GSIRenderer2D renderer, GSSettingPanel<?> hoveredElement) {
		long delta = Util.getMeasuringTimeMs() - descAnimStart;

		float progress = Math.min(1.0f, delta / DESC_ANIMATION_TIME);
		
		progress = 1.0f - (float)Math.pow(1.0 - progress, 3.0);
		
		int descHeight = startDescHeight + Math.round((targetDescHeight - startDescHeight) * progress);
		int descWidth = width - settingsWidth;
		
		int descX = settingsWidth;
		
		int scrollOffset = GSPanelUtil.getScrollY(this);
		int viewHeight = GSPanelUtil.getViewportSize(this).getHeight();
		int descY = GSMathUtil.clamp(hoveredElement.y, scrollOffset, viewHeight + scrollOffset - descHeight);
		
		if (descWidth > 0 && descHeight > 0 && targetDescHeight != 0) {
			renderer.fillRect(descX, descY, descWidth, descHeight, DESC_BACKGROUND_COLOR);
			
			int alpha = GSMathUtil.clamp((int)(progress * 128.0f + 127.0f), 0, 255) << 24;
			
			int y = descY + DESC_LINE_MARGIN;
			for (OrderedText line : descLines) {
				if (y + renderer.getTextHeight() > descY + descHeight)
					break;
				
				renderer.drawText(line, descX + DESC_LINE_MARGIN, y, (DESC_TEXT_COLOR & 0xFFFFFF) | alpha);

				y += renderer.getTextHeight() + DESC_LINE_SPACING;
			}
		}
	}

	@Override
	protected GSDimension calculatePreferredSize() {
		int w = 0, h = 0;
		for (GSSettingCategoryElement element : settingCategories.values()) {
			w = Math.max(w, element.getPreferredWidth());
			h += element.getPreferredHeight() + 2 * SETTING_CATEGORY_MARGIN;
		}
		return new GSDimension(w, h);
	}
	
	@Override
	public boolean isScrollableWidthFilled() {
		return true;
	}
	
	private class GSSettingCategoryElement {
		
		private final Text titleText;
		
		private final List<GSSettingPanel<?>> settings;
		
		private int x;
		private int y;
		private int width;
		private int height;
		
		public GSSettingCategoryElement(GSSettingCategory category) {
			titleText = new TranslatableText("setting." + category.getName());
			
			settings = new LinkedList<>();
		}
		
		public int getPreferredWidth() {
			int minimumWidth = 0;
			for (GSSettingPanel<?> element : settings) {
				if (element.getPreferredWidth() > minimumWidth)
					minimumWidth = element.getPreferredWidth();
			}
			return minimumWidth;
		}
		
		public int getPreferredHeight() {
			GSIRenderer2D renderer = GSPanelContext.getRenderer();
			
			int h = 0;
			h += renderer.getTextHeight();
			h += CATEGORY_TITLE_MARGIN_BOTTOM;

			for (GSSettingPanel<?> element : settings)
				h += element.getPreferredHeight();
			
			return h;
		}

		public void addSettingPanel(GSSettingPanel<?> panel) {
			settings.add(panel);
			GSSettingsGUI.this.add(panel);
		}

		public void removeSetting(GSSetting<?> setting) {
			Iterator<GSSettingPanel<?>> settingItr = settings.iterator();
			while (settingItr.hasNext()) {
				GSSettingPanel<?> panel = settingItr.next();
				if (panel.getSetting() == setting) {
					GSSettingsGUI.this.remove(panel);
					settingItr.remove();
				}
			}
		}
		
		public void onSettingChanged(GSSetting<?> setting) {
			for (GSSettingPanel<?> element : settings) {
				if (element.getSetting().getName().equals(setting.getName())) {
					element.onSettingChanged();
					break;
				}
			}
		}
		
		public int layoutElements(int x, int y, int width) {
			this.x = x;
			this.y = y;

			this.width = width;
			
			GSIRenderer2D renderer = GSPanelContext.getRenderer();
			
			y += renderer.getTextHeight();
			y += CATEGORY_TITLE_MARGIN_BOTTOM;

			for (GSSettingPanel<?> element : settings) {
				element.setPreferredBounds(x, y, width);
				y += element.height;
			}
			
			this.height = y - this.y;
			
			return y;
		}
		
		public GSSettingPanel<?> getHoveredElement(int mouseX, int mouseY) {
			if (!isHovered(mouseX, mouseY))
				return null;

			for (GSSettingPanel<?> element : settings) {
				if (element.isInBounds(mouseX, mouseY))
					return element;
			}
			
			return null;
		}
		
		public boolean isHovered(int mouseX, int mouseY) {
			return mouseX >= this.x && mouseX < this.x + this.width &&
			       mouseY >= this.y && mouseY < this.y + this.height;
		}
		
		public void render(GSIRenderer2D renderer) {
			renderer.drawCenteredText(titleText, x + width / 2, y, CATEGORY_TITLE_COLOR);
		}
		
		public boolean isEmpty() {
			return settings.isEmpty();
		}
	}
}
