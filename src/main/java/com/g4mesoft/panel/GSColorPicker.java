package com.g4mesoft.panel;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.util.GSColorUtil;

public class GSColorPicker extends GSParentPanel {

	protected static final int DEFAULT_BACKGROUND_COLOR = 0xFF252526;

	private static final GSIBorder DEFAULT_WHEEL_BORDER = new GSEmptyBorder(0, 0, 0, 3);
	private static final GSIBorder DEFAULT_SLIDER_BORDER = new GSEmptyBorder(3);
	
	private final GSColorWheel colorWheel;
	private final GSColorBrightnessSlider brightnessSlider;
	
	private final List<GSIActionListener> listeners;
	
	public GSColorPicker(int initialColor) {
		colorWheel = new GSColorWheel();
		brightnessSlider = new GSColorBrightnessSlider();
		
		listeners = new ArrayList<>();

		setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
		setLayoutManager(new GSColorPickerLayoutManager());
		
		colorWheel.addActionListener(() -> {
			brightnessSlider.setHue(colorWheel.getHue());
			brightnessSlider.setSaturation(colorWheel.getSaturation());
			dispatchActionEvent();
		});
		colorWheel.setBorder(DEFAULT_WHEEL_BORDER);
		
		brightnessSlider.addActionListener(() -> {
			colorWheel.setBrightness(brightnessSlider.getBrightness());
			dispatchActionEvent();
		});
		brightnessSlider.setBorder(DEFAULT_SLIDER_BORDER);
		
		setColor(initialColor);
	
		add(colorWheel);
		add(brightnessSlider);
	}
	
	public void addActionListener(GSIActionListener listener) {
		listeners.add(listener);
	}

	public void removeActionListener(GSIActionListener listener) {
		listeners.remove(listener);
	}
	
	private void dispatchActionEvent() {
		listeners.forEach(GSIActionListener::actionPerformed);
	}
	
	public GSColorWheel getColorWheel() {
		return colorWheel;
	}

	public GSColorBrightnessSlider getBrightnessSlider() {
		return brightnessSlider;
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
