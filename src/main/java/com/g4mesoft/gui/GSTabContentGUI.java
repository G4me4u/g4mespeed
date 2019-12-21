package com.g4mesoft.gui;

import net.minecraft.text.Text;

public abstract class GSTabContentGUI extends GSParentGUI {

	protected GSTabbedGUI tabOwner;
	
	protected GSTabContentGUI(Text title) {
		super(title);
	
		tabOwner = null;
	}
	
	public void setTabOwner(GSTabbedGUI tabOwner) {
		this.tabOwner = tabOwner;
	}
	
	public int getScrollOffset() {
		return (tabOwner == null) ? 0 : tabOwner.getScrollOffset();
	}
	
	public abstract int getContentHeight();
	
}
