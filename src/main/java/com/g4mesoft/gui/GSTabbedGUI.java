package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.sound.SoundEvents;

@Environment(EnvType.CLIENT)
public class GSTabbedGUI extends GSScreen {

	private static final int TAB_VERTICAL_PADDING = 5;
	private static final int TAB_HORIZONTAL_PADDING = 5;

	private static final int HORIZONTAL_MARGIN = 5;
	private static final int VERTICAL_MARGIN = 5;

	private static final int MINIMUM_TAB_WIDTH = 60;

	private static final int TAB_TEXT_COLOR = 0xFFDEDEDE;
	private static final int TAB_BORDER_COLOR = 0xFFDDDDDD;

	private static final int SELECTED_TEXT_COLOR = 0xFFFFFFFF;
	private static final int SELECTED_BACKGROUND_COLOR = 0xAAAAAAAA;
	private static final int HOVERED_BACKGROUND_COLOR = 0xAAEDEDFF;

	private int tabHeight;
	private List<GSTabEntry> tabs;
	private boolean tabsChanged;

	private int selectedTabIndex;

	public GSTabbedGUI() {
		super(NarratorManager.EMPTY);

		tabs = new ArrayList<GSTabEntry>();
		selectedTabIndex = -1;
	}

	public void addTab(String title, GSScreen tabContent) {
		tabs.add(new GSTabEntry(title, tabContent));
		tabsChanged = true;

		if (tabContent != null)
			tabContent.setSelected(false);
		
		if (selectedTabIndex == -1)
			setSelectedTabIndex(0);
	}

	public void setSelectedTabIndex(int index) {
		GSTabEntry tab = getSelectedTab();
		if (tab != null && tab.getTabContent() != null)
			tab.getTabContent().setSelected(false);
		
		selectedTabIndex = index;

		tab = getSelectedTab();
		if (tab != null && tab.getTabContent() != null) {
			tab.getTabContent().setSelected(true);
			
			if (getFocused() != tab.getTabContent())
				setFocused(tab.getTabContent());
		}
	}
	
	public GSTabEntry getSelectedTab() {
		return (selectedTabIndex != -1) ? tabs.get(selectedTabIndex) : null;
	}

	private void layoutTabs() {
		tabHeight = font.fontHeight + TAB_VERTICAL_PADDING * 2;

		for (GSTabEntry tab : tabs)
			tab.setWidth(font.getStringWidth(tab.getTranslatedTitle()) + TAB_HORIZONTAL_PADDING * 2);

		int contentWidth = Math.max(width - HORIZONTAL_MARGIN * 2, tabs.size());
		int contentHeight = Math.max(height - tabHeight - VERTICAL_MARGIN * 2, 1);

		int remainingTabs = tabs.size();
		int remainingWidth = contentWidth;

		GSTabEntry[] sortedTabs = tabs.toArray(new GSTabEntry[0]);
		Arrays.sort(sortedTabs, (a, b) -> Integer.compare(a.getWidth(), b.getWidth()));

		int minimumTabWidth = MINIMUM_TAB_WIDTH;
		GSTabEntry widestTab = sortedTabs[remainingTabs - 1];
		if (widestTab.getWidth() * remainingTabs <= remainingWidth)
			minimumTabWidth = widestTab.getWidth();

		// Iterate from smallest to largest tab
		for (int i = 0; i < sortedTabs.length; i++) {
			GSTabEntry tab = sortedTabs[remainingTabs - 1];
			int tabWidth = Math.max(minimumTabWidth, tab.getWidth());
			if (tabWidth * remainingTabs > remainingWidth)
				break;
			tab.setWidth(tabWidth);
			tab.setDisplayTitle(tab.getTranslatedTitle());
			remainingWidth -= tabWidth;
			remainingTabs--;
		}

		for (; remainingTabs > 0; remainingTabs--) {
			GSTabEntry tab = sortedTabs[remainingTabs - 1];
			tab.setWidth(remainingWidth / remainingTabs);
			tab.setDisplayTitle(trimText(tab.getTranslatedTitle(), tab.getWidth()));
			remainingWidth -= tab.getWidth();
		}

		children.clear();

		int tabXOffset = HORIZONTAL_MARGIN;
		for (GSTabEntry tab : tabs) {
			GSScreen content = tab.getTabContent();
			if (content != null) {
				int xo = HORIZONTAL_MARGIN;
				int yo = VERTICAL_MARGIN + tabHeight;
				content.initBounds(minecraft, xo, yo, contentWidth, contentHeight);
				children.add(content);
			}

			tab.setX(tabXOffset);
			tabXOffset += tab.getWidth();
		}

		tabsChanged = false;
	}

