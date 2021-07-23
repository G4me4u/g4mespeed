package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.util.GSMathUtil;

public class GSFloatSetting extends GSSetting<Float> {

	private float value;
	
	private final float minValue;
	private final float maxValue;
	private final float interval;

	public GSFloatSetting(String name, float defaultValue) {
		this(name, defaultValue, true);
	}

	public GSFloatSetting(String name, float defaultValue, boolean visibleInGui) {
		this(name, defaultValue, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, visibleInGui);
	}

	public GSFloatSetting(String name, float defaultValue, float minValue, float maxValue) {
		this(name, defaultValue, minValue, maxValue, true);
	}

	public GSFloatSetting(String name, float defaultValue, float minValue, float maxValue, boolean visibleInGui) {
		this(name, defaultValue, minValue, maxValue, 0.0f, visibleInGui);
	}

	public GSFloatSetting(String name, float defaultValue, float minValue, float maxValue, float interval) {
		this(name, defaultValue, minValue, maxValue, interval, true);
	}
	
	public GSFloatSetting(String name, float defaultValue, float minValue, float maxValue, float interval, boolean visibleInGui) {
		super(name, defaultValue, visibleInGui);

		this.minValue = minValue;
		this.maxValue = maxValue;
		this.interval = interval;
		
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
		if (!GSMathUtil.equalsApproximate(interval * 0.5f, 0.0f)) {
			float deviation = value % interval;

			value -= deviation;
			if (Math.abs(deviation) > interval * 0.5f)
				value = (deviation < 0.0f) ? (value - interval) : (value + interval);
		}
		
		return GSMathUtil.clamp(value, minValue, maxValue);
	}
	
	@Override
	public boolean isDefaultValue() {
		return GSMathUtil.equalsApproximate(defaultValue.floatValue(), value);
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSFloatSetting;
	}

	@Override
	public boolean isSameSetting(GSSetting<?> other) {
		if (other instanceof GSFloatSetting) {
			GSFloatSetting floatSetting = (GSFloatSetting)other;
			if (!GSMathUtil.equalsApproximate(defaultValue, floatSetting.getDefaultValue()))
				return false;
			if (!GSMathUtil.equalsApproximate(minValue, floatSetting.getMinValue()))
				return false;
			if (!GSMathUtil.equalsApproximate(maxValue, floatSetting.getMaxValue()))
				return false;
			if (!GSMathUtil.equalsApproximate(interval, floatSetting.getInterval()))
				return false;
			return true;
		}
		
		return false;
	}
	
	public float getMinValue() {
		return minValue;
	}
	
	public float getMaxValue() {
		return maxValue;
	}

	public float getInterval() {
		return interval;
	}

	@Override
	public GSSetting<Float> copySetting() {
		return new GSFloatSetting(name, defaultValue, minValue, maxValue, interval, visibleInGui).setValue(value).setEnabledInGui(isEnabledInGui());
	}
}
