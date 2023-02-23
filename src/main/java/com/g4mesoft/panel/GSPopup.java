package com.g4mesoft.panel;

import com.g4mesoft.panel.event.GSILayoutEventListener;
import com.g4mesoft.panel.event.GSLayoutEvent;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.client.render.VertexFormats;

public class GSPopup extends GSParentPanel {

	private static final int SHADOW_WIDTH    = 5;
	private static final int SHADOW_OFFSET_X = 1;
	private static final int SHADOW_OFFSET_Y = 2;
	private static final int SHADOW_COLOR = 0x80000000;
	
	protected final GSPanel content;
	protected final boolean stealingFocus;
	
	private boolean hiddenOnFocusLost;
	
	protected GSPanel source;
	private GSEPopupPlacement placement;
	private int relX;
	private int relY;
	private boolean sourceFocusedOnHide;
	
	private GSILayoutEventListener sourceLayoutListener;

	public GSPopup(GSPanel content) {
		this(content, true);
	}
	
	public GSPopup(GSPanel content, boolean stealingFocus) {
		if (content == null)
			throw new IllegalArgumentException("content is null");
		this.content = content;
		this.stealingFocus =  stealingFocus;
		
		hiddenOnFocusLost = false;

		source = null;
		placement = GSEPopupPlacement.ABSOLUTE;
		relX = relY = 0;
		sourceFocusedOnHide = true;
		
		sourceLayoutListener = null;
	}
	
	@Override
	protected void onShown() {
		super.onShown();
		
		if (placement != GSEPopupPlacement.ABSOLUTE && source != null) {
			sourceLayoutListener = new GSSourceLayoutListener();
			source.addLayoutEventListener(sourceLayoutListener);
		}

	}

	@Override
	protected void onHidden() {
		super.onHidden();
		
		if (placement != GSEPopupPlacement.ABSOLUTE && source != null) {
			source.removeLayoutEventListener(sourceLayoutListener);
			sourceLayoutListener = null;
		}

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
		content.setBounds(0, 0, width, height);
	}
	
	@Override
	public GSDimension calculateMinimumSize() {
		return content.getProperty(MINIMUM_SIZE);
	}

	@Override
	protected GSDimension calculatePreferredSize() {
		return content.getProperty(PREFERRED_SIZE);
	}

	public void show(GSPanel source, GSLocation location) {
		show(source, location, GSEPopupPlacement.ABSOLUTE);
	}

	public void show(GSPanel source, GSLocation location, GSEPopupPlacement placement) {
		show(source, location.getX(), location.getY(), placement);
	}

	public void show(GSPanel source, int x, int y) {
		show(source, x, y, GSEPopupPlacement.ABSOLUTE);
	}

	public void show(GSPanel source, int x, int y, GSEPopupPlacement placement) {
		if (placement == null)
			throw new IllegalArgumentException("placement is null");
		if (getParent() != null)
			return;

		// Update source (root panel if not specified).
		GSRootPanel rootPanel = GSPanelContext.getRootPanel();
		this.source = source != null ? source : rootPanel;
		
		this.placement = placement;
		this.relX = x;
		this.relY = y;
		
		updateBounds(x, y);
		
		super.add(content);
	
		rootPanel.add(this, GSRootPanel.POPUP_LAYER);
		GSPanelContext.getEventDispatcher().pushTopPopup(this);
		
		if (isStealingFocus())
			content.requestFocus();
	}
	
