package com.g4mesoft.setting;

import java.io.IOException;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.setting.decoder.GSISettingDecoder;
import com.g4mesoft.util.GSBufferUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

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
	public void read(PacketByteBuf buf) throws IOException {
		category = GSSettingCategory.read(buf);
		type = GSESettingChangeType.fromIndex(buf.readVarInt());
		String decoderType = buf.readString(16);
		String settingName = buf.readString(GSBufferUtil.MAX_STRING_LENGTH);
		
		GSISettingDecoder<?> decoder = GSSettingManager.getSettingDecoder(decoderType);
		if (decoder == null)
			throw new IOException("No valid decoder found");
		setting = decoder.decodeSetting(settingName, buf);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void write(PacketByteBuf buf) throws IOException {
		@SuppressWarnings("rawtypes")
		GSISettingDecoder decoder = GSSettingManager.getSettingDecoder(setting.getClass());
		if (decoder == null)
			throw new IOException("No valid decoder found");

		category.write(buf);
		buf.writeVarInt(type.getIndex());
		buf.writeString(decoder.getTypeString());
		buf.writeString(setting.getName());
		
		decoder.encodeSetting(buf, setting);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
		if (type != GSESettingChangeType.SETTING_CHANGED)
			return;
		
		if (controller.isAllowedSettingChange(player)) {
			GSSetting<?> currentSetting = controller.getSettingManager().getSetting(category, setting.getName());
			if (currentSetting != null && currentSetting.isActive() && currentSetting.isVisibleInGUI())
				currentSetting.setValueIfSameType(setting);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
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
