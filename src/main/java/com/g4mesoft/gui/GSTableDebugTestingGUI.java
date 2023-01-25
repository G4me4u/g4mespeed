package com.g4mesoft.gui;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSEAnchor;
import com.g4mesoft.panel.GSEFill;
import com.g4mesoft.panel.GSGridLayoutManager;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.button.GSButton;
import com.g4mesoft.panel.scroll.GSScrollPanel;
import com.g4mesoft.panel.scroll.GSViewport;
import com.g4mesoft.panel.table.GSITableModel;
import com.g4mesoft.panel.table.GSTablePanel;

import net.minecraft.text.LiteralText;

public class GSTableDebugTestingGUI extends GSParentPanel {

	private static final int COLUMN_COUNT = 10;
	private static final int ROW_COUNT = 20;
	
	private GSPanel previousPanel;
	
	public GSTableDebugTestingGUI() {
		setLayoutManager(new GSGridLayoutManager());
		
		GSButton button = new GSButton("Reset Table");
		button.getLayout()
			.set(GSGridLayoutManager.GRID_X, 0)
			.set(GSGridLayoutManager.GRID_Y, 1)
			.set(GSGridLayoutManager.ANCHOR, GSEAnchor.CENTER)
			.set(GSGridLayoutManager.FILL, GSEFill.NONE);
		button.addActionListener(this::reset);
		add(button);
		
		reset();
	}
	
	private void reset() {
		if (previousPanel != null)
			remove(previousPanel);
		
		GSTablePanel table = new GSTablePanel(COLUMN_COUNT, ROW_COUNT);

		GSITableModel model = table.getModel();
		for (int c = 0; c < model.getColumnCount(); c++)
			model.getColumn(c).setHeaderValue("Column " + (c + 1));
		for (int r = 0; r < model.getRowCount(); r++)
			model.getRow(r).setHeaderValue("Row " + (r + 1));
		table.setPreferredRowCount(15);
		table.setPreferredColumnCount(4);
		
		for (int c = 0; c < model.getColumnCount(); c++) {
			for (int r = 0; r < model.getRowCount(); r++)
				model.setCellValue(c, r, new LiteralText(String.format("(%d, %d)", r + 1, c + 1)));
		}

		boolean withScroll = true;
		if (withScroll) {
			previousPanel = new GSScrollPanel(table);
		} else {
			previousPanel = table;
		}

		previousPanel.getLayout()
			.set(GSGridLayoutManager.GRID_X, 0)
			.set(GSGridLayoutManager.GRID_Y, 0)
			.set(GSGridLayoutManager.ANCHOR, GSEAnchor.CENTER)
			.set(GSGridLayoutManager.FILL, GSEFill.NONE);
		add(previousPanel);
		
		boolean printPreferredSize = false;
		if (printPreferredSize) {
			GSPanel parent = table.getParent();
			if (parent instanceof GSViewport) {
				GSDimension vPrefSize = ((GSViewport)parent).getProperty(GSPanel.PREFERRED_SIZE);
				System.out.printf("w: %d, h: %d\n", vPrefSize.getWidth(), vPrefSize.getHeight());
			}
		}
	}
}
