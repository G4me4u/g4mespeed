package com.g4mesoft.gui.event;

public interface GSIKeyListener extends GSIEventListener {

	default public void keyPressed(GSKeyEvent event) {
	}

	default public void keyReleased(GSKeyEvent event) {
	}

	default public void keyTyped(GSKeyEvent event) {
	}
	
}
