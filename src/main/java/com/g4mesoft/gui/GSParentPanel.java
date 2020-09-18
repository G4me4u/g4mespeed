package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.g4mesoft.gui.renderer.GSIRenderer2D;

public class GSParentPanel extends GSPanel implements GSIParentElement {

	private final List<GSIElement> children;
	
	public GSParentPanel() {
		children = new ArrayList<>();
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible != isVisible()) {
			super.setVisible(visible);
		
			for (GSIElement element : children)
				element.setVisible(visible);
		}
	}
	
	@Override
	public void add(GSIElement element) {
		children.add(element);

		element.onAdded(this);
		element.setVisible(isVisible());
	}
	
	@Override
	public void remove(GSIElement element) {
		if (children.remove(element)) {
			element.setVisible(false);
			element.onRemoved(this);
		}
	}
	
	@Override
	public void removeAll() {
		while (!children.isEmpty())
			remove(children.get(children.size() - 1));
	}
	
	@Override
	public void update() {
		super.update();

		updateChildren();
	}

	protected void updateChildren() {
		for (GSIElement element : getChildren())
			element.update();
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		renderChildren(renderer);
	}
	
	protected void renderChildren(GSIRenderer2D renderer) {
		for (GSIElement element : getChildren()) {
			element.preRender(renderer);
			element.render(renderer);
			element.postRender(renderer);
		}
	}
	
	@Override
	public GSIElement getChildAt(int x, int y) {
		for (GSIElement child : children) {
			if (child.isInBounds(x, y))
				return child;
		}
		
		return null;
	}

	@Override
	public List<GSIElement> getChildren() {
		return Collections.unmodifiableList(children);
	}
}
