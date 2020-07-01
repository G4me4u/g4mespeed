package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.hotkey.GSKeyBinding;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
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
		tabs = new ArrayList<GSTabEntry>();
		selectedTabIndex = -1;
	}

	public void addTab(String title, GSPanel tabContent) {
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
		tabHeight = textRenderer.fontHeight + TAB_VERTICAL_PADDING * 2;

		for (GSTabEntry tab : tabs)
			tab.setWidth(textRenderer.getWidth(tab.getTranslatedTitle()) + TAB_HORIZONTAL_PADDING * 2);

		int contentWidth = Math.max(width - HORIZONTAL_MARGIN * 2, tabs.size());
		int contentHeight = Math.max(height - tabHeight - VERTICAL_MARGIN, 1);

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

		for ( ; remainingTabs > 0; remainingTabs--) {
			GSTabEntry tab = sortedTabs[remainingTabs - 1];
			tab.setWidth(remainingWidth / remainingTabs);
			tab.setDisplayTitle(trimText(textRenderer, tab.getTranslatedTitle(), tab.getWidth()));
			remainingWidth -= tab.getWidth();
		}

		clearChildren();

		int tabXOffset = HORIZONTAL_MARGIN;
		for (GSTabEntry tab : tabs) {
			GSPanel content = tab.getTabContent();
			if (content != null) {
				int xo = HORIZONTAL_MARGIN;
				int yo = VERTICAL_MARGIN + tabHeight;
				content.initBounds(client, xo, yo, contentWidth, contentHeight);
				addPanel(content);
			}

			tab.setX(tabXOffset);
			tabXOffset += tab.getWidth();
		}

		tabsChanged = false;
	}
	
	@Override
	public void tickPanels() {
		GSTabEntry selectedTab = getSelectedTab();
		if (selectedTab != null && selectedTab.getTabContent() != null)
			selectedTab.getTabContent().tick();
	}

	@Override
	@GSCoreOverride
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (tabsChanged)
			layoutTabs();
		
		renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		renderTabs(matrixStack, mouseX, mouseY);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	protected void renderPanels(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		GSTabEntry selectedTab = getSelectedTab();
		if (selectedTab != null && selectedTab.getTabContent() != null)
			selectedTab.getTabContent().render(matrixStack, mouseX, mouseY, partialTicks);
	}

	private void renderTabs(MatrixStack matrixStack, int mouseX, int mouseY) {
		int totalTabWidth = 0;

		for (int i = 0; i < tabs.size(); i++) {
			GSTabEntry tab = tabs.get(i);
			boolean selected = (i == selectedTabIndex);
			boolean hovered = isTabHovered(tab, mouseX, mouseY);

			renderTab(matrixStack, tab, i, selected, hovered);
			totalTabWidth += tab.getWidth();
		}

		drawHorizontalLine(matrixStack, HORIZONTAL_MARGIN, totalTabWidth + HORIZONTAL_MARGIN, VERTICAL_MARGIN, TAB_BORDER_COLOR);
		drawHorizontalLine(matrixStack, HORIZONTAL_MARGIN, totalTabWidth + HORIZONTAL_MARGIN, VERTICAL_MARGIN + tabHeight - 1, TAB_BORDER_COLOR);
		drawVerticalLine(matrixStack, HORIZONTAL_MARGIN, VERTICAL_MARGIN, tabHeight + VERTICAL_MARGIN, TAB_BORDER_COLOR);
		drawVerticalLine(matrixStack, totalTabWidth + HORIZONTAL_MARGIN, VERTICAL_MARGIN, tabHeight + VERTICAL_MARGIN, TAB_BORDER_COLOR);
	}

	private boolean isTabHovered(GSTabEntry tab, double mouseX, double mouseY) {
		return mouseX >= tab.getX() && mouseX <= tab.getX() + tab.getWidth() && mouseY >= VERTICAL_MARGIN
				&& mouseY <= VERTICAL_MARGIN + tabHeight;
	}

	private void playClickSound(SoundManager soundManager) {
		soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	private void renderTab(MatrixStack matrixStack, GSTabEntry tab, int tabIndex, boolean selected, boolean hovered) {
		int background = hovered ? HOVERED_BACKGROUND_COLOR : (selected ? SELECTED_BACKGROUND_COLOR : 0x00000000);
		if (background != 0x00000000)
			fill(matrixStack, tab.getX(), VERTICAL_MARGIN, tab.getX() + tab.getWidth(), VERTICAL_MARGIN + tabHeight, background);

		int xc = tab.getX() + tab.getWidth() / 2;
		int yc = VERTICAL_MARGIN + (tabHeight - textRenderer.fontHeight) / 2;
		
		drawCenteredString(matrixStack, textRenderer, tab.getDisplayTitle(), xc, yc, selected ? SELECTED_TEXT_COLOR : TAB_TEXT_COLOR);

		if (tabIndex != 0)
			drawVerticalLine(matrixStack, tab.getX(), VERTICAL_MARGIN, tabHeight + VERTICAL_MARGIN, TAB_BORDER_COLOR);
	}

	@Override
	public boolean onMouseClickedGS(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			for (int i = 0; i < tabs.size(); i++) {
				if (i != selectedTabIndex && isTabHovered(tabs.get(i), mouseX, mouseY)) {
					setSelectedTabIndex(i);
					playClickSound(client.getSoundManager());
					return true;
				}
			}
		}

		return super.onMouseClickedGS(mouseX, mouseY, button);
	}

	@Override
	public boolean onKeyPressedGS(int key, int scancode, int mods) {
		if (super.onKeyPressedGS(key, scancode, mods))
			return true;
		
		GSKeyBinding openGUIKey = GSControllerClient.getInstance().getOpenGUIKey();
		if (openGUIKey != null && key == openGUIKey.getGLFWKeyCode()) {
			this.onClose();
			return true;
		}
		
		return false;
	}
	
	@Override
	@GSCoreOverride
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
	@GSCoreOverride
	public void init() {
		super.init();
		tabsChanged = true;
	}

	@Override
	@GSCoreOverride
	public boolean isPauseScreen() {
		return true;
	}
	
	private class GSTabEntry {

		private final String title;
		private final GSPanel tabContent;

		private String displayTitle;
		private int x;
		private int width;

		public GSTabEntry(String title, GSPanel tabContent) {
			this.title = title;
			this.tabContent = tabContent;
		}

		public GSPanel getTabContent() {
			return tabContent;
		}

		public void setDisplayTitle(String displayTitle) {
			this.displayTitle = displayTitle;
		}

		public String getDisplayTitle() {
			return displayTitle;
		}
		
		public String getTranslatedTitle() {
			return GSControllerClient.getInstance()
					.getTranslationModule().getTranslation(title);
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
