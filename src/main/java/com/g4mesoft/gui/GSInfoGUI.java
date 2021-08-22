package com.g4mesoft.gui;

import java.util.Collection;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionInfoList;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GSInfoGUI extends GSParentPanel {

	private static final int TEXT_COLOR              = 0xFFFFFFFF;
	private static final int EXTENSION_NAME_COLOR    = 0xFFAAAAAA;
	
	private static final int VERSION_COLOR           = 0xFF22FF22;
	private static final int LESS_THAN_VERSION_COLOR = 0xFFFFFF00;
	private static final int INVALID_VERSION_COLOR   = 0xFFFF5555;
	
	private static final int TEXT_SPACING = 5;
	
	private static final Text SERVER_EXTENSIONS_TITLE = new TranslatableText("gui.info.serverExtensionsTitle");
	private static final Text CLIENT_EXTENSIONS_TITLE = new TranslatableText("gui.info.clientExtensionsTitle");
	private static final Text INVALID_VERSION_TEXT    = new TranslatableText("gui.info.invalidVersion");
	private static final String EXTENSION_NAME_TRANSLATION_KEY = "gui.info.extensionName";
	
	private final GSClientController client;
	
	public GSInfoGUI(GSClientController client) {
		this.client = client;
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		Collection<GSExtensionInfo> serverInfoList = client.getServerExtensionInfoList().getAllInfo();
		Collection<GSExtensionInfo> clientInfoList = G4mespeedMod.getExtensionInfoList().getAllInfo();
		
		int lineCount = 3 + serverInfoList.size() + clientInfoList.size();
		
		int xc = width / 2;
		int y = height / 2 - renderer.getLineHeight() * lineCount / 2 - 10;
		
		if (client.isConnectedToServer()) {
			y = drawExtensionList(renderer, xc, y, SERVER_EXTENSIONS_TITLE, serverInfoList);
			y += renderer.getLineHeight();
		}
		y = drawExtensionList(renderer, xc, y, CLIENT_EXTENSIONS_TITLE, clientInfoList);
	}
	
	private int drawExtensionList(GSIRenderer2D renderer, int xc, int y, Text title, Collection<GSExtensionInfo> infoList) {
		renderer.drawCenteredText(title, xc, y, TEXT_COLOR);
		y += renderer.getLineHeight();

		for (GSExtensionInfo info : infoList) {
			drawExtensionInfo(renderer, info, xc, y);
			y += renderer.getLineHeight();
		}
		
		return y;
	}
	
	private void drawExtensionInfo(GSIRenderer2D renderer, GSExtensionInfo info, int xc, int y) {
		Text versionText;
		int versionColor;
		
		GSVersion version = info.getVersion();

		if (!version.isInvalid()) {
			versionText = new LiteralText(version.toString());
			
			GSExtensionInfoList clientInfoList = G4mespeedMod.getExtensionInfoList();
			GSExtensionInfo clientInfo = clientInfoList.getInfo(info.getUniqueId());
			
			if (version.isLessThan(clientInfo.getVersion())) {
				versionColor = LESS_THAN_VERSION_COLOR;
			} else {
				versionColor = VERSION_COLOR;
			}
		} else {
			versionText = INVALID_VERSION_TEXT;
			versionColor = INVALID_VERSION_COLOR;
		}
		
		Text prefix = new TranslatableText(EXTENSION_NAME_TRANSLATION_KEY, info.getName());

		float pw = renderer.getTextWidth(prefix) + TEXT_SPACING;
		float tw = pw + renderer.getTextWidth(versionText);
		
		int tx = xc - (int)(tw / 2.0f);
		renderer.drawText(prefix, tx, y, EXTENSION_NAME_COLOR);
		renderer.drawText(versionText, tx + (int)Math.ceil(pw), y, versionColor);
	}
}
