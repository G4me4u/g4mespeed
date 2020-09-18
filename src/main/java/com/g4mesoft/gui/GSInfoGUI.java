package com.g4mesoft.gui;

import java.util.Collection;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionInfoList;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.gui.renderer.GSIRenderer2D;

public class GSInfoGUI extends GSParentPanel {

	private static final int TEXT_COLOR              = 0xFFFFFFFF;
	private static final int EXTENSION_NAME_COLOR    = 0xFFAAAAAA;
	
	private static final int VERSION_COLOR           = 0xFF22FF22;
	private static final int LESS_THAN_VERSION_COLOR = 0xFFFFFF00;
	private static final int INVALID_VERSION_COLOR   = 0xFFFF5555;
	
	private static final int TEXT_SPACING = 5;
	
	private static final String SERVER_EXTENSIONS_TITLE = "gui.info.serverExtensionsTitle";
	private static final String CLIENT_EXTENSIONS_TITLE = "gui.info.clientExtensionsTitle";
	private static final String EXTENSION_NAME_TEXT     = "gui.info.extensionName";
	private static final String INVALID_VERSION_TEXT    = "gui.info.invalidVersion";
	
	private final GSControllerClient client;
	
	public GSInfoGUI(GSControllerClient client) {
		this.client = client;
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		Collection<GSExtensionInfo> serverInfoList = client.getServerExtensionInfoList().getAllInfo();
		Collection<GSExtensionInfo> clientInfoList = G4mespeedMod.getExtensionInfoList().getAllInfo();
		
		int numLines = 3 + serverInfoList.size() + clientInfoList.size();
		
		int xc = width / 2;
		int y = height / 2 - renderer.getLineHeight() * numLines / 2 - 10;
		
		renderer.drawCenteredString(i18nTranslate(SERVER_EXTENSIONS_TITLE), xc, y, TEXT_COLOR);
		y += renderer.getLineHeight();

		for (GSExtensionInfo info : serverInfoList) {
			drawExtensionInfo(renderer, info, xc, y);
			y += renderer.getLineHeight();
		}

		y += renderer.getLineHeight();
		renderer.drawCenteredString(i18nTranslate(CLIENT_EXTENSIONS_TITLE), xc, y, TEXT_COLOR);
		y += renderer.getLineHeight();

		for (GSExtensionInfo info : clientInfoList) {
			drawExtensionInfo(renderer, info, xc, y);
			y += renderer.getLineHeight();
		}
	}
	
	private void drawExtensionInfo(GSIRenderer2D renderer, GSExtensionInfo info, int xc, int y) {
		String versionString;
		int versionColor;
		
		GSVersion version = info.getVersion();

		if (!version.isInvalid()) {
			versionString = version.toString();
			
			GSExtensionInfoList clientInfoList = G4mespeedMod.getExtensionInfoList();
			GSExtensionInfo clientInfo = clientInfoList.getInfo(info.getUniqueId());
			
			if (version.isLessThan(clientInfo.getVersion())) {
				versionColor = LESS_THAN_VERSION_COLOR;
			} else {
				versionColor = VERSION_COLOR;
			}
		} else {
			versionString = i18nTranslate(INVALID_VERSION_TEXT);
			versionColor = INVALID_VERSION_COLOR;
		}
		
		String prefix = i18nTranslateFormatted(EXTENSION_NAME_TEXT, info.getName());

		float pw = renderer.getStringWidth(prefix) + TEXT_SPACING;
		float tw = pw + renderer.getStringWidth(versionString);
		
		int tx = xc - (int)(tw / 2.0f);
		renderer.drawString(prefix, tx, y, EXTENSION_NAME_COLOR);
		renderer.drawString(versionString, tx + (int)Math.ceil(pw), y, versionColor);
	}
}
