package com.g4mesoft.core.compat;

import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.module.tps.GSTpsModule;

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
		// Fallback to not installed manager...
		if (serverTRM == null)
			serverTRM = new GSNotInstalledCarpetTickrateManager();
		if (clientTRM == null)
			clientTRM = new GSNotInstalledCarpetTickrateManager();
	}

	public GSICarpetTickrateManager getServerTickrateManager() {
		return serverTRM;
	}

	public GSICarpetTickrateManager getClientTickrateManager() {
		return clientTRM;
	}
	
	private class GSNotInstalledCarpetTickrateManager implements GSICarpetTickrateManager {

		@Override
		public void onInit(GSIModuleManager manager) {
			// Do nothing
		}

		@Override
		public void onClose() {
			// Do nothing
		}

		@Override
		public boolean isTickrateLinked() {
			return false;
		}

		@Override
		public boolean runsNormally() {
			return true;
		}

		@Override
		public float getTickrate() {
			// Just return the GS tickrate.
			return GSController.getInstanceOnThread()
					.getModule(GSTpsModule.class).getTps();
		}

		@Override
		public void setTickrate(float tickrate) {
		}

		@Override
		public boolean isPollingCompatMode() {
			return false;
		}

		@Override
		public void addListener(GSICarpetTickrateListener listener) {
		}

		@Override
		public void removeListener(GSICarpetTickrateListener listener) {
		}
	}
}
