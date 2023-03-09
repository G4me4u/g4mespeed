package com.g4mesoft.panel;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;

public class GSColorPicker extends GSParentPanel {

	private static final int SLIDER_MARGIN = 3;
	
	protected static final int BACKGROUND_COLOR = 0xFF252526;
	
	private final GSColorWheel colorWheel;
	private final GSColorBrightnessSlider brightnessSlider;
	
	private final List<GSIActionListener> listeners;
	
	public GSColorPicker(int initialColor) {
		colorWheel = new GSColorWheel();
		brightnessSlider = new GSColorBrightnessSlider();
		
		listeners = new ArrayList<>();
		
		colorWheel.addActionListener(() -> {
			brightnessSlider.setHue(colorWheel.getHue());
			brightnessSlider.setSaturation(colorWheel.getSaturation());
			dispatchActionEvent();
		});
		
		brightnessSlider.addActionListener(() -> {
			colorWheel.setBrightness(brightnessSlider.getBrightness());
			dispatchActionEvent();
		});
		
		setColor(initialColor);
	
		add(colorWheel);
		add(brightnessSlider);
	}
	
	@Override
	protected void layout() {
		int bsw = brightnessSlider.getProperty(PREFERRED_WIDTH);
		int bsh = Math.max(0, height - 2 * SLIDER_MARGIN);
		int bsx = width - bsw - SLIDER_MARGIN;
		int cws = Math.min(bsx - SLIDER_MARGIN, height);
		
		colorWheel.setBounds(0, (height - cws) / 2, cws, cws);
		brightnessSlider.setBounds(bsx, (height - bsh) / 2, bsw, bsh);
	}

	@Override
	public void render(GSIRenderer2D renderer) {
		renderer.fillRect(0, 0, width, height, BACKGROUND_COLOR);
		
		super.render(renderer);
	}
	
	@Override
	protected GSDimension calculatePreferredSize() {
		GSDimension cws = colorWheel.getProperty(PREFERRED_SIZE);
		GSDimension bss = brightnessSlider.getProperty(PREFERRED_SIZE);
		
		int w = cws.getWidth() + bss.getWidth() + 2 * SLIDER_MARGIN;
		int h = Math.max(bss.getHeight() + + 2 * SLIDER_MARGIN, cws.getHeight());
		
		return new GSDimension(w, h);
	}
	
	public void addActionListener(GSIActionListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");
		listeners.add(listener);
	}

	public void removeActionListener(GSIActionListener listener) {
		listeners.remove(listener);
	}
	
	private void dispatchActionEvent() {
		listeners.forEach(GSIActionListener::actionPerformed);
	}
	
	public int getColor() {
		return GSColorUtil.hsb2rgb(colorWheel.getHue(),
		                           colorWheel.getSaturation(),
		                           brightnessSlider.getBrightness());
	}

	public void setColor(int color) {
		double[] hsb = GSColorUtil.rgb2hsb(color);
		colorWheel.setHSB(hsb[0], hsb[1], hsb[2]);
		brightnessSlider.setHSB(hsb[0], hsb[1], hsb[2]);
	}
}
