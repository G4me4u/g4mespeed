package com.g4mesoft.gui;

import java.util.LinkedList;

import com.g4mesoft.ui.panel.GSClosableParentPanel;
import com.g4mesoft.ui.panel.GSPanel;
import com.g4mesoft.ui.panel.event.GSCompoundButtonStroke;
import com.g4mesoft.ui.panel.event.GSEvent;
import com.g4mesoft.ui.panel.event.GSIButtonStroke;
import com.g4mesoft.ui.panel.event.GSKeyButtonStroke;
import com.g4mesoft.ui.panel.event.GSKeyEvent;
import com.g4mesoft.ui.panel.event.GSMouseButtonStroke;
import com.g4mesoft.ui.panel.event.GSMouseEvent;

public class GSContentHistoryGUI extends GSClosableParentPanel {

	private final LinkedList<GSPanel> contentHistory;
	
	private GSPanel content;
	
	public GSContentHistoryGUI(GSPanel primaryContent, GSIButtonStroke backButton) {
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
	
	public void removeHistory(GSPanel content) {
		if (content == this.content) {
			back();
		} else if (!contentHistory.isEmpty() && content != contentHistory.getFirst()) {
			contentHistory.removeLastOccurrence(content);
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
		invalidate();
		
		content.requestFocus();
	}
}
