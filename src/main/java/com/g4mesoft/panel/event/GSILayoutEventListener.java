package com.g4mesoft.panel.event;

public interface GSILayoutEventListener {

	default public void panelAdded(GSLayoutEvent event) {
	}

	default public void panelRemoved(GSLayoutEvent event) {
	}
	
	default public void panelResized(GSLayoutEvent event) {
	}

	default public void panelMoved(GSLayoutEvent event) {
	}
	
	default public void panelShown(GSLayoutEvent event) {
	}
	
	default public void panelHidden(GSLayoutEvent event) {
	}
	
}
