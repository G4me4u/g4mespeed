package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSScrollPanel extends GSParentPanel {

	private final GSScrollContentPanel contentPanel;
	private final GSScrollBar scrollBar;

	public GSScrollPanel() {
		this(null);
	}
	
	public GSScrollPanel(GSPanel content) {
		contentPanel = new GSScrollContentPanel();
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
		contentPanel.setBounds(0, 0, Math.max(0, width - scrollBar.getWidth()), height);
		
		// Re-validate the scroll offset.
		scrollBar.setScrollOffset(scrollBar.getScrollOffset());
	}
	
	public void setContent(GSPanel content) {
		contentPanel.setContent(content);
	}
	
	public int getScrollOffset() {
		return (int)scrollBar.getScrollOffset();
	}
	
	private class GSScrollContentPanel extends GSParentPanel implements GSIScrollable {
		
		private GSPanel content; 
		
		public void setContent(GSPanel content) {
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
			renderer.translate(x, y - getScrollOffset(getParent()));
		}
		
		@Override
		public void postRender(GSIRenderer2D renderer) {
			renderer.popClip();
			renderer.popMatrix();
		}
		
		@Override
		public int getEventOffsetY() {
			return super.getEventOffsetY() - getScrollOffset(getParent());
		}

		@Override
		public GSPanel getChildAt(int x, int y) {
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
			if (content instanceof GSIScrollable)
				return ((GSIScrollable)content).getContentWidth();
			return 0;
		}

		@Override
		public int getContentHeight() {
			if (content instanceof GSIScrollable) 
				return ((GSIScrollable)content).getContentHeight();
			return 0;
		}
		
		@Override
		public float getIncrementalScrollX(int sign) {
			if (content instanceof GSIScrollable)
				return ((GSIScrollable)content).getIncrementalScrollX(sign);
			return GSIScrollable.super.getIncrementalScrollX(sign);
		}

		@Override
		public float getIncrementalScrollY(int sign) {
			if (content instanceof GSIScrollable)
				return ((GSIScrollable)content).getIncrementalScrollY(sign);
			return GSIScrollable.super.getIncrementalScrollY(sign);
		}
	}
}
