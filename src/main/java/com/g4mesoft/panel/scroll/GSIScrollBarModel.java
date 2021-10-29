package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSIChangeListener;

public interface GSIScrollBarModel {

	public void addChangeListener(GSIChangeListener listener);

	public void removeChangeListener(GSIChangeListener listener);

	public void addScrollListener(GSIScrollListener listener);
	
	public void removeScrollListener(GSIScrollListener listener);
	
	public float getScroll();

	public void setScroll(float scroll);

	public float getMinScroll();

	public void setMinScroll(float minScroll);

	public float getMaxScroll();

	public void setMaxScroll(float maxScroll);
	
	public void setScrollInterval(float minScroll, float maxScroll);
	
	public float getBlockScroll();
	
	public void setBlockScroll(float blockScroll);
	
}
