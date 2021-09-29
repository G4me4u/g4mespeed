package com.g4mesoft.panel;

public class GSColorPickerLayoutManager implements GSILayoutManager {

	@Override
	public GSDimension getMinimumInnerSize(GSParentPanel parent) {
		return getSize(parent, GSLayoutProperties.MINIMUM_SIZE);
	}

	@Override
	public GSDimension getPreferredInnerSize(GSParentPanel parent) {
		return getSize(parent, GSLayoutProperties.PREFERRED_SIZE);
	}
	
	private GSDimension getSize(GSParentPanel parent, GSILayoutProperty<GSDimension> sizeProperty) {
		GSColorPicker colorPicker = (GSColorPicker)parent;

		GSPanel colorWheel = colorPicker.getColorWheel();
		GSDimension cwSize = colorWheel.getProperty(sizeProperty);

		GSPanel brightnessSlider = colorPicker.getBrightnessSlider();
		GSDimension bsSize = brightnessSlider.getProperty(sizeProperty);

		int width  = cwSize.getWidth() + bsSize.getWidth();
		int height = Math.max(cwSize.getHeight(), bsSize.getHeight());
		
		return new GSDimension(width, height);
	}

	@Override
	public void layoutChildren(GSParentPanel parent) {
		GSColorPicker colorPicker = (GSColorPicker)parent;

		int availW = colorPicker.getInnerWidth();
		int availH = colorPicker.getInnerHeight();

		GSPanel brightnessSlider = colorPicker.getBrightnessSlider();
		GSPanel colorWheel = colorPicker.getColorWheel();
		
		int bsPrefW = brightnessSlider.getProperty(GSLayoutProperties.PREFERRED_WIDTH);
		int bsw = Math.min(availW, bsPrefW);
		int bsx = Math.max(0, availW - bsw);
		brightnessSlider.setOuterBounds(bsx, 0, bsw, availH);

		int cws = Math.max(0, Math.min(bsx, availH));
		colorWheel.setOuterBounds(0, (availH - cws) / 2, cws, cws);
	}
}
