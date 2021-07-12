package com.g4mesoft.setting;

import java.io.IOException;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class GSSettingPermissionPacket implements GSIPacket {

	private boolean allowedSettingChange;
	
	public GSSettingPermissionPacket() {
	}

	public GSSettingPermissionPacket(boolean allowedSettingChange) {
		this.allowedSettingChange = allowedSettingChange;
	}
	
	@Override
	public void read(PacketByteBuf buf) throws IOException {
		allowedSettingChange = buf.readBoolean();
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeBoolean(allowedSettingChange);
	}

	@Override
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSClientController controller) {
		controller.getServerSettings().setAllowedSettingChange(allowedSettingChange);
	}
}
