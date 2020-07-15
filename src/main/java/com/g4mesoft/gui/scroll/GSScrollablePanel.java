package com.g4mesoft.gui.scroll;

import com.g4mesoft.gui.GSParentPanel;
import com.g4mesoft.gui.renderer.GSIRenderer2D;

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
			renderer.pushTransform();
			renderer.pushClip(x, y, width, height);
			renderer.translate(x, y - getScrollOffset());
		}
		
		@Override
		public void postRender(GSIRenderer2D renderer) {
			renderer.popClip();
			renderer.popTransform();
		}
		
		@Override
		public int getEventOffsetY() {
			return super.getEventOffsetY() - getScrollOffset();
		}

		@Override
		public int getContentWidth() {
			return content.getContentWidth();
		}

		@Override
		public int getContentHeight() {
			return content.getContentHeight();
		}
		
		@Override
		public float getIncrementalScrollX(int sign) {
			return content.getIncrementalScrollX(sign);
		}

		@Override
		public float getIncrementalScrollY(int sign) {
			return content.getIncrementalScrollY(sign);
		}
	}
}
