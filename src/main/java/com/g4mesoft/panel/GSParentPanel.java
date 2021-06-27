package com.g4mesoft.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.g4mesoft.renderer.GSIRenderer2D;

public class GSParentPanel extends GSPanel {

	protected final List<GSPanel> children;
	
	private GSILayoutManager layoutManager;
	private boolean minimumSizeSet;
	private boolean preferredSizeSet;
	
	public GSParentPanel() {
		children = new ArrayList<>();
		
		layoutManager = null;
		minimumSizeSet = preferredSizeSet = false;
	}
	
	@Override
	protected void layout() {
		if (layoutManager != null)
			layoutManager.layoutChildren(this);
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible != isVisible()) {
			super.setVisible(visible);
		
			for (GSPanel panel : children)
				panel.setVisible(visible);
		}
	}
	
	@Override
	public void add(GSPanel panel) {
		children.add(panel);

		panel.onAdded(this);
		panel.setVisible(isVisible());
	}
	
	@Override
	public void remove(GSPanel panel) {
		if (children.remove(panel))
			onChildRemoved(panel);
	}
	
	@Override
	public void removeAll() {
		while (!children.isEmpty())
			onChildRemoved(children.remove(children.size() - 1));
	}
	
	protected void onChildRemoved(GSPanel child) {
		child.setVisible(false);
		child.onRemoved(this);
	}
	
	@Override
	public void update() {
		super.update();

		updateChildren();
	}

	protected void updateChildren() {
		for (GSPanel child : getChildren())
			child.update();
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		renderChildren(renderer);
	}
	
	protected void renderChildren(GSIRenderer2D renderer) {
		for (GSPanel child : getChildren()) {
			child.preRender(renderer);
			child.render(renderer);
			child.postRender(renderer);
		}
	}
	
	@Override
	public GSPanel getChildAt(int x, int y) {
		// Traverse children in reverse direction to match
		// the order that they are drawn (last on top).
		for (int i = children.size() - 1; i >= 0; i--) {
			GSPanel child = children.get(i);
			if (child.isInBounds(x, y))
				return child;
		}
		
		return null;
	}

	public List<GSPanel> getChildren() {
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
