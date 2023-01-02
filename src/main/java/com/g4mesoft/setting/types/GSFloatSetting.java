package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.util.GSMathUtil;

public class GSFloatSetting extends GSSetting<Float> {

	private volatile float value;
	
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

		if (Float.isNaN(interval) || interval <= 0.0f)
			throw new IllegalArgumentException("interval must be positive");
		if (Float.isNaN(minValue))
			throw new IllegalArgumentException("minValue is NaN");
		if (Float.isNaN(maxValue))
			throw new IllegalArgumentException("maxValue is NaN");
		if (minValue > maxValue)
			throw new IllegalArgumentException("minValue must not be greater than maxValue");
		if (Float.isNaN(defaultValue))
			throw new IllegalArgumentException("defaultValue is NaN");
		
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.interval = interval;
		
		this.value = adjustValue(defaultValue);
	}
	
	@Override
	public Float get() {
		return Float.valueOf(value);
	}

	@Override
	public GSFloatSetting set(Float value) {
		return set(value.floatValue());
	}
	
	/**
	 * Changes the value of this setting to the new value specified. If the value
	 * is not on an approximate value between two intervals, it will be rounded to
	 * the nearest interval border.
	 * 
	 * @param value - the new value of this setting, before adjustment.
	 * 
	 * @return this float setting
	 */
	public GSFloatSetting set(float value) {
		float newValue = adjustValue(value);
		if (newValue != this.value) {
			this.value = newValue;
			notifyOwnerChange();
		}
		
		return this;
	}
	
	/* Adjust value such that it rounds to the nearest value between intervals */
	private float adjustValue(float value) {
		if (!GSMathUtil.equalsApproximate(interval * 0.5f, 0.0f)) {
			float deviation = value % interval;

			value -= deviation;
			if (Math.abs(deviation) > interval * 0.5f)
				value = (deviation < 0.0f) ? (value - interval) : (value + interval);
		}
		
		return GSMathUtil.clamp(value, minValue, maxValue);
	}

	/**
	 * Increments this setting by approximately one interval. Invoking this method
	 * is equivalent to the following code snippet:
	 * <pre>
	 *     setting.incrementValue(1);
	 * </pre>
	 * 
	 * @return this float setting
	 * 
	 * @see #incrementValue(int)
	 */
	public GSFloatSetting incrementValue() {
		return incrementValue(1);
	}
	
	/**
	 * Increments the value stored in this setting by the interval specified by
	 * {@link #getInterval()}, {@code count} times. If the new value is above the
	 * maximum value of this setting, it will wrap back to the minimum value.
	 * <br/><br/>
	 * Note: listeners are notified only if the value changed.
	 * 
	 * @param count - the number of intervals to increment by.
	 * 
	 * @return this float setting
	 * 
	 * @see #decrementValue(int)
	 * @see #setValue(float)
	 */
	public GSFloatSetting incrementValue(int count) {
		float newValue = value + interval * count;
		if (newValue > maxValue)
			newValue = minValue;
		return set(newValue);
	}

	/**
	 * Decrements this setting by approximately one interval. Invoking this method
	 * is equivalent to the following code snippet:
	 * <pre>
	 *     setting.decrementValue(1);
	 * </pre>
	 * 
	 * @return this float setting
	 * 
	 * @see #decrementValue(int)
	 */
	public GSFloatSetting decrementValue() {
		return decrementValue(1);
	}

	/**
	 * Decrements the value stored in this setting by the interval specified by
	 * {@link #getInterval()}, {@code count} times. If the new value is below the
	 * minimum value of this setting, it will wrap back to the maximum value.
	 * <br/><br/>
	 * Note: listeners are notified only if the value changed.
	 * 
	 * @param count - the number of intervals to decrement by.
	 * 
	 * @return this float setting
	 * 
	 * @see #incrementValue(int)
	 * @see #setValue(float)
	 */
	public GSFloatSetting decrementValue(int count) {
		float newValue = value - interval * count;
		if (newValue < minValue)
			newValue = maxValue;
		return set(newValue);
	}
	
	@Override
	public boolean isDefault() {
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
			if (!GSMathUtil.equalsApproximate(defaultValue, floatSetting.getDefault()))
				return false;
			if (!GSMathUtil.equalsApproximate(minValue, floatSetting.getMin()))
				return false;
			if (!GSMathUtil.equalsApproximate(maxValue, floatSetting.getMax()))
				return false;
			if (!GSMathUtil.equalsApproximate(interval, floatSetting.getInterval()))
				return false;
			return true;
		}
		
		return false;
	}
	
	/**
	 * @return the minimum value this setting can have
	 */
	public float getMin() {
		return minValue;
	}
	
	/**
	 * @return the maximum value this setting can have
	 */
	public float getMax() {
		return maxValue;
	}

	/**
	 * @deprecated Replaced by {@link #getMin()}
	 * 
	 * @return the minimum value this setting can have
	 */
	@Deprecated
	public final float getMinValue() {
		return getMin();
	}
	
	/**
	 * @deprecated Replaced by {@link #getMax()}
	 * 
	 * @return the maximum value this setting can have
	 */
	@Deprecated
	public final float getMaxValue() {
		return getMax();
	}

	/**
	 * @return The interval which determines which values this setting can
	 *         take. The setting is always a multiple of the interval, unless
	 *         it is either the maximum or minimum value and those are not a
	 *         multiple of the interval.
	 */
	public float getInterval() {
		return interval;
	}

	@Override
	public GSSetting<Float> copySetting() {
		return new GSFloatSetting(name, defaultValue, minValue, maxValue, interval, visibleInGui).set(value).setEnabledInGui(isEnabledInGui());
	}
}
