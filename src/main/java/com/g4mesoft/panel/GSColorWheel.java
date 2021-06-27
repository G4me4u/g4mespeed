package com.g4mesoft.panel;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;

import net.minecraft.client.render.VertexFormats;

public class GSColorWheel extends GSPanel implements GSIMouseListener {

	private static final int CURSOR_RADIUS = 2;
	private static final int PREFERRED_RADIUS = 50;
	
	private final List<GSIActionListener> listeners;

	private double hue;
	private double saturation;
	private double brightness;
	
	private int wheelRadius;
	private int wheelX;
	private int wheelY;
	
	public GSColorWheel() {
		listeners = new ArrayList<>();

		hue        = 0.0;
		saturation = 0.0;
		brightness = 1.0;
		
		setFocusable(false);
		addMouseEventListener(this);
	}
	
	@Override
	protected void layout() {
		wheelX = width  / 2;
		wheelY = height / 2;
		
		wheelRadius = Math.min(wheelX, wheelY) - CURSOR_RADIUS;
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		renderer.build(GSIRenderer2D.QUADS, VertexFormats.POSITION_COLOR);
		
		drawColorWheel(renderer);
		drawCursor(renderer);
		
		renderer.finish();
	}
	
	private void drawColorWheel(GSIRenderer2D renderer) {
		for (int x = -wheelRadius; x <= wheelRadius; x++) {
			for (int y = -wheelRadius; y <= wheelRadius; y++) {
				double d = Math.sqrt(x * x + y * y);

				if (d < wheelRadius) {
					// Calculate RGB from the HSB wheel coordinates.
					int rgb = GSColorUtil.hsb2rgb(xy2hue(x, y), dist2sat(d), brightness);
					// Apply alpha correction for anti-aliasing
					double alpha = Math.min(1.0, wheelRadius - d);
					int c = GSColorUtil.withAlpha(rgb, alpha);

					renderer.fillRect(wheelX + x, wheelY + y, 1, 1, c);
				}
			}
		}
	}
	
	private void drawCursor(GSIRenderer2D renderer) {
		// Convert hue and saturation to wheel coordinates
		double omega = Math.PI * (2.0 * hue - 1.0);
		double dist = saturation * wheelRadius;

		// See #xy2hue(x, y) for why x is negated.
		int x = (int)(-Math.cos(omega) * dist);
		int y = (int)( Math.sin(omega) * dist);

		// Use inverted color at (x, y) for better visibility
		int c = GSColorUtil.invertRGB(GSColorUtil.hsb2rgb(hue, saturation, brightness));
		
		int cx = wheelX + x;
		int cy = wheelY + y;
		
		renderer.fillRect(cx,                 cy - CURSOR_RADIUS, 1, CURSOR_RADIUS, c);
		renderer.fillRect(cx,                 cy + 1,             1, CURSOR_RADIUS, c);
		renderer.fillRect(cx - CURSOR_RADIUS, cy,                 CURSOR_RADIUS, 1, c);
		renderer.fillRect(cx + 1,             cy,                 CURSOR_RADIUS, 1, c);
	}
	
	@Override
	protected GSDimension calculatePreferredSize() {
		return new GSDimension(2 * PREFERRED_RADIUS + 1 + CURSOR_RADIUS,
		                       2 * PREFERRED_RADIUS + 1 + CURSOR_RADIUS);
	}
	
	@Override
	public void mousePressed(GSMouseEvent event) {
		if (event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			setHSBFromXY(event.getX() - wheelX, event.getY() - wheelY);
			event.consume();
		}
	}

	@Override
	public void mouseDragged(GSMouseEvent event) {
		if (event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			setHSBFromXY(event.getX() - wheelX, event.getY() - wheelY);
			event.consume();
		}
	}
	
	private void setHSBFromXY(int x, int y) {
		hue        = Math.min(1.0, xy2hue(x, y));
		saturation = Math.min(1.0, xy2sat(x, y));
		
		dispatchActionEvent();
	}
	
	private double xy2hue(int x, int y) {
		return 0.5 * (Math.atan2(y, -x) / Math.PI + 1.0);
	}

	private double xy2sat(int x, int y) {
		return dist2sat(Math.sqrt(x * x + y * y));
	}
	
	private double dist2sat(double dist) {
		return dist / wheelRadius;
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
		this.hue = hue;
		this.saturation = saturation;
		this.brightness = brightness;
	}
}
