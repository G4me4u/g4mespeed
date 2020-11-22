package com.g4mesoft.gui.scroll;

import com.g4mesoft.gui.GSIElement;
import com.g4mesoft.gui.GSParentPanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSScrollablePanel extends GSParentPanel {

	private final GSScrollableContentPanel contentPanel;
	private final GSScrollBar scrollBar;

	public GSScrollablePanel() {
		this(null);
	}
	
	public GSScrollablePanel(GSIScrollableElement content) {
		contentPanel = new GSScrollableContentPanel();
		scrollBar = new GSScrollBar(contentPanel, null);
	
		add(contentPanel);
		add(scrollBar);

		if (content != null)
			contentPanel.setContent(content);
	}
	
	@Override
	public void onBoundsChanged() {
		super.onBoundsChanged();
		
		scrollBar.initVerticalRight(width, 0, height);
		contentPanel.setBounds(0, 0, width - scrollBar.getWidth(), height);
		
		// Re-validate the scroll offset.
		scrollBar.setScrollOffset(scrollBar.getScrollOffset());
	}
	
	public void setContent(GSIScrollableElement content) {
		contentPanel.setContent(content);
	}
	
	public int getScrollOffset() {
		return (int)scrollBar.getScrollOffset();
	}
	
	private class GSScrollableContentPanel extends GSParentPanel implements GSIScrollableViewport {
		
		private GSIScrollableElement content; 
		
		public void setContent(GSIScrollableElement content) {
			if (this.content != null)
				remove(this.content);
			
			this.content = content;
			
			if (content != null) {
				content.setBounds(0, 0, width, height);
				add(content);
			}
		}
		
		@Override
		protected void onBoundsChanged() {
			super.onBoundsChanged();

			if (content != null)
				content.setBounds(0, 0, width, height);
		}
		
		@Override
		public void preRender(GSIRenderer2D renderer) {
			renderer.pushMatrix();
			renderer.pushClip(x, y, width, height);
			renderer.translate(x, y - getScrollOffset());
		}
		
		@Override
		public void postRender(GSIRenderer2D renderer) {
			renderer.popClip();
			renderer.popMatrix();
		}
		
		@Override
		public int getEventOffsetY() {
			return super.getEventOffsetY() - getScrollOffset();
		}

		@Override
		public GSIElement getChildAt(int x, int y) {
			if (content != null && x >= 0 && x < width && y >= 0) {
				// Since the content itself is located at (0, 0) with
				// dimensions (width, height), we have to do a manual
				// check if the y-coordinate is within the content.
				if (y < Math.max(getContentHeight(), height))
					return content;
			}
			
			return null;
		}
		
		@Override
		public int getContentWidth() {
			return (content != null) ? content.getContentWidth() : 0;
		}

		@Override
		public int getContentHeight() {
			return (content != null) ? content.getContentHeight() : 0;
		}
		
		@Override
		public float getIncrementalScrollX(int sign) {
			if (content != null)
				return content.getIncrementalScrollX(sign);
			
			return GSIScrollableViewport.super.getIncrementalScrollX(sign);
		}

		@Override
		public float getIncrementalScrollY(int sign) {
			if (content != null)
				return content.getIncrementalScrollY(sign);

			return GSIScrollableViewport.super.getIncrementalScrollY(sign);
		}
	}
}
