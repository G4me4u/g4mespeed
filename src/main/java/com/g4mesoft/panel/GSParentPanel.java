package com.g4mesoft.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.g4mesoft.renderer.GSIRenderer2D;

public class GSParentPanel extends GSPanel {

	protected final List<GSPanel> children;
	
	private GSILayoutManager layoutManager;

	public GSParentPanel() {
		this(null);
	}
	
	public GSParentPanel(GSILayoutManager layoutManager) {
		children = new ArrayList<>();
		
		setLayoutManager(layoutManager);
	}
	
	@Override
	protected void layout() {
		if (layoutManager != null)
			layoutManager.layoutChildren(this);
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible != isVisible()) {
			for (GSPanel panel : children)
				panel.setVisible(visible);

			super.setVisible(visible);
		}
	}
	
	@Override
	public void add(GSPanel panel) {
		children.add(panel);

		panel.onAdded(this);
		panel.setVisible(isVisible());

		invalidate();
	}
	
	@Override
	public void remove(GSPanel panel) {
		if (children.remove(panel)) {
			onChildRemoved(panel);
			invalidate();
		}
	}

	@Override
	public void remove(int index) {
		GSPanel panel = children.remove(index);
		if (panel != null) {
			onChildRemoved(panel);
			invalidate();
		}
	}
	
	@Override
	public void removeAll() {
		if (!children.isEmpty()) {
			int count = children.size();
			do {
				// NOTE: Remove last to ensure we do not
				// have an O(n^2) removeAll algorithm.
				// For reference, see ArrayList#remove.
				onChildRemoved(children.remove(--count));
			} while (count != 0);
		
			invalidate();
		}
	}

	protected void onChildRemoved(GSPanel child) {
		child.setVisible(false);
		child.onRemoved(this);
	}
	
	@Override
	public GSPanel get(int index) {
		return children.get(index);
	}
	
	@Override
	public GSPanel getChildAt(int x, int y) {
		// Traverse children in reverse direction to match the order
		// that they are drawn (first to last, bottom to top).
		for (int i = children.size() - 1; i >= 0; i--) {
			GSPanel child = children.get(i);
			int rx = x - child.getInnerX();
			int ry = y - child.getInnerY();
			if (rx >= 0 && ry >= 0 && rx < child.getInnerWidth() && ry < child.getInnerHeight())
				return child;
		}
		
		return null;
	}
	
	@Override
	public boolean isEmpty() {
		return children.isEmpty();
	}

	@Override
	public List<GSPanel> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	@Override
	protected void renderForeground(GSIRenderer2D renderer) {
		super.renderForeground(renderer);

		renderChildren(renderer);
	}
	
	protected void renderChildren(GSIRenderer2D renderer) {
		for (GSPanel child : children)
			child.render(renderer);
	}
	
	@Override
	public GSILayoutManager getLayoutManager() {
		return layoutManager;
	}

	@Override
	public void setLayoutManager(GSILayoutManager layoutManager) {
		if (layoutManager != this.layoutManager) {
			this.layoutManager = layoutManager;
		
			invalidate();
		}
	}

	/* Visible for GSLayoutProperties */
	@Override
	int getDefaultMinimumWidth() {
		if (cachedMinimumInnerSize == null && layoutManager != null)
			cachedMinimumInnerSize = layoutManager.getMinimumInnerSize(this);
		return super.getDefaultMinimumWidth();
	}

	/* Visible for GSLayoutProperties */
	@Override
	int getDefaultMinimumHeight() {
		if (cachedMinimumInnerSize == null && layoutManager != null)
			cachedMinimumInnerSize = layoutManager.getMinimumInnerSize(this);
		return super.getDefaultMinimumHeight();
	}

	/* Visible for GSLayoutProperties */
	@Override
	int getDefaultPreferredWidth() {
		if (cachedPreferredInnerSize == null && layoutManager != null)
			cachedPreferredInnerSize = layoutManager.getPreferredInnerSize(this);
		return super.getDefaultPreferredWidth();
	}
	
	/* Visible for GSLayoutProperties */
	@Override
	int getDefaultPreferredHeight() {
		if (cachedPreferredInnerSize == null && layoutManager != null)
			cachedPreferredInnerSize = layoutManager.getPreferredInnerSize(this);
		return super.getDefaultPreferredHeight();
	}
}
