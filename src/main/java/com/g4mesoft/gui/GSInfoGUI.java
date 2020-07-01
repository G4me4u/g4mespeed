package com.g4mesoft.gui;

import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.translation.GSTranslationModule;

import net.minecraft.client.util.math.MatrixStack;

public class GSInfoGUI extends GSParentPanel {

	private static final int TEXT_COLOR              = 0xFFFFFFFF;
	private static final int VERSION_COLOR           = 0xFF22FF22;
	private static final int LESS_THAN_VERSION_COLOR = 0xFFFFFF00;
	private static final int INVALID_VERSION_COLOR   = 0xFFFF5555;
	
	private static final String SERVER_VERSION_TITLE_MSG = "gui.info.serverVersionTitle";
	private static final String INVALID_SERVER_MSG       = "gui.info.invalidServer";
	private static final String CLIENT_VERSION_TITLE_MSG = "gui.info.clientVersionTitle";
	
	private final GSControllerClient controllerClient;
	
	public GSInfoGUI(GSControllerClient controllerClient) {
		this.controllerClient = controllerClient;
	}
	
	@Override
	public void renderTranslated(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderTranslated(matrixStack, mouseX, mouseY, partialTicks);
	
		int lineHeight = textRenderer.fontHeight + 2;
		
		int xc = width / 2;
		int y = height / 2 - lineHeight * 5 / 2 - 10;
		
		GSTranslationModule translationModule = getTranslationModule();
		
		drawCenteredString(matrixStack, textRenderer, translationModule.getTranslation(SERVER_VERSION_TITLE_MSG), xc, y, TEXT_COLOR);
		y += lineHeight;
		if (controllerClient.isG4mespeedServer()) {
			GSVersion serverVersion = controllerClient.getServerVersion();
			int versionColor = VERSION_COLOR;
			if (serverVersion.isLessThan(controllerClient.getCoreVersion()))
				versionColor = LESS_THAN_VERSION_COLOR;
			drawCenteredString(matrixStack, textRenderer, serverVersion.toString(), xc, y, versionColor);
		} else {
			drawCenteredString(matrixStack, textRenderer, translationModule.getTranslation(INVALID_SERVER_MSG), xc, y, INVALID_VERSION_COLOR);
		}
		y += lineHeight * 2;
		drawCenteredString(matrixStack, textRenderer, translationModule.getTranslation(CLIENT_VERSION_TITLE_MSG), xc, y, TEXT_COLOR);
		y += lineHeight;
		drawCenteredString(matrixStack, textRenderer, controllerClient.getCoreVersion().toString(), xc, y, VERSION_COLOR);
	}
}
