package com.g4mesoft.gui;

import java.util.LinkedList;

import com.g4mesoft.panel.GSClosableParentPanel;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.event.GSCompoundButtonStroke;
import com.g4mesoft.panel.event.GSEvent;
import com.g4mesoft.panel.event.GSIButtonStroke;
import com.g4mesoft.panel.event.GSKeyButtonStroke;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.panel.event.GSMouseButtonStroke;
import com.g4mesoft.panel.event.GSMouseEvent;

public class GSMainGUI extends GSClosableParentPanel {

	private final LinkedList<GSPanel> contentHistory;
	
	private GSPanel content;
	
	public GSMainGUI(GSPanel primaryContent, GSIButtonStroke backButton) {
		contentHistory = new LinkedList<>();
		
		setContentNoHistory(primaryContent);

		// Add extra UI specific ways to traverse back
		backButton = new GSCompoundButtonStroke(backButton, new GSMouseButtonStroke(GSMouseEvent.BUTTON_4));
		backButton = new GSCompoundButtonStroke(backButton, new GSKeyButtonStroke(GSKeyEvent.KEY_LEFT, 
				GSEvent.MODIFIER_CONTROL | GSEvent.MODIFIER_ALT));
		
		putButtonStroke(backButton, this::back);
	}

	@Override
	protected void layout() {
		super.layout();

		content.setBounds(0, 0, width, height);
	}

	public void clearHistory() {
		if (!contentHistory.isEmpty()) {
			setContentNoHistory(contentHistory.getFirst());
			contentHistory.clear();
		}
	}
	
	public void back() {
		if (!contentHistory.isEmpty()) {
			setContentNoHistory(contentHistory.removeLast());
		} else {
			close();
		}
	}
	
	public GSPanel getContent() {
		return content;
	}

	public void setContent(GSPanel content) {
		contentHistory.addLast(this.content);
		setContentNoHistory(content);
	}
	
	private void setContentNoHistory(GSPanel content) {
		this.content = content;

		removeAll();
		add(content);
		requestLayout();
		
		content.requestFocus();
	}
}
