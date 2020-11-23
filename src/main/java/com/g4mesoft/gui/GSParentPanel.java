package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.g4mesoft.renderer.GSIRenderer2D;

public class GSParentPanel extends GSPanel implements GSIParentElement {

	private final List<GSIElement> children;
	
	private GSILayoutManager layoutManager;
	private boolean minimumSizeSet;
	private boolean preferredSizeSet;
	
	public GSParentPanel() {
		children = new ArrayList<>();
		
		layoutManager = null;
		minimumSizeSet = preferredSizeSet = false;
	}
	
	@Override
	public void layout() {
		if (layoutManager != null)
			layoutManager.layoutChildren(this);
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
		if (children.remove(element))
			onChildRemoved(element);
	}
	
	@Override
	public void removeAll() {
		while (!children.isEmpty())
			onChildRemoved(children.remove(children.size() - 1));
	}
	
	protected void onChildRemoved(GSIElement child) {
		child.setVisible(false);
		child.onRemoved(this);
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
	
	public GSILayoutManager getLayoutManager() {
		return layoutManager;
	}

	public void setLayoutManager(GSILayoutManager layoutManager) {
		this.layoutManager = layoutManager;
	
		requestLayout();
	}
	
	@Override
	public GSDimension getMinimumSize() {
		if (!minimumSizeSet && minimumSize == null && layoutManager != null)
			minimumSize = layoutManager.getMinimumSize(this);
		
		return super.getMinimumSize();
	}
	
	@Override
	public void setMinimumSize(GSDimension minimumSize) {
		super.setMinimumSize(minimumSize);
		
		minimumSizeSet = (minimumSize != null);
	}

	@Override
	public GSDimension getPreferredSize() {
		if (!preferredSizeSet && preferredSize == null && layoutManager != null)
			preferredSize = layoutManager.getPreferredSize(this);
		
		return super.getPreferredSize();
	}
	
	@Override
	public void setPreferredSize(GSDimension preferredSize) {
		super.setPreferredSize(preferredSize);
		
		preferredSizeSet = (preferredSize != null);
	}
}
