package com.g4mesoft.modmenu;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.gui.GSHotkeyGUI;
import com.g4mesoft.gui.GSInfoGUI;
import com.g4mesoft.gui.GSSettingsGUI;
import com.g4mesoft.gui.GSTabbedGUI;
import com.g4mesoft.panel.GSClosableParentPanel;
import com.g4mesoft.panel.GSETextAlignment;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.field.GSTextLabel;
import com.g4mesoft.panel.legend.GSButtonPanel;
import com.g4mesoft.panel.scroll.GSScrollPanel;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.renderer.GSTexture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GSModMenuConfigPanel extends GSClosableParentPanel {

	private static final GSTexture BACKGROUND_TEXTURE = new GSTexture(Screen.OPTIONS_BACKGROUND_TEXTURE, 32, 32);
	
	private static final float BACKGROUND_R = 64.0f / 255.0f;
	private static final float BACKGROUND_G = 64.0f / 255.0f;
	private static final float BACKGROUND_B = 64.0f / 255.0f;
	
	private static final float DARK_BACKGROUND_R = 32.0f / 255.0f;
	private static final float DARK_BACKGROUND_G = 32.0f / 255.0f;
	private static final float DARK_BACKGROUND_B = 32.0f / 255.0f;
	
	private static final int TOP_MARGIN    = 32;
	private static final int BOTTOM_MARGIN = 32;
	
	private static final int BUTTON_MARGIN = 5;
	private static final int DONE_WIDTH    = 200;
	
	private static final int SHADOW_WIDTH  = 4;
	
	private static final Text DONE_TEXT = new TranslatableText("g4mespeed.modmenu.done");
	private static final Text TITLE_TEXT = new TranslatableText("g4mespeed.modmenu.title");
	
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

		configGUI.setOuterBounds(0, TOP_MARGIN, innerWidth, innerHeight - TOP_MARGIN - BOTTOM_MARGIN);
		closeButton.setPreferredBounds((innerWidth - DONE_WIDTH) / 2, innerHeight - BOTTOM_MARGIN + BUTTON_MARGIN, DONE_WIDTH);
		titleLabel.setOuterBounds(0, 0, innerWidth, TOP_MARGIN);
	}
	
	@Override
	protected void renderBackground(GSIRenderer2D renderer, int x, int y, int width, int height) {
		// Draw content background (scrollable)
		int cx = configGUI.getOuterX();
		int cy = configGUI.getOuterY();

		// If the content is a scroll panel, offset the background.
		GSPanel content = configGUI.getSelectedTabContent();
		if (content instanceof GSScrollPanel) {
			cx -= ((GSScrollPanel)content).getViewportOffsetX();
			cy -= ((GSScrollPanel)content).getViewportOffsetY();
		}
		
		// Draw content background
		renderer.drawTexture(BACKGROUND_TEXTURE.getRegion(cx, cy, width, configGUI.getOuterHeight()),
				configGUI.getOuterX(), configGUI.getOuterY(), DARK_BACKGROUND_R, DARK_BACKGROUND_G, DARK_BACKGROUND_B);
		// Draw top margin
		renderer.drawTexture(BACKGROUND_TEXTURE.getRegion(x, y, width, TOP_MARGIN),
				x, y, BACKGROUND_R, BACKGROUND_G, BACKGROUND_B);
		// Draw bottom margin
		renderer.drawTexture(BACKGROUND_TEXTURE.getRegion(x, y + height - BOTTOM_MARGIN, width, BOTTOM_MARGIN),
				x, y + height - BOTTOM_MARGIN, BACKGROUND_R, BACKGROUND_G, BACKGROUND_B);
	}
	
	@Override
	protected void renderForeground(GSIRenderer2D renderer) {
		super.renderForeground(renderer);
		
		// Render content shadows
		renderer.fillVGradient(0, TOP_MARGIN, innerWidth, SHADOW_WIDTH, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);
		renderer.fillVGradient(0, innerHeight - BOTTOM_MARGIN - SHADOW_WIDTH, innerWidth, SHADOW_WIDTH, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
	}
	
	@Override
	public void close() {
		MinecraftClient.getInstance().openScreen(previous);
	}
}
