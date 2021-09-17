package com.g4mesoft.panel;

import java.util.function.Function;

import com.g4mesoft.util.GSMathUtil;

public final class GSIntLayoutProperty extends GSBasicLayoutProperty<Integer> {

	private int minValue;
	private int maxValue;
	
	public GSIntLayoutProperty(String name, int defValue) {
		this(name, defValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public GSIntLayoutProperty(String name, int defValue, int minValue, int maxValue) {
		super(name, defValue);
	
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public GSIntLayoutProperty(String name, Function<GSPanel, Integer> defValueFunction) {
		this(name, defValueFunction, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public GSIntLayoutProperty(String name, Function<GSPanel, Integer> defValueFunction, int minValue, int maxValue) {
		super(name, defValueFunction);

		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	@Override
	public Integer validate(Integer value) {
		return GSMathUtil.clamp(value, minValue, maxValue);
	}
	
	public int getMinValue() {
		return minValue;
	}
	
	public int getMaxValue() {
		return maxValue;
	}
}
