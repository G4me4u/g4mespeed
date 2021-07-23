package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.util.GSMathUtil;

public class GSIntegerSetting extends GSSetting<Integer> {

	private int value;
	
	private final int minValue;
	private final int maxValue;
	private final int interval;

	public GSIntegerSetting(String name, int defaultValue) {
		this(name, defaultValue, true);
	}

	public GSIntegerSetting(String name, int defaultValue, boolean visibleInGui) {
		this(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, visibleInGui);
	}

	public GSIntegerSetting(String name, int defaultValue, int minValue, int maxValue) {
		this(name, defaultValue, minValue, maxValue, true);
	}

	public GSIntegerSetting(String name, int defaultValue, int minValue, int maxValue, boolean visibleInGui) {
		this(name, defaultValue, minValue, maxValue, 1, visibleInGui);
	}
	
	public GSIntegerSetting(String name, int defaultValue, int minValue, int maxValue, int interval) {
		this(name, defaultValue, minValue, maxValue, interval, true);
	}

	public GSIntegerSetting(String name, int defaultValue, int minValue, int maxValue, int interval, boolean visibleInGui) {
		super(name, defaultValue, visibleInGui);

		this.minValue = minValue;
		this.maxValue = maxValue;
		this.interval = interval;
		
		this.value = adjustValue(defaultValue);
	}
	
	@Override
	public Integer getValue() {
		return Integer.valueOf(value);
	}

	@Override
	public GSIntegerSetting setValue(Integer value) {
		int newValue = adjustValue(value.intValue());
		if (newValue != this.value) {
			this.value = newValue;
			notifyOwnerChange();
		}
		
		return this;
	}
	
	private int adjustValue(int value) {
		if (interval / 2 != 0) {
			int deviation = value % interval;

			value -= deviation;
			if (Math.abs(deviation) > interval / 2)
				value = (deviation < 0) ? (value - interval) : (value + interval);
		}
		
		return GSMathUtil.clamp(value, minValue, maxValue);
	}

	@Override
	public boolean isDefaultValue() {
		return defaultValue.intValue() == value;
	}
	
	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSIntegerSetting;
	}
	
	@Override
	public boolean isSameSetting(GSSetting<?> other) {
		if (other instanceof GSIntegerSetting) {
			GSIntegerSetting integerSetting = (GSIntegerSetting)other;
			if (defaultValue != integerSetting.getDefaultValue())
				return false;
			if (minValue != integerSetting.getMinValue())
				return false;
			if (maxValue != integerSetting.getMaxValue())
				return false;
			if (interval != integerSetting.getInterval())
				return false;
			return true;
		}
		
		return false;
	}
	
	public int getMinValue() {
		return minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public int getInterval() {
		return interval;
	}

	@Override
	public GSSetting<Integer> copySetting() {
		return new GSIntegerSetting(name, defaultValue, minValue, maxValue, interval, visibleInGui).setValue(value).setEnabledInGui(isEnabledInGui());
	}
}
