package com.g4mesoft.gui.event;

public interface GSIMouseListener extends GSIEventListener {

	default public void mouseMoved(GSMouseEvent event) {
	}

	default public void mouseDragged(GSMouseEvent event) {
	}

	default public void mousePressed(GSMouseEvent event) {
	}

	default public void mouseReleased(GSMouseEvent event) {
	}

	default public void mouseScrolled(GSMouseEvent event) {
	}
	
}
