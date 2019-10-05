package com.g4mesoft.gui.widget;

import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.gui.widget.SliderWidget;

public class GSSliderWidget extends SliderWidget {

	public static final int SLIDER_HEIGHT = 20;

	private final GSSliderListener listener;
	private final GSSliderFormatter formatter;

	public GSSliderWidget(int x, int y, int width, double value, GSSliderListener listener, GSSliderFormatter formatter) {
		super(null, x, y, width, SLIDER_HEIGHT, value);

		this.listener = listener;
		this.formatter = formatter;
		
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		setMessage(formatter.formatValue(value));
	}

	@Override
	protected void applyValue() {
		if (listener != null)
			value = listener.onValueChanged(value);
	}

	public void setValueSilent(double value) {
		this.value = GSMathUtils.clamp(value, 0.0D, 1.0D);
		
		updateMessage();
	}

	public static interface GSSliderListener {

		public double onValueChanged(double value);

	}

	public static interface GSSliderFormatter {
		
		public String formatValue(double value);
		
	}
}
