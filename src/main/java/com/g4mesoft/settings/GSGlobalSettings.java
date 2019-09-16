package com.g4mesoft.settings;

public class GSGlobalSettings {

	private boolean gsEnabled;
	
	public GSGlobalSettings() {
		gsEnabled = true;
	}
	
	public boolean isEnabled() {
		return gsEnabled;
	}
	
	public void setEnabled(boolean enabled) {
		gsEnabled = enabled;
	}
}
