package com.g4mesoft.panel.event;

public class GSCompoundButtonStroke implements GSIButtonStroke {

	private final GSIButtonStroke button0;
	private final GSIButtonStroke button1;
	
	public GSCompoundButtonStroke(GSIButtonStroke button0, GSIButtonStroke button1) {
		if (button0 == null || button1 == null)
			throw new IllegalArgumentException("Button strokes must not be null!");
		
		this.button0 = button0;
		this.button1 = button1;
	}

	public GSIButtonStroke getButton0() {
		return button0;
	}

	public GSIButtonStroke getButton1() {
		return button1;
	}
	
	@Override
	public boolean isMatching(GSEvent event) {
		return (button0.isMatching(event) || button1.isMatching(event));
	}
}
