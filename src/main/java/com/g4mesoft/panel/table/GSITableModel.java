package com.g4mesoft.panel.table;

public interface GSITableModel {

	public void install(GSTablePanel table);

	public void uninstall(GSTablePanel table);

	public void addListener(GSITableModelListener listener);

	public void removeListener(GSITableModelListener listener);
	
	public GSITableColumn getColumn(int columnIndex);

	public GSITableRow getRow(int rowIndex);

	public Object getCellValue(int columnIndex, int rowIndex);

	public void setCellValue(int columnIndex, int rowIndex, Object value);

	public boolean isColumnHeaderHidden();

	public void setColumnHeaderHidden(boolean hidden);

	public boolean isRowHeaderHidden();
	
	public void setRowHeaderHidden(boolean hidden);
	
	public int getColumnCount();

	public int getRowCount();

}
