package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.util.GSMathUtil;

public class GSIntegerSetting extends GSSetting<Integer> {

	private volatile int value;
	
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

		if (interval <= 0)
			throw new IllegalArgumentException("interval must be positive");
		if (minValue > maxValue)
			throw new IllegalArgumentException("minValue must not be greater than maxValue");
		if (defaultValue < minValue || defaultValue > maxValue)
			throw new IllegalArgumentException("defaultValue is not in range");
		
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.interval = interval;
		
		this.value = adjustValue(defaultValue);
	}
	
	@Override
	public Integer get() {
		return Integer.valueOf(value);
	}

	/**
	 * @deprecated Replaced by {@link #get()}
	 * 
	 * @return the value of this setting
	 */
	@Deprecated
	public Integer getValue() {
		return get();
	}
	
	@Override
	public GSIntegerSetting set(Integer value) {
		return set(value.intValue());
	}
	
	/**
	 * @deprecated Replaced by {@link #set(Integer)}
	 * 
	 * @param value - the new value of this setting
	 * 
	 * @return this setting
	 */
	@Deprecated
	public GSIntegerSetting setValue(Integer value) {
		return set(value);
	}
	
	/**
	 * Changes the value of this setting to the new value specified. If the value
	 * is not on an exact value between two intervals, it will be rounded to the
	 * nearest interval border.
	 * 
	 * @param value - the new value of this setting, before adjustment.
	 * 
	 * @return this integer setting
	 */
	public GSIntegerSetting set(int value) {
		int newValue = adjustValue(value);
		if (newValue != this.value) {
			this.value = newValue;
			notifyOwnerChange();
		}
		
		return this;
	}
	
	/* Adjust value such that it rounds to the nearest value between intervals */
	private int adjustValue(int value) {
		if (interval / 2 != 0) {
			int deviation = value % interval;

			value -= deviation;
			if (Math.abs(deviation) > interval / 2)
				value = (deviation < 0) ? (value - interval) : (value + interval);
		}
		
		return GSMathUtil.clamp(value, minValue, maxValue);
	}

	/**
	 * Increments this setting by exactly one interval. Invoking this method is
	 * equivalent to the following code snippet:
	 * <pre>
	 *     setting.increment(1);
	 * </pre>
	 * 
	 * @return this integer setting
	 * 
	 * @see #increment(int)
	 */
	public GSIntegerSetting increment() {
		return increment(1);
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
	 * @return this integer setting
	 * 
	 * @see #decrement(int)
	 * @see #setValue(int)
	 */
	public GSIntegerSetting increment(int count) {
		if (count <= 0)
			throw new IllegalArgumentException("count must be positive!");
		// Store new value in long to ensure no overflow.
		long newValue = value + interval * count;
		if (newValue > maxValue)
			newValue = minValue;
		// No overflow since we have the above check.
		return set((int)newValue);
	}

	/**
	 * Decrements this setting by exactly one interval. Invoking this method is
	 * equivalent to the following code snippet:
	 * <pre>
	 *     setting.decrement(1);
	 * </pre>
	 * 
	 * @return this integer setting
	 * 
	 * @see #decrement(int)
	 */
	public GSIntegerSetting decrement() {
		return decrement(1);
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
	 * @return this integer setting
	 * 
	 * @see #increment(int)
	 * @see #setValue(int)
	 */
	public GSIntegerSetting decrement(int count) {
		if (count <= 0)
			throw new IllegalArgumentException("count must be positive!");
		// Store new value in long to ensure no underflow.
		long newValue = value - interval * count;
		if (newValue < minValue)
			newValue = maxValue;
		// No underflow since we have the above check.
		return set((int)newValue);
	}
	
	@Override
	public boolean isDefault() {
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
			if (defaultValue != integerSetting.getDefault())
				return false;
			if (minValue != integerSetting.getMin())
				return false;
			if (maxValue != integerSetting.getMax())
				return false;
			if (interval != integerSetting.getInterval())
				return false;
			return true;
		}
		
		return false;
	}
	
	/**
	 * @return the minimum value this setting can have
	 */
	public int getMin() {
		return minValue;
	}
	
	/**
	 * @return the maximum value this setting can have
	 */
	public int getMax() {
		return maxValue;
	}

	/**
	 * @deprecated Replaced by {@link #getMin()}
	 * 
	 * @return the minimum value this setting can have
	 */
	@Deprecated
	public final int getMinValue() {
		return getMin();
	}
	
	/**
	 * @deprecated Replaced by {@link #getMax()}
	 * 
	 * @return the maximum value this setting can have
	 */
	@Deprecated
	public final int getMaxValue() {
		return getMax();
	}

	/**
	 * @return The interval which determines which values this setting can
	 *         take. The setting is always a multiple of the interval, unless
	 *         it is either the maximum or minimum value and those are not a
	 *         multiple of the interval.
	 */
	public int getInterval() {
		return interval;
	}

	@Override
	public GSSetting<Integer> copySetting() {
		return new GSIntegerSetting(name, defaultValue, minValue, maxValue, interval, visibleInGui).set(value).setEnabledInGui(isEnabledInGui());
	}
}
