package com.g4mesoft.core.compat;

public final class GSCarpetCompat extends GSAbstractCompat {

	/* Visible for GSOutdatedCarpetTickrateManager */
	static final String G4MESPEED_INTERFACE_NAME = "g4mespeed";
	
	private GSICarpetTickrateManager serverTRM;
	private GSICarpetTickrateManager clientTRM;
	
	public GSCarpetCompat() {
		serverTRM = clientTRM = null;
	}
	
	@Override
	public void detect() {
		serverTRM = GSServerCarpetTickrateManager.create();
		if (serverTRM == null) {
			// Likely, either carpet is not installed, or
			// we are using an outdated version of carpet.
			serverTRM = clientTRM = GSOutdatedCarpetTickrateManager.create();
		} else {
			clientTRM = GSClientCarpetTickrateManager.create();
		}
	}

	public GSICarpetTickrateManager getServerTickrateManager() {
		return serverTRM;
	}

	public GSICarpetTickrateManager getClientTickrateManager() {
		return clientTRM;
	}
}