	@Override
	public void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		renderBackground();

		super.renderTranslated(mouseX, mouseY, partialTicks);

		renderTabs(mouseX, mouseY);

		GSTabEntry selectedTab = getSelectedTab();
		if (selectedTab != null && selectedTab.getTabContent() != null)
			selectedTab.getTabContent().render(mouseX, mouseY, partialTicks);
	}

	@Override
	public void tick() {
		super.tick();

		GSTabEntry selectedTab = getSelectedTab();
		if (selectedTab != null && selectedTab.getTabContent() != null)
			selectedTab.getTabContent().tick();
	}

	private void renderTabs(int mouseX, int mouseY) {
		if (tabsChanged)
			layoutTabs();

		int totalTabWidth = 0;

		for (int i = 0; i < tabs.size(); i++) {
			GSTabEntry tab = tabs.get(i);
			boolean selected = (i == selectedTabIndex);
			boolean hovered = isTabHovered(tab, mouseX, mouseY);

			renderTab(tab, i, selected, hovered);
			totalTabWidth += tab.getWidth();
		}

		hLine(HORIZONTAL_MARGIN, totalTabWidth + HORIZONTAL_MARGIN, VERTICAL_MARGIN, TAB_BORDER_COLOR);
		hLine(HORIZONTAL_MARGIN, totalTabWidth + HORIZONTAL_MARGIN, VERTICAL_MARGIN + tabHeight - 1, TAB_BORDER_COLOR);
		vLine(HORIZONTAL_MARGIN, VERTICAL_MARGIN, tabHeight + VERTICAL_MARGIN, TAB_BORDER_COLOR);
		vLine(totalTabWidth + HORIZONTAL_MARGIN, VERTICAL_MARGIN, tabHeight + VERTICAL_MARGIN, TAB_BORDER_COLOR);
	}

	private boolean isTabHovered(GSTabEntry tab, double mouseX, double mouseY) {
		return mouseX >= tab.getX() && mouseX <= tab.getX() + tab.getWidth() && mouseY >= VERTICAL_MARGIN
				&& mouseY <= VERTICAL_MARGIN + tabHeight;
	}

	private void playClickSound(SoundManager soundManager) {
		soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	private void renderTab(GSTabEntry tab, int tabIndex, boolean selected, boolean hovered) {
		int background = hovered ? HOVERED_BACKGROUND_COLOR : (selected ? SELECTED_BACKGROUND_COLOR : 0x00000000);
		if (background != 0x00000000)
			fill(tab.getX(), VERTICAL_MARGIN, tab.getX() + tab.getWidth(), VERTICAL_MARGIN + tabHeight, background);

		int xc = tab.getX() + tab.getWidth() / 2;
		int yc = VERTICAL_MARGIN + (tabHeight - font.fontHeight) / 2;
		
		drawCenteredString(font, tab.getDisplayTitle(), xc, yc, selected ? SELECTED_TEXT_COLOR : TAB_TEXT_COLOR);

		if (tabIndex != 0)
			vLine(tab.getX(), VERTICAL_MARGIN, tabHeight + VERTICAL_MARGIN, TAB_BORDER_COLOR);
	}

	@Override
	public boolean mouseClickedTranslated(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			for (int i = 0; i < tabs.size(); i++) {
				if (i != selectedTabIndex && isTabHovered(tabs.get(i), mouseX, mouseY)) {
					setSelectedTabIndex(i);
					playClickSound(minecraft.getSoundManager());
					return true;
				}
			}
		}

		return super.mouseClickedTranslated(mouseX, mouseY, button);
	}

	@Override
	public void setFocused(Element element) {
		super.setFocused(element);

		if (element != null && !tabs.isEmpty()) {
			for (int i = 0; i < tabs.size(); i++) {
				if (i != selectedTabIndex && tabs.get(i).tabContent == element) {
					setSelectedTabIndex(i);
					break;
				}
			}
		}
	}

	@Override
	public void init() {
		super.init();
		tabsChanged = true;
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		tabsChanged = true;
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}

	private class GSTabEntry {

		private final String title;
		private final GSScreen tabContent;

		private String displayTitle;
		private int x;
		private int width;

		public GSTabEntry(String title, GSScreen tabContent) {
			this.title = title;
			this.tabContent = tabContent;
		}

		public GSScreen getTabContent() {
			return tabContent;
		}

		public void setDisplayTitle(String displayTitle) {
			this.displayTitle = displayTitle;
		}

		public String getDisplayTitle() {
			return displayTitle;
		}
		
		public String getTranslatedTitle() {
			return getTranslationModule().getTranslation(title);
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getX() {
			return x;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getWidth() {
			return width;
		}
	}
}
