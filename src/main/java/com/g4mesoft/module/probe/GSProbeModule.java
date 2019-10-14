package com.g4mesoft.module.probe;

import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.gui.GSTabbedGUI;

public class GSProbeModule implements GSIModule {

	public static final GSVersion PROBE_INTRODUCTION_VERSION = new GSVersion(1, 0, 1);
	private static final String PROBE_GUI_TITLE = "Probes";

	@Override
	public void init(GSIModuleManager manager) {
	}

	@Override
	public void initGUI(GSTabbedGUI tabbedGUI) {
		tabbedGUI.addTab(PROBE_GUI_TITLE, null);
	}
}
