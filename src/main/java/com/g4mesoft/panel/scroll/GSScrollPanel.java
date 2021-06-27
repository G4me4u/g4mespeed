package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSScrollPanel extends GSParentPanel {

	private final GSScrollContentPanel contentPanel;
	private final GSScrollBar scrollBar;

	public GSScrollPanel() {
		this(null);
		
		setFocusable(false);
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
	public void layout() {
		int sw = scrollBar.getPreferredSize().getWidth();
		
		scrollBar.setBounds(Math.max(0, width - sw), 0, sw, height);
		contentPanel.setBounds(0, 0, scrollBar.getX(), height);
		
		// Re-validate the scroll offset.
		scrollBar.setScrollOffset(scrollBar.getScrollOffset());
	}
	
	public void setContent(GSPanel content) {
		contentPanel.setContent(content);
	}
	
	public int getScrollOffset() {
		return (int)scrollBar.getScrollOffset();
	}
	
	@Override
	protected GSDimension calculatePreferredSize() {
		GSDimension cps = contentPanel.getPreferredSize();
		GSDimension sps = scrollBar.getPreferredSize();
		
		int w = cps.getWidth() + sps.getWidth();
	
		// Handle overflow
		if (w < 0)
			w = Integer.MAX_VALUE;
	
		return new GSDimension(w, cps.getHeight());
	}
	
	class GSScrollContentPanel extends GSParentPanel implements GSIScrollable {
		
		private GSPanel content; 
		
		private GSScrollContentPanel() {
			setFocusable(false);
		}
		
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
		public void layout() {
			if (content != null)
				content.setBounds(0, 0, width, height);
		}
		
		@Override
		public void preRender(GSIRenderer2D renderer) {
			renderer.pushClip(x, y, width, height);
			super.preRender(renderer);
		}
		
		@Override
		public void postRender(GSIRenderer2D renderer) {
			super.postRender(renderer);
			renderer.popClip();
		}
		
		@Override
		public int getViewOffsetY() {
			return super.getViewOffsetY() - getScrollOffset(getParent());
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
		protected GSDimension calculatePreferredSize() {
			return content.getPreferredSize();
		}

		@Override
		protected GSDimension calculateMinimumSize() {
			return content.getMinimumSize();
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