	private void updateBounds(int x, int y) {
		GSDimension ps = getProperty(PREFERRED_SIZE);

		if (source != null) {
			GSLocation viewLocation = GSPanelUtil.getViewLocation(source);
			
			int dw = source.getWidth() - ps.getWidth();
			switch (placement) {
			case RELATIVE:
				x += viewLocation.getX();
				break;
			case NORTHWEST:
			case WEST:
			case SOUTHWEST:
				x = viewLocation.getX();
			case NORTH:
			case CENTER:
			case SOUTH:
				x = viewLocation.getX() + dw / 2;
				break;
			case NORTHEAST:
			case EAST:
			case SOUTHEAST:
				x = viewLocation.getX() + dw;
			case ABSOLUTE:
			default:
				break;
			}

			int dh = source.getHeight() - ps.getHeight();
			switch (placement) {
			case RELATIVE:
				y += viewLocation.getY();
				break;
			case NORTHWEST:
			case NORTH:
			case NORTHEAST:
				y = viewLocation.getY();
			case WEST:
			case CENTER:
			case EAST:
				y = viewLocation.getY() + dh / 2;
				break;
			case SOUTHWEST:
			case SOUTH:
			case SOUTHEAST:
				y = viewLocation.getY() + dh;
			case ABSOLUTE:
			default:
				break;
			}
		}
		
		setBounds(adjustLocation(x, y, ps), ps);
	}
	
	private GSLocation adjustLocation(int x, int y, GSDimension size) {
		GSRootPanel rootPanel = GSPanelContext.getRootPanel();
		if (x + size.getWidth() >= rootPanel.getWidth()) {
			// Force pop-up to be left of right
			x = rootPanel.getWidth() - size.getWidth();
		}
		if (y + size.getHeight() >= rootPanel.getHeight()) {
			// Force pop-up to be above bottom
			y = rootPanel.getHeight() - size.getHeight();
		}
		return new GSLocation(x, y);
	}
	
	public void hide() {
		GSPanelContext.getEventDispatcher().popTopPopup(this);
		
		// Transfer focus before losing it due to being removed from parent.
		if (this.source != null) {
			GSPanel source = this.source;
			this.source = null;
			if (sourceFocusedOnHide && source.isAdded())
				source.requestFocus();
		}

		super.remove(content);

		GSPanel parent = getParent();
		if (parent != null)
			parent.remove(this);
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		renderShadow(renderer);
	
		// Fix issues with text rendering (depth enabled)
		renderer.pushMatrix();
		renderer.translateDepth(0.1f);
		super.render(renderer);
		renderer.popMatrix();
	}
	
	protected void renderShadow(GSIRenderer2D renderer) {
		renderer.pushMatrix();
		// Translate to top-left of shadow
		renderer.translate(SHADOW_OFFSET_X - SHADOW_WIDTH,
		                   SHADOW_OFFSET_Y - SHADOW_WIDTH);
		renderer.build(GSIRenderer2D.QUADS, VertexFormats.POSITION_COLOR);

		int w  = width  - SHADOW_OFFSET_X;
		int h  = height - SHADOW_OFFSET_Y;
		int bx = width  + SHADOW_WIDTH - SHADOW_OFFSET_X;
		int by = height + SHADOW_WIDTH - SHADOW_OFFSET_Y;
		
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
	}
	
	public boolean isSourceFocusedOnHide() {
		return sourceFocusedOnHide;
	}
	
	public void setSourceFocusedOnHide(boolean focusedOnHide) {
		this.sourceFocusedOnHide = focusedOnHide;
	}
	
	public boolean isStealingFocus() {
		return stealingFocus;
	}
	
	public boolean isHiddenOnFocusLost() {
		return hiddenOnFocusLost;
	}
	
	public void setHiddenOnFocusLost(boolean flag) {
		if (flag != hiddenOnFocusLost) {
			hiddenOnFocusLost = flag;
			
			if (flag && isAdded() && !GSPanelUtil.isFocusWithin(this))
				hide();
		}
	}

	private class GSSourceLayoutListener implements GSILayoutEventListener {

		@Override
		public void panelMoved(GSLayoutEvent event) {
			// TODO(Christian): also capture events further up in the tree
			if (source != null && source.isVisible())
				updateBounds(relX, relY);
		}

		@Override
		public void panelResized(GSLayoutEvent event) {
			if (source != null && source.isVisible())
				updateBounds(relX, relY);
		}
	}
}
