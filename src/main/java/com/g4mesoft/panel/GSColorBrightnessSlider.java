package com.g4mesoft.panel;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;
import com.g4mesoft.util.GSMathUtil;

public class GSColorBrightnessSlider extends GSPanel implements GSIMouseListener {

	private static final GSDimension PREFERRED_SIZE = new GSDimension(8, 50);
	
	private static final int THUMB_HEIGHT = 5;
	
	private final List<GSIActionListener> listeners;
	
	private double hue;
	private double saturation;
	private double brightness;
	
	public GSColorBrightnessSlider() {
		listeners = new ArrayList<>();

		hue        = 0.0;
		saturation = 0.0;
		brightness = 1.0;
		
		setFocusable(false);
		addMouseEventListener(this);
	}
	
	@Override
	protected void renderForeground(GSIRenderer2D renderer) {
		renderGradient(renderer);
		renderThumb(renderer);
	}

	protected void renderGradient(GSIRenderer2D renderer) {
		int tc = GSColorUtil.hsb2rgb(hue, saturation, 1.0);
		int bc = GSColorUtil.hsb2rgb(hue, saturation, 0.0);
		renderer.fillVGradient(0, 0, innerWidth, innerHeight, tc, bc);
	}

	protected void renderThumb(GSIRenderer2D renderer) {
		int thumbY = (int)((1.0 - brightness) * (innerHeight - THUMB_HEIGHT));
		int color = GSColorUtil.invertRGB(GSColorUtil.hsb2rgb(hue, saturation, brightness));
		renderer.drawRect(0, thumbY, innerWidth, THUMB_HEIGHT, color);
	}
	
	@Override
	protected GSDimension calculatePreferredInnerSize() {
		return PREFERRED_SIZE;
	}
	
	@Override
	public void mousePressed(GSMouseEvent event) {
		if (event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			setBrightnessFromY(event.getY());
			event.consume();
		}
	}
	
	@Override
	public void mouseDragged(GSMouseEvent event) {
		if (event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			setBrightnessFromY(event.getY());
			event.consume();
		}
	}
	
	private void setBrightnessFromY(int y) {
		double b = 1.0 - (double)(y - THUMB_HEIGHT / 2) / (innerHeight - THUMB_HEIGHT);
		brightness = GSMathUtil.clamp(b, 0.0, 1.0);
		dispatchActionEvent();
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
	
	public double getHue() {
		return hue;
	}
	
	public void setHue(double hue) {
		this.hue = hue;
	}
	
	public double getSaturation() {
		return saturation;
	}
	
	public void setSaturation(double saturation) {
		this.saturation = saturation;
	}
	
	public double getBrightness() {
		return brightness;
	}
	
	public void setBrightness(double brightness) {
		this.brightness = brightness;
	}
	
	public void setHSB(double hue, double saturation, double brightness) {
		setHue(hue);
		setSaturation(saturation);
		setBrightness(brightness);
	}
}
