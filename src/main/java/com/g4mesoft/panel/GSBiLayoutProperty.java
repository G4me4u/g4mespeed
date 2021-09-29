package com.g4mesoft.panel;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GSBiLayoutProperty<T, L, R> implements GSILayoutProperty<T> {

	private final GSILayoutProperty<L> leftProperty;
	private final GSILayoutProperty<R> rightProperty;
	private final BiFunction<L, R, T> constructor;
	private final Function<T, L> leftGetter;
	private final Function<T, R> rightGetter;

	public GSBiLayoutProperty(GSILayoutProperty<L> leftProperty, GSILayoutProperty<R> rightProperty, BiFunction<L, R, T> constructor, Function<T, L> leftGetter, Function<T, R> rightGetter) {
		if (leftProperty == null)
			throw new IllegalArgumentException("leftProperty is null");
		if (rightProperty == null)
			throw new IllegalArgumentException("rightProperty is null");
		if (constructor == null)
			throw new IllegalArgumentException("constructor is null");
		if (leftGetter == null)
			throw new IllegalArgumentException("leftGetter is null");
		if (rightGetter == null)
			throw new IllegalArgumentException("rightGetter is null");
		
		this.leftProperty = leftProperty;
		this.rightProperty = rightProperty;
		this.constructor = constructor;
		this.leftGetter = leftGetter;
		this.rightGetter = rightGetter;
	}
	
	@Override
	public T get(Map<GSILayoutProperty<?>, Object> map, GSPanel panel) {
		L left = leftProperty.get(map, panel);
		R right = rightProperty.get(map, panel);
		return constructor.apply(left, right);
	}

	@Override
	public T remove(Map<GSILayoutProperty<?>, Object> map, GSPanel panel) {
		L left = leftProperty.remove(map, panel);
		R right = rightProperty.remove(map, panel);
		if (left == null && right == null)
			return null;
		if (left == null)
			left = leftProperty.computeDefaultValue(panel);
		if (right == null)
			right = rightProperty.computeDefaultValue(panel);
		return constructor.apply(left, right);
	}
	
	@Override
	public void put(Map<GSILayoutProperty<?>, Object> map, T value, GSPanel panel) {
		leftProperty.put(map, leftGetter.apply(value), panel);
		rightProperty.put(map, rightGetter.apply(value), panel);
	}
	
	@Override
	public T computeDefaultValue(GSPanel panel) {
		L left = leftProperty.computeDefaultValue(panel);
		R right = rightProperty.computeDefaultValue(panel);
		return constructor.apply(left, right);
	}

	@Override
	public int hashCode() {
		int hash = leftProperty.hashCode();
		hash = 31 * hash + rightProperty.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof GSBiLayoutProperty) {
			GSBiLayoutProperty<?, ?, ?> property = (GSBiLayoutProperty<?, ?, ?>)obj;
			return leftProperty.equals(property.leftProperty) &&
			       rightProperty.equals(property.rightProperty);
		}
		return false;
	}
}
