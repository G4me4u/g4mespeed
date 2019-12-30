package com.g4mesoft.gui.widget;

import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.gui.widget.SliderWidget;

public class GSSliderWidget extends SliderWidget {

	public static final int SLIDER_HEIGHT = 20;

	public static final int MAX_WIDTH = 196;

	private final GSSliderListener listener;
	private final GSSliderFormatter formatter;

	public GSSliderWidget(int x, int y, int width, double value, GSSliderListener listener, GSSliderFormatter formatter) {
		super(null, x, y, width, SLIDER_HEIGHT, value);

		this.listener = listener;
		this.formatter = formatter;
		
		updateMessage();
	}

	@GSCoreOverride
	@Override
	protected void updateMessage() {
		setMessage(formatter.formatValue(value));
	}

	@GSCoreOverride
	@Override
	protected void applyValue() {
		if (listener != null)
			listener.onValueChanged(value);
	}

	public void setValueSilent(double value) {
		this.value = GSMathUtils.clamp(value, 0.0, 1.0);
		
		updateMessage();
	}
	
	@GSCoreOverride
	@Override
	public boolean isHovered() {
		// An oversight when the developers made the sliders
		// is that they do not react properly when disabled.
		return active && super.isHovered();
	}

	public static interface GSSliderListener {

		public void onValueChanged(double value);

	}

	public static interface GSSliderFormatter {
		
		public String formatValue(double value);
		
	}
}
