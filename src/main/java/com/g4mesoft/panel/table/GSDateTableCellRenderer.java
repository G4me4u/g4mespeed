package com.g4mesoft.panel.table;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.util.Date;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.ibm.icu.util.Calendar;

public final class GSDateTableCellRenderer implements GSITableCellRenderer<Date> {

	private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);
	private static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT);
	
	private static final String TODAY_TRANSLATION_KEY = "panel.date.todayAt";
	private static final String YESTERDAY_TRANSLATION_KEY = "panel.date.yesterdayAt";
	/* Cache for today and yesterday translations... */
	private static String todayTranslation = null;
	private static String yesterdayTranslation = null;
	
	public static final GSDateTableCellRenderer INSTANCE = new GSDateTableCellRenderer();
	
	private GSDateTableCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, Date value, GSRectangle bounds, GSTablePanel table) {
		GSStringTableCellRenderer.INSTANCE.render(renderer, formatDate(value), bounds, table);
	}
	
	@Override
	public GSDimension getMinimumSize(Date value) {
		return GSStringTableCellRenderer.INSTANCE.getMinimumSize(formatDate(value));
	}
	
	private static String formatDate(Date value) {
		StringBuffer buffer = new StringBuffer();
		// Format date. Check if the date is today or yesterday,
		// and replace with corresponding replacement strings.
		String dateReplacement = null;
		Calendar calendar = Calendar.getInstance();
		if (calendar.fieldDifference(value, Calendar.YEAR) == 0) {
			switch (calendar.fieldDifference(value, Calendar.DAY_OF_YEAR)) {
			case 0:
				if (todayTranslation == null)
					todayTranslation = GSPanelContext.i18nTranslate(TODAY_TRANSLATION_KEY);
				dateReplacement = todayTranslation;
				break;
			case -1:
				if (yesterdayTranslation == null)
					yesterdayTranslation = GSPanelContext.i18nTranslate(YESTERDAY_TRANSLATION_KEY);
				dateReplacement = yesterdayTranslation;
				break;
			}
		}
		if (dateReplacement != null) {
			// Replace the date in the formatted string
			buffer.append(dateReplacement);
		} else {
			DATE_FORMAT.format(value, buffer, GSDontCareFieldPosition.INSTANCE);
		}
		// Format time.
		buffer.append(' ');
		TIME_FORMAT.format(value, buffer, GSDontCareFieldPosition.INSTANCE);
		
		return buffer.toString();
	}
	
	private static class GSDontCareFieldPosition extends FieldPosition {

		public static final GSDontCareFieldPosition INSTANCE = new GSDontCareFieldPosition();
		
		private GSDontCareFieldPosition() {
			super(0);
		}

		@Override
		public void setBeginIndex(int bi) {
			// Do nothing
		}

		@Override
		public void setEndIndex(int ei) {
			// Do nothing
		}
	}
}
