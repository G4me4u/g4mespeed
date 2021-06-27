package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.hotkey.GSKeyBinding;
import com.g4mesoft.panel.GSClosableParentPanel;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.event.GSCompoundButtonStroke;
import com.g4mesoft.panel.event.GSIButtonStroke;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSKeyBindingButtonStroke;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

@Environment(EnvType.CLIENT)
public class GSTabbedGUI extends GSClosableParentPanel implements GSIMouseListener {

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

	private int selectedTabIndex;

	public GSTabbedGUI() {
		tabs = new ArrayList<>();
		selectedTabIndex = -1;
		
		GSKeyBinding guiKeyBinding = GSControllerClient.getInstance().getOpenGUIKey();
		GSIButtonStroke guiButton = new GSKeyBindingButtonStroke(guiKeyBinding);
		setCloseButton(new GSCompoundButtonStroke(getCloseButton(), guiButton));
		
		addMouseEventListener(this);
	}

	public void addTab(String title, GSPanel tabContent) {
		tabs.add(new GSTabEntry(title, tabContent));

		if (selectedTabIndex == -1)
			setSelectedTabIndex(0);
		
		if (isVisible())
			requestLayout();
	}

	public void setSelectedTabIndex(int index) {
		if (index != -1 && (index < 0 || index >= tabs.size()))
			throw new IllegalArgumentException("Invalid tab index: " + index);
		
		if (selectedTabIndex != -1)
			remove(getTabContent(selectedTabIndex));
		
		selectedTabIndex = index;

		if (index != -1)
			add(getTabContent(index));
	}
	
	public GSPanel getTabContent(int index) {
		return (index != -1) ? tabs.get(index).getTabContent() : null;
	}

	public void layout() {
		GSIRenderer2D renderer = GSPanelContext.getRenderer();

		tabHeight = renderer.getTextHeight() + TAB_VERTICAL_PADDING * 2;

		for (GSTabEntry tab : tabs) {
			String title = i18nTranslate(tab.getTitle());
			int titleWidth = (int)Math.ceil(renderer.getTextWidth(title));

			tab.setDisplayTitle(title);
			tab.setWidth(titleWidth + TAB_HORIZONTAL_PADDING * 2);
		}

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

			remainingWidth -= tabWidth;
			remainingTabs--;
		}

		for ( ; remainingTabs > 0; remainingTabs--) {
			GSTabEntry tab = sortedTabs[remainingTabs - 1];
			tab.setWidth(remainingWidth / remainingTabs);
			remainingWidth -= tab.getWidth();
		}

		for (GSTabEntry tab : sortedTabs) {
			String title = tab.getDisplayTitle();
			title = renderer.trimString(title, tab.getWidth());
			tab.setDisplayTitle(title);
		}

		int tabOffsetX = HORIZONTAL_MARGIN;
		for (GSTabEntry tab : tabs) {
			GSPanel content = tab.getTabContent();
			if (content != null) {
				int xo = HORIZONTAL_MARGIN;
				int yo = VERTICAL_MARGIN + tabHeight;
				content.setBounds(xo, yo, contentWidth, contentHeight);
			}

			tab.setX(tabOffsetX);
			tabOffsetX += tab.getWidth();
		}
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		renderBackground(renderer);
		
		super.render(renderer);
		
		renderTabs(renderer);
	}

	private void renderTabs(GSIRenderer2D renderer) {
		int totalTabWidth = 0;

		for (int i = 0; i < tabs.size(); i++) {
			GSTabEntry tab = tabs.get(i);
			boolean selected = (i == selectedTabIndex);

			renderTab(renderer, tab, i, selected);
			totalTabWidth += tab.getWidth();
		}

		renderer.drawRect(HORIZONTAL_MARGIN, VERTICAL_MARGIN, totalTabWidth, tabHeight, TAB_BORDER_COLOR);
	}

	private void renderTab(GSIRenderer2D renderer, GSTabEntry tab, int tabIndex, boolean selected) {
		if (tab.isHovered(renderer.getMouseX(), renderer.getMouseY())) {
			renderer.fillRect(tab.getX(), VERTICAL_MARGIN, tab.getWidth(), tabHeight, HOVERED_BACKGROUND_COLOR);
		} else if (selected) {
			renderer.fillRect(tab.getX(), VERTICAL_MARGIN, tab.getWidth(), tabHeight, SELECTED_BACKGROUND_COLOR);
		}
		
		int xc = tab.getX() + tab.getWidth() / 2;
		int yc = VERTICAL_MARGIN + (tabHeight - renderer.getTextHeight()) / 2;
		
		int titleColor = selected ? SELECTED_TEXT_COLOR : TAB_TEXT_COLOR;
		renderer.drawCenteredText(tab.getDisplayTitle(), xc, yc, titleColor);

		if (tabIndex != 0)
			renderer.drawVLine(tab.getX(), VERTICAL_MARGIN, tabHeight + VERTICAL_MARGIN, TAB_BORDER_COLOR);
	}

	private void playClickSound() {
		GSPanelContext.playSound(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
	
	@Override
	public void mousePressed(GSMouseEvent event) {
		if (event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			for (int i = 0; i < tabs.size(); i++) {
				GSTabEntry tab = tabs.get(i);
				if (tab.isHovered(event.getX(), event.getY())) {
					if (i != selectedTabIndex) {
						setSelectedTabIndex(i);
						playClickSound();
					}
					
					event.consume();
					return;
				}
			}
		}
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

		public String getTitle() {
			return title;
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
		
		private boolean isHovered(int mouseX, int mouseY) {
			if (mouseX < x || mouseX >= x + width)
				return false;
			if (mouseY < VERTICAL_MARGIN || mouseY >= VERTICAL_MARGIN + tabHeight)
				return false;
			return true;
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
