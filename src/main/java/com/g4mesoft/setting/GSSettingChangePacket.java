package com.g4mesoft.setting;

import java.io.IOException;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

public class GSSettingChangePacket implements GSIPacket {

	private GSSettingCategory category;
	private GSSetting<?> setting;
	private GSESettingChangeType type;
	
	public GSSettingChangePacket() {
	}

	public GSSettingChangePacket(GSSettingCategory category, GSSetting<?> setting, GSESettingChangeType type) {
		this.category = category;
		this.setting = setting;
		this.type = type;
	}

	@Override
	public void read(GSDecodeBuffer buf) throws IOException {
		category = GSSettingCategory.read(buf);
		type = GSESettingChangeType.fromIndex(buf.readUnsignedByte());
		String decoderType = buf.readString(GSSettingMap.MAX_TYPESTRING_LENGTH);
		String settingName = buf.readString();
		
		GSISettingDecoder<?> decoder = GSSettingManager.getDecoder(decoderType);
		if (decoder == null)
			throw new IOException("No valid decoder found");
		setting = decoder.decodeSetting(settingName, buf);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void write(GSEncodeBuffer buf) throws IOException {
		@SuppressWarnings("rawtypes")
		GSISettingDecoder decoder = GSSettingManager.getDecoder(setting);
		if (decoder == null)
			throw new IOException("No valid decoder found");

		category.write(buf);
		buf.writeUnsignedByte((short)type.getIndex());
		buf.writeString(decoder.getTypeString(), GSSettingMap.MAX_TYPESTRING_LENGTH);
		buf.writeString(setting.getName());
		
		decoder.encodeSetting(buf, setting);
	}

	@Override
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
		if (type != GSESettingChangeType.SETTING_CHANGED)
			return;
		
		if (controller.isAllowedSettingChange(player)) {
			// Select the appropriate setting manager
			GSSettingManager settingManager = controller.getGlobalSettingManager();
			if (!settingManager.isRegistered(category, setting.getName()))
				settingManager = controller.getWorldSettingManager();
			// Update the setting value
			GSSetting<?> currentSetting = settingManager.getSetting(category, setting.getName());
			if (currentSetting != null && currentSetting.isActive() && currentSetting.isVisibleInGui() && currentSetting.isEnabledInGui())
				currentSetting.setIfSameType(setting);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSClientController controller) {
		GSRemoteSettingManager remoteSettings = controller.getServerSettings();
		
		switch (type) {
		case SETTING_CHANGED:
			remoteSettings.onRemoteSettingChanged(category, setting);
			break;
		case SETTING_ADDED:
			remoteSettings.onRemoteSettingAdded(category, setting);
			break;
		case SETTING_REMOVED:
			remoteSettings.onRemoteSettingRemoved(category, setting);
			break;
		}
	}
	
	public enum GSESettingChangeType {
		SETTING_CHANGED(0),
		SETTING_ADDED(1),
		SETTING_REMOVED(2);
		
		private final int index;
		
		private GSESettingChangeType(int index) {
			this.index = index;
		}
		
		public static GSESettingChangeType fromIndex(int index) {
			switch (index) {
			case 0:
				return SETTING_CHANGED;
			case 1:
				return SETTING_ADDED;
			case 2:
				return SETTING_REMOVED;
			}
			
			return null;
		}
		
		public int getIndex() {
			return index;
		}
	}
}
