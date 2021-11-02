package com.g4mesoft.panel;

import java.util.function.Function;

import com.g4mesoft.util.GSMathUtil;

public final class GSFloatLayoutProperty extends GSBasicLayoutProperty<Float> {

	private float minValue;
	private float maxValue;
	
	public GSFloatLayoutProperty(String name, float defValue) {
		this(name, defValue, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
	}
	
	public GSFloatLayoutProperty(String name, float defValue, float minValue, float maxValue) {
		super(name, defValue);
	
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public GSFloatLayoutProperty(String name, Function<GSPanel, Float> defValueFunction) {
		this(name, defValueFunction, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
	}

	public GSFloatLayoutProperty(String name, Function<GSPanel, Float> defValueFunction, float minValue, float maxValue) {
		super(name, defValueFunction);

		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	@Override
	public Float validate(Float value) {
		return GSMathUtil.clamp(value, minValue, maxValue);
	}
	
	public float getMinValue() {
		return minValue;
	}
	
	public float getMaxValue() {
		return maxValue;
	}
}
