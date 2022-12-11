package com.g4mesoft.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.g4mesoft.renderer.GSIRenderer2D;

public class GSParentPanel extends GSPanel {

	protected final List<GSPanel> children;
	private boolean iteratingChildren;
	
	private GSILayoutManager layoutManager;

	public GSParentPanel() {
		this(null);
	}
	
	public GSParentPanel(GSILayoutManager layoutManager) {
		children = new ArrayList<>();
		
		if (layoutManager != null)
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
			iteratingChildren = true;
			try {
				for (GSPanel panel : children)
					panel.setVisible(visible);
			} finally {
				iteratingChildren = false;
			}

			super.setVisible(visible);
		}
	}
	
	@Override
	public void add(GSPanel panel) {
		if (panel == null)
			throw new IllegalArgumentException("panel is null");
		if (iteratingChildren)
			throw new IllegalStateException("Children can not be added  when iterating");
		
		children.add(panel);

		panel.onAdded(this);
		panel.setVisible(isVisible());

		if (!isValidating())
			invalidate();
		panel.invalidate();
	}
	
	@Override
	public void remove(GSPanel panel) {
		if (iteratingChildren)
			throw new IllegalStateException("Children can not be removed when iterating");

		if (children.remove(panel)) {
			onChildRemoved(panel);
			if (!isValidating())
				invalidate();
		}
	}

	@Override
	public void remove(int index) {
		if (iteratingChildren)
			throw new IllegalStateException("Children can not be removed when iterating");
		
		GSPanel panel = children.remove(index);
		if (panel != null) {
			onChildRemoved(panel);
			if (!isValidating())
				invalidate();
		}
	}
	
	@Override
	public void removeAll() {
		if (iteratingChildren)
			throw new IllegalStateException("Children can not be removed when iterating");
		
		if (!children.isEmpty()) {
			do {
				// NOTE: Remove last to ensure we do not
				// have an O(n^2) removeAll algorithm.
				// For reference, view ArrayList#remove.
				int lastIndex = children.size() - 1;
				onChildRemoved(children.remove(lastIndex));
			} while (!children.isEmpty());
		
			if (!isValidating())
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
		iteratingChildren = true;
		try {
			// Traverse children in reverse direction to match
			// the order that they are drawn (last on top).
			for (int i = children.size() - 1; i >= 0; i--) {
				GSPanel child = children.get(i);
				if (child.isInBounds(x, y))
					return child;
			}
		} finally {
			iteratingChildren = false;
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
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		renderChildren(renderer);
	}
	
	protected void renderChildren(GSIRenderer2D renderer) {
		GSRectangle clipBounds = renderer.getClipBounds();
		
		iteratingChildren = true;
		try {
			for (GSPanel child : children) {
				// Ensure that the child is actually visible.
				if (clipBounds.intersects(child.getBounds())) {
					child.preRender(renderer);
					child.render(renderer);
					child.postRender(renderer);
				}
			}
		} finally {
			iteratingChildren = false;
		}
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

	@Override
	void invalidateNow() {
		super.invalidateNow();
		// Invalidate the entire sub-tree
		iteratingChildren = true;
		try {
			for (GSPanel child : children)
				child.invalidate();
		} finally {
			iteratingChildren = false;
		}
	}
	
	@Override
	protected void validate() {
		super.validate();
		// Validate the entire sub-tree
		iteratingChildren = true;
		try {
			for (GSPanel child : children)
				child.revalidate();
		} finally {
			iteratingChildren = false;
		}
	}
	
	/* Visible for GSLayoutProperties */
	@Override
	int getDefaultMinimumWidth() {
		if (cachedMinimumSize == null && layoutManager != null)
			cachedMinimumSize = layoutManager.getMinimumSize(this);
		return super.getDefaultMinimumWidth();
	}

	/* Visible for GSLayoutProperties */
	@Override
	int getDefaultMinimumHeight() {
		if (cachedMinimumSize == null && layoutManager != null)
			cachedMinimumSize = layoutManager.getMinimumSize(this);
		return super.getDefaultMinimumHeight();
	}

	/* Visible for GSLayoutProperties */
	@Override
	int getDefaultPreferredWidth() {
		if (cachedPreferredSize == null && layoutManager != null)
			cachedPreferredSize = layoutManager.getPreferredSize(this);
		return super.getDefaultPreferredWidth();
	}
	
	/* Visible for GSLayoutProperties */
	@Override
	int getDefaultPreferredHeight() {
		if (cachedPreferredSize == null && layoutManager != null)
			cachedPreferredSize = layoutManager.getPreferredSize(this);
		return super.getDefaultPreferredHeight();
	}
}
