package com.g4mesoft.gui;

import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.gui.renderer.GSIRenderer2D;

public class GSInfoGUI extends GSParentPanel {

	private static final int TEXT_COLOR              = 0xFFFFFFFF;
	private static final int VERSION_COLOR           = 0xFF22FF22;
	private static final int LESS_THAN_VERSION_COLOR = 0xFFFFFF00;
	private static final int INVALID_VERSION_COLOR   = 0xFFFF5555;
	
	private static final String SERVER_VERSION_TITLE_MSG = "gui.info.serverVersionTitle";
	private static final String INVALID_VERSION_MSG       = "gui.info.invalidServer";
	private static final String CLIENT_VERSION_TITLE_MSG = "gui.info.clientVersionTitle";
	
	private final GSControllerClient controllerClient;
	
	public GSInfoGUI(GSControllerClient controllerClient) {
		this.controllerClient = controllerClient;
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		int xc = width / 2;
		int y = height / 2 - renderer.getLineHeight() * 5 / 2 - 10;
		
		drawVersion(renderer, SERVER_VERSION_TITLE_MSG, controllerClient.getServerVersion(), xc, y);
		y += 3 * renderer.getLineHeight();
		
		drawVersion(renderer, CLIENT_VERSION_TITLE_MSG, controllerClient.getCoreVersion(), xc, y);
	}
	
	private void drawVersion(GSIRenderer2D renderer, String title, GSVersion version, int xc, int y) {
		renderer.drawCenteredString(i18nTranslate(title), xc, y, TEXT_COLOR);
		y += renderer.getLineHeight();

		String versionString;
		int versionColor;
		if (!version.isInvalid()) {
			versionString = version.toString();
			
			if (version.isLessThan(controllerClient.getCoreVersion())) {
				versionColor = LESS_THAN_VERSION_COLOR;
			} else {
				versionColor = VERSION_COLOR;
			}
		} else {
			versionString = i18nTranslate(INVALID_VERSION_MSG);
			versionColor = INVALID_VERSION_COLOR;
		}

		renderer.drawCenteredString(versionString, xc, y, versionColor);
	}
}
