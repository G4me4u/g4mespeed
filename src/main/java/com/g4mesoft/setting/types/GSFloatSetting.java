package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.util.GSMathUtils;

public class GSFloatSetting extends GSSetting<Float> {

	private float value;
	
	private final float minValue;
	private final float interval;
	private final float maxValue;

	public GSFloatSetting(String name, float defaultValue) {
		this(name, defaultValue, true);
	}

	public GSFloatSetting(String name, float defaultValue, boolean availableInGui) {
		this(name, defaultValue, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, availableInGui);
	}

	public GSFloatSetting(String name, float defaultValue, float minValue, float maxValue) {
		this(name, defaultValue, minValue, maxValue, true);
	}

	public GSFloatSetting(String name, float defaultValue, float minValue, float maxValue, boolean availableInGui) {
		this(name, defaultValue, minValue, maxValue, 0.0f, availableInGui);
	}

	public GSFloatSetting(String name, float defaultValue, float minValue, float maxValue, float interval) {
		this(name, defaultValue, minValue, maxValue, interval, true);
	}
	
	public GSFloatSetting(String name, float defaultValue, float minValue, float maxValue, float interval, boolean availableInGui) {
		super(name, defaultValue, availableInGui);

		this.minValue = minValue;
		this.interval = interval;
		this.maxValue = maxValue;
		
		this.value = adjustValue(defaultValue);
	}
	
	@Override
	public Float getValue() {
		return Float.valueOf(value);
	}

	@Override
	public GSFloatSetting setValue(Float value) {
		float newValue = adjustValue(value.floatValue());
		if (newValue != this.value) {
			this.value = newValue;
			notifyOwnerChange();
		}
		
		return this;
	}
	
	private float adjustValue(float value) {
		if (!GSMathUtils.equalsApproximate(interval * 0.5f, 0.0f)) {
			float deviation = value % interval;

			value -= deviation;
			if (Math.abs(deviation) > interval * 0.5f)
				value = (deviation < 0.0f) ? (value - interval) : (value + interval);
		}
		
		return GSMathUtils.clamp(value, minValue, maxValue);
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSFloatSetting;
	}
	
	public float getMinValue() {
		return minValue;
	}
	
	public float getInterval() {
		return interval;
	}

	public float getMaxValue() {
		return maxValue;
	}

	@Override
	public GSSetting<Float> copySetting() {
		return new GSFloatSetting(name, defaultValue, minValue, maxValue, interval, availableInGui).setValue(value);
	}
}
