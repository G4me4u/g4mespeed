package com.g4mesoft.panel;

import java.util.Map;
import java.util.function.Function;

import com.google.common.base.Functions;

class GSBasicLayoutProperty<T> implements GSILayoutProperty<T> {

	private final String name;
	private final Function<GSPanel, T> defValueFunction;
	private final Function<T, T> copyConstructor;

	public GSBasicLayoutProperty(String name, T defValue) {
		this(name, defValue, Functions.identity());
	}
	
	public GSBasicLayoutProperty(String name, T defValue, Function<T, T> copyConstructor) {
		this(name, (ignore -> copyConstructor.apply(defValue)), copyConstructor);
	}

	public GSBasicLayoutProperty(String name, Function<GSPanel, T> defValueFunction) {
		this(name, defValueFunction, Functions.identity());
	}

	public GSBasicLayoutProperty(String name, Function<GSPanel, T> defValueFunction, Function<T, T> copyConstructor) {
		if (name == null)
			throw new IllegalArgumentException("name is null");
		if (defValueFunction == null)
			throw new IllegalArgumentException("default value function is null");
		if (copyConstructor == null)
			throw new IllegalArgumentException("copyConstructor is null");

		this.name = name;
		this.defValueFunction = defValueFunction;
		this.copyConstructor = copyConstructor;
	}
	
	@Override
	public T get(Map<GSILayoutProperty<?>, Object> map, GSPanel panel) {
		Object value = map.get(this);
		if (value == null)
			return computeDefaultValue(panel);
		return castUnchecked(value);
	}

	@Override
	public T remove(Map<GSILayoutProperty<?>, Object> map, GSPanel panel) {
		return castUnchecked(map.remove(this));
	}
	
	@Override
	public void put(Map<GSILayoutProperty<?>, Object> map, T value, GSPanel panel) {
		map.put(this, validate(copy(value)));
	}
	
	@Override
	public T computeDefaultValue(GSPanel panel) {
		return defValueFunction.apply(panel);
	}

	@SuppressWarnings("unchecked")
	public T castUnchecked(Object value) {
		return (T)value;
	}
	
	protected T validate(T value) {
		return value;
	}
	
	public T copy(T value) {
		return copyConstructor.apply(value);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof GSBasicLayoutProperty)
			return name.equals(((GSBasicLayoutProperty<?>)obj).name);
		return false;
	}
}
