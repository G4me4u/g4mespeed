package com.g4mesoft.panel;

import java.util.Map;

public class GSSpacingLayoutProperty implements GSILayoutProperty<GSSpacing> {

	private final GSILayoutProperty<Integer> topProperty;
	private final GSILayoutProperty<Integer> leftProperty;
	private final GSILayoutProperty<Integer> bottomProperty;
	private final GSILayoutProperty<Integer> rightProperty;

	public GSSpacingLayoutProperty(GSILayoutProperty<Integer> topProperty, GSILayoutProperty<Integer> leftProperty, GSILayoutProperty<Integer> bottomProperty, GSILayoutProperty<Integer> rightProperty) {
		if (topProperty == null)
			throw new IllegalArgumentException("topProperty is null");
		if (leftProperty == null)
			throw new IllegalArgumentException("leftProperty is null");
		if (bottomProperty == null)
			throw new IllegalArgumentException("bottomProperty is null");
		if (rightProperty == null)
			throw new IllegalArgumentException("rightProperty is null");
		
		this.topProperty = topProperty;
		this.leftProperty = leftProperty;
		this.bottomProperty = bottomProperty;
		this.rightProperty = rightProperty;
	}
	
	@Override
	public GSSpacing get(Map<GSILayoutProperty<?>, Object> map, GSPanel panel) {
		Integer top = topProperty.get(map, panel);
		Integer left = leftProperty.get(map, panel);
		Integer bottom = bottomProperty.get(map, panel);
		Integer right = rightProperty.get(map, panel);
		return new GSSpacing(top, left, bottom, right);
	}

	@Override
	public GSSpacing remove(Map<GSILayoutProperty<?>, Object> map, GSPanel panel) {
		Integer top = topProperty.remove(map, panel);
		Integer left = leftProperty.remove(map, panel);
		Integer bottom = bottomProperty.remove(map, panel);
		Integer right = rightProperty.remove(map, panel);
		if (top == null && left == null && bottom == null && right == null)
			return null;
		if (top == null)
			top = topProperty.computeDefaultValue(panel);
		if (left == null)
			left = leftProperty.computeDefaultValue(panel);
		if (bottom == null)
			bottom = bottomProperty.computeDefaultValue(panel);
		if (right == null)
			right = rightProperty.computeDefaultValue(panel);
		return new GSSpacing(top, left, bottom, right);
	}
	
	@Override
	public void put(Map<GSILayoutProperty<?>, Object> map, GSSpacing value, GSPanel panel) {
		topProperty.put(map, value.getTop(), panel);
		leftProperty.put(map, value.getLeft(), panel);
		bottomProperty.put(map, value.getBottom(), panel);
		rightProperty.put(map, value.getRight(), panel);
	}
	
	@Override
	public GSSpacing computeDefaultValue(GSPanel panel) {
		Integer top = topProperty.computeDefaultValue(panel);
		Integer left = leftProperty.computeDefaultValue(panel);
		Integer bottom = bottomProperty.computeDefaultValue(panel);
		Integer right = rightProperty.computeDefaultValue(panel);
		return new GSSpacing(top, left, bottom, right);
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
		if (obj instanceof GSSpacingLayoutProperty) {
			GSSpacingLayoutProperty property = (GSSpacingLayoutProperty)obj;
			return topProperty.equals(property.topProperty) &&
			       leftProperty.equals(property.leftProperty) &&
			       bottomProperty.equals(property.bottomProperty) &&
			       rightProperty.equals(property.rightProperty);
		}
		return false;
	}
}
