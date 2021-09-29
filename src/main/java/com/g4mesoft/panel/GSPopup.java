package com.g4mesoft.panel;

import com.g4mesoft.renderer.GSClipRect;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.client.render.VertexFormats;

public class GSPopup extends GSParentPanel {

	private static final int SHADOW_WIDTH    = 5;
	private static final int SHADOW_OFFSET_X = 1;
	private static final int SHADOW_OFFSET_Y = 2;
	private static final int SHADOW_COLOR = 0x80000000;
	
	protected final GSPanel content;
	protected GSPanel source;
	
	public GSPopup(GSPanel content) {
		if (content == null)
			throw new IllegalArgumentException("content is null");
		this.content = content;
		source = null;
	}
	
	@Override
	public void onAdded(GSPanel parent) {
		super.onAdded(parent);

		if (source != null)
			source.incrementPopupCount();
	}

	@Override
	public void onRemoved(GSPanel parent) {
		super.onRemoved(parent);
		
		if (source != null)
			source.decrementPopupCount();
	}
	
	@Override
	public void add(GSPanel panel) {
		throw new UnsupportedOperationException("Popups can only have one child");
	}

	@Override
	public void remove(GSPanel panel) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void layout() {
		content.setOuterBounds(0, 0, innerWidth, innerHeight);
	}
	
	@Override
	public GSDimension calculateMinimumInnerSize() {
		return content.getProperty(MINIMUM_SIZE);
	}

	@Override
	protected GSDimension calculatePreferredInnerSize() {
		return content.getProperty(PREFERRED_SIZE);
	}

	public void show(GSPanel source, GSLocation location) {
		show(source, location, false);
	}

	public void show(GSPanel source, GSLocation location, boolean relative) {
		show(source, location.getX(), location.getY(), relative);
	}

	public void show(GSPanel source, int x, int y) {
		show(source, x, y, false);
	}

	public void show(GSPanel source, int x, int y, boolean relative) {
		if (getParent() != null)
			return;
		
		this.source = source;
		
		if (relative && source != null) {
			GSLocation viewLocation = GSPanelUtil.getViewLocation(source);
			
			x += viewLocation.getX();
			y += viewLocation.getY();
		}
		
		GSDimension pref = getProperty(PREFERRED_SIZE);
		setOuterBounds(adjustLocation(x, y, pref), pref);
		
		super.add(content);
	
		GSRootPanel rootPanel = GSPanelContext.getRootPanel();
		rootPanel.add(this, GSRootPanel.POPUP_LAYER);
		content.requestFocus();
	}
	
	private GSLocation adjustLocation(int x, int y, GSDimension size) {
		GSRootPanel rootPanel = GSPanelContext.getRootPanel();
		if (x + size.getWidth() >= rootPanel.getInnerWidth()) {
			// Force pop-up to be left of right
			x = rootPanel.getInnerWidth() - size.getWidth();
		}
		if (y + size.getHeight() >= rootPanel.getInnerHeight()) {
			// Force pop-up to be above bottom
			y = rootPanel.getInnerHeight() - size.getHeight();
		}
		return new GSLocation(x, y);
	}
	
	public void hide() {
		GSPanel parent = getParent();
		if (parent != null)
			parent.remove(this);
		
		super.remove(content);

		if (source != null) {
			source.requestFocus();
			source = null;
		}
	}
	
	@Override
	protected void renderBorder(GSIRenderer2D renderer) {
		super.renderBorder(renderer);

		// Temporarily remove the border clip
		GSClipRect oldClipRect = renderer.popClip();
		
		renderer.pushMatrix();
		// Translate to top-left of shadow
		renderer.translate(SHADOW_OFFSET_X - SHADOW_WIDTH, SHADOW_OFFSET_Y - SHADOW_WIDTH);
		renderer.build(GSIRenderer2D.QUADS, VertexFormats.POSITION_COLOR);

		int w  = outerWidth  - SHADOW_OFFSET_X;
		int h  = outerHeight - SHADOW_OFFSET_Y;
		int bx = outerWidth  + SHADOW_WIDTH - SHADOW_OFFSET_X;
		int by = outerHeight + SHADOW_WIDTH - SHADOW_OFFSET_Y;
		
		// Left, right, top, and bottom shadows
		renderer.fillHGradient( 0, SHADOW_WIDTH, SHADOW_WIDTH, h, 0, SHADOW_COLOR);
		renderer.fillHGradient(bx, SHADOW_WIDTH, SHADOW_WIDTH, h, SHADOW_COLOR, 0);
		renderer.fillVGradient(SHADOW_WIDTH,  0, w, SHADOW_WIDTH, 0, SHADOW_COLOR);
		renderer.fillVGradient(SHADOW_WIDTH, by, w, SHADOW_WIDTH, SHADOW_COLOR, 0);
		
		// Top-left, top-right, bottom-left, bottom-right shadows
		renderer.fillGradient( 0,  0, SHADOW_WIDTH, SHADOW_WIDTH, 0, 0, 0, SHADOW_COLOR, false);
		renderer.fillGradient(bx,  0, SHADOW_WIDTH, SHADOW_WIDTH, 0, 0, SHADOW_COLOR, 0, true);
		renderer.fillGradient( 0, by, SHADOW_WIDTH, SHADOW_WIDTH, 0, SHADOW_COLOR, 0, 0, true);
		renderer.fillGradient(bx, by, SHADOW_WIDTH, SHADOW_WIDTH, SHADOW_COLOR, 0, 0, 0, false);

		renderer.finish();
		renderer.popMatrix();
	
		// Re-push the border clip rectangle.
		renderer.pushClip(oldClipRect);
	}
	
	@Override
	protected void renderForeground(GSIRenderer2D renderer) {
		// Vanilla text rendering will enable depth in order to render
		// shadows. This causes issues with text rendered on top of
		// other text. Fix these issues with a small z-translation.
		renderer.pushMatrix();
		renderer.translateDepth(0.1f);
		super.renderForeground(renderer);
		renderer.popMatrix();
	}
}
