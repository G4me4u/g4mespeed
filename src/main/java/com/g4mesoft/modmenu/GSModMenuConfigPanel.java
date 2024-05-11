package com.g4mesoft.modmenu;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.gui.GSHotkeyGUI;
import com.g4mesoft.gui.GSInfoGUI;
import com.g4mesoft.gui.GSTabbedGUI;
import com.g4mesoft.gui.setting.GSSettingsGUI;
import com.g4mesoft.ui.panel.GSClosableParentPanel;
import com.g4mesoft.ui.panel.GSETextAlignment;
import com.g4mesoft.ui.panel.GSPanel;
import com.g4mesoft.ui.panel.field.GSTextLabel;
import com.g4mesoft.ui.panel.legacy.GSButtonPanel;
import com.g4mesoft.ui.panel.scroll.GSScrollPanel;
import com.g4mesoft.ui.renderer.GSIRenderer2D;
import com.g4mesoft.ui.renderer.GSTexture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GSModMenuConfigPanel extends GSClosableParentPanel {

	private static final GSTexture LIST_BACKGROUND_TEXTURE = new GSTexture(new Identifier("textures/gui/menu_list_background.png"), 16, 16);
	private static final GSTexture HEADER_SEPARATOR_TEXTURE = new GSTexture(Screen.HEADER_SEPARATOR_TEXTURE, 32, 2);
	private static final GSTexture FOOTER_SEPARATOR_TEXTURE = new GSTexture(Screen.FOOTER_SEPARATOR_TEXTURE, 32, 2);
	
	private static final GSTexture INWORLD_LIST_BACKGROUND_TEXTURE = new GSTexture(new Identifier("textures/gui/inworld_menu_list_background.png"), 16, 16);
	private static final GSTexture INWORLD_HEADER_SEPARATOR_TEXTURE = new GSTexture(Screen.INWORLD_HEADER_SEPARATOR_TEXTURE, 32, 2);
	private static final GSTexture INWORLD_FOOTER_SEPARATOR_TEXTURE = new GSTexture(Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE, 32, 2);
	
	private static final int TOP_MARGIN    = 32;
	private static final int BOTTOM_MARGIN = 32;
	private static final int SEPARATOR_HEIGHT = 2;
	
	private static final int BUTTON_MARGIN = 5;
	private static final int DONE_WIDTH    = 200;
	
	private static final Text DONE_TEXT = Text.translatable("g4mespeed.modmenu.done");
	private static final Text TITLE_TEXT = Text.translatable("g4mespeed.modmenu.title");
	
	private final Screen previous;
	
	private final GSTabbedGUI configGUI;
	private final GSButtonPanel closeButton;
	private final GSTextLabel titleLabel;
	
	public GSModMenuConfigPanel(Screen previous) {
		this.previous = previous;
	
		GSClientController controller = GSClientController.getInstance();
		
		configGUI = new GSTabbedGUI(false);
		configGUI.addTab(GSClientController.CLIENT_SETTINGS_GUI_TITLE, new GSScrollPanel(new GSSettingsGUI(controller.getSettingManager())));
		configGUI.addTab(GSClientController.HOTKEY_GUI_TITLE,          new GSScrollPanel(new GSHotkeyGUI(controller.getKeyManager())));
		configGUI.addTab(GSClientController.G4MESPEED_INFO_GUI_TITLE,  new GSInfoGUI(controller));
		
		closeButton = new GSButtonPanel(DONE_TEXT, this::close);
		titleLabel = new GSTextLabel(TITLE_TEXT);
		titleLabel.setTextAlignment(GSETextAlignment.CENTER);
		
		add(configGUI);
		add(closeButton);
		add(titleLabel);
	}
	
	@Override
	protected void layout() {
		super.layout();

		configGUI.setBounds(0, TOP_MARGIN, width, height - TOP_MARGIN - BOTTOM_MARGIN);
		closeButton.setPreferredBounds((width - DONE_WIDTH) / 2, height - BOTTOM_MARGIN + BUTTON_MARGIN, DONE_WIDTH);
		titleLabel.setBounds(0, 0, width, TOP_MARGIN);
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		renderBackground(renderer);
		
		super.render(renderer);
		
		renderSeparators(renderer);
	}
	
	private boolean isInWorld() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client.world != null;
	}

	private void renderBackground(GSIRenderer2D renderer) {
		// Draw menu background
		renderer.drawMenuBackground(x, y, width, height, isInWorld());
		// Draw content background (scrollable)
		int x = configGUI.getX();
		int y = configGUI.getY();
		// If the content is a scroll panel, offset the background.
		GSPanel content = configGUI.getSelectedTabContent();
		if (content instanceof GSScrollPanel) {
			x += ((GSScrollPanel)content).getViewportOffsetX();
			y += ((GSScrollPanel)content).getViewportOffsetY();
		}
		GSTexture contentBackground = isInWorld() ? INWORLD_LIST_BACKGROUND_TEXTURE : LIST_BACKGROUND_TEXTURE;
		// Draw content background
		renderer.drawTexture(contentBackground.getRegion(x, y, width, configGUI.getHeight()), 0, configGUI.getY());
	}
	
	private void renderSeparators(GSIRenderer2D renderer) {
		GSTexture headerSeparator = isInWorld() ? INWORLD_HEADER_SEPARATOR_TEXTURE : HEADER_SEPARATOR_TEXTURE;
		GSTexture footerSeparator = isInWorld() ? INWORLD_FOOTER_SEPARATOR_TEXTURE : FOOTER_SEPARATOR_TEXTURE;
		
		// Draw header separator
		renderer.drawTexture(headerSeparator.getRegion(0, 0, width, SEPARATOR_HEIGHT), 0, TOP_MARGIN - SEPARATOR_HEIGHT);
		// Draw footer separator
		renderer.drawTexture(footerSeparator.getRegion(0, 0, width, SEPARATOR_HEIGHT), 0, height - BOTTOM_MARGIN);
	}
	
	@Override
	public void close() {
		MinecraftClient.getInstance().setScreen(previous);
	}
}
