package com.g4mesoft.panel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;

import com.g4mesoft.panel.scroll.GSScrollPanel;
import com.g4mesoft.panel.scroll.GSViewport;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class GSPanelUtil {

	private static final DateTimeFormatter DATE_FORMAT      = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
	private static final DateTimeFormatter TIME_FORMAT      = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
	/* Replacement i18n strings for date format */
	private static final String TODAY_AT_KEY     = "panel.date.todayAt";
	private static final String YESTERDAY_AT_KEY = "panel.date.yesterdayAt";
	private static final String TOMORROW_AT_KEY  = "panel.date.tomorrowAt";
	private static final String TODAY_KEY        = "panel.date.today";
	private static final String YESTERDAY_KEY    = "panel.date.yesterday";
	private static final String TOMORROW_KEY     = "panel.date.tomorrow";
	/* Return values for #getRelativeDate(...) */
	private static final int DATE_OTHER     = -1;
	private static final int DATE_TODAY     =  0;
	private static final int DATE_YESTERDAY =  1;
	private static final int DATE_TOMORROW  =  2;
	
	private GSPanelUtil() {
	}
	
	public static void drawLabel(GSIRenderer2D renderer, GSIcon icon, int spacing, Text text, int textColor,
	                             boolean shadowed, GSEIconAlignment iconAlignment, GSETextAlignment textAlignment,
	                             GSRectangle bounds) {
		drawLabel(renderer, icon, spacing, text, textColor, shadowed, iconAlignment, textAlignment,
				bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}
	
	public static void drawLabel(GSIRenderer2D renderer, GSIcon icon, int spacing, Text text, int textColor,
	                             boolean shadowed, GSEIconAlignment iconAlignment, GSETextAlignment textAlignment,
	                             int x, int y, int width, int height) {
		
		// Remaining width for text
		int rw = width;
		if (icon != null) {
			rw -= icon.getWidth();
			// Handle extra margin between text and icon
			if (text != null)
				rw -= spacing;
		}
		
		// Trim text and get text width
		OrderedText trimmedText = null;
		int tw = 0;
		if (text != null) {
			trimmedText = renderer.trimString(text, rw);
			tw = (int)Math.ceil(renderer.getTextWidth(trimmedText));
		}
		
		// Handle text alignment
		int cx = x;
		switch (textAlignment) {
		case CENTER:
			cx += (rw - tw) / 2;
			break;
		case RIGHT:
			cx += rw - tw;
			break;
		case LEFT:
		default:
			break;
		}
		
		if (text != null) {
			int tx = cx;
			int ty = y + (height - renderer.getTextAscent()) / 2;
		
			// Handle left icon alignment (move text to the right)
			if (icon != null && iconAlignment != GSEIconAlignment.RIGHT)
				tx += icon.getWidth() + spacing;
			renderer.drawText(trimmedText, tx, ty, textColor, shadowed);
		}

		if (icon != null) {
			int ix = cx;
			int iy = y + (height - icon.getHeight()) / 2;
			
			// Handle right icon alignment (move icon to the right)
			if (text != null && iconAlignment == GSEIconAlignment.RIGHT)
				ix += tw + spacing;
			
			icon.render(renderer, new GSRectangle(ix, iy, icon.getSize()));
		}
	}
	
	public static GSDimension labelPreferredSize(GSIcon icon, Text text, int spacing) {
		GSIRenderer2D renderer = GSPanelContext.getRenderer();
		
		int w = 0, h = 0;
		if (text != null) {
			w = (int)Math.ceil(renderer.getTextWidth(text));
			h = renderer.getLineHeight();
			
			if (icon != null) {
				w += icon.getWidth() + spacing;
				h = Math.max(h, icon.getHeight());
			}
		} else if (icon != null) {
			w = icon.getWidth();
			h = icon.getHeight();
		}
		
		return new GSDimension(w, h);
	}
	
	/**
	 * Computes the index in the sequence that is right after, or before, the word
	 * starting at the given {@code startIndex}, depending on if {@code backward}
	 * is {@code false} or {@code true}, respectively.
	 * Following is an example:
	 * <pre>
	 * sequence:   "I like pineapples, oranges, and lemons!"
	 *                     ^   ^     ^
	 *                     3   1     2
	 * 1: beginIndex
	 * 2: result (backward = false)
	 * 3: result (backward = true)
	 * </pre>
	 * And a similar example:
	 * <pre>
	 * sequence:   "I like pineapples, oranges, and lemons!"
	 *                     ^         ^ ^
	 *                     3         1 2
	 * 1: beginIndex
	 * 2: result (backward = false)
	 * 3: result (backward = true)
	 * </pre>
	 * Generally, the result is computed in an intuitive way for navigating a text
	 * field. In particular, it skips characters until it hits one of a different
	 * category. Categories that are considered is letters/digits, and symbols.
	 * If an unspecified character is found in-between these, it is also skipped.
	 * <br><br>
	 * Note: going backwards, indices in the middle of a word results in the index
	 *       at the beginning of that word. However, the index at the beginning of a
	 *       word always results in the index at the beginning of the previous word.
	 * 
	 * @param sequence - the sequence to search for the beginning or end of a word.
	 * @param beginIndex - the index to begin the search
	 * @param backward - whether to search backward or forward
	 * 
	 * @return the starting index of the next or previous word, depending on the
	 *         given {@code backward} flag.
	 */
	public static int getIndexAfterWord(CharSequence sequence, int beginIndex, boolean backward) {
		int nextIndex = beginIndex;
		if (backward) {
			GSEWordCharacterType prevType = GSEWordCharacterType.OTHER;
			for ( ; nextIndex > 0; nextIndex--) {
				GSEWordCharacterType type = getWordCharacterTypeAt(sequence, nextIndex - 1);
				if (type != prevType && prevType != GSEWordCharacterType.OTHER)
					break;
				prevType = type;
			}
		} else {
			GSEWordCharacterType prevType = getWordCharacterTypeAt(sequence, beginIndex);
			for ( ; nextIndex < sequence.length(); nextIndex++) {
				GSEWordCharacterType type = getWordCharacterTypeAt(sequence, nextIndex);
				if (type != prevType && type != GSEWordCharacterType.OTHER)
					break;
				prevType = type;
			}
		}
		return nextIndex;
	}
	
	/* Checks the type letter/digit, symbol, and other, of the char at the specified index. */
	private static GSEWordCharacterType getWordCharacterTypeAt(CharSequence sequence, int index) {
		if (index >= 0 && index < sequence.length()) {
			switch (Character.getType(sequence.charAt(index))) {
			// Letters and digits
			case Character.UPPERCASE_LETTER:
			case Character.LOWERCASE_LETTER:
			case Character.TITLECASE_LETTER:
			case Character.MODIFIER_LETTER:
			case Character.OTHER_LETTER:
			case Character.DECIMAL_DIGIT_NUMBER:
			case Character.OTHER_NUMBER:
				return GSEWordCharacterType.LETTER_OR_DIGIT;
			// Symbols
			case Character.LETTER_NUMBER:
			case Character.DASH_PUNCTUATION:
			case Character.START_PUNCTUATION:
			case Character.END_PUNCTUATION:
			case Character.CONNECTOR_PUNCTUATION:
			case Character.OTHER_PUNCTUATION:
			case Character.MATH_SYMBOL:
			case Character.CURRENCY_SYMBOL:
			case Character.MODIFIER_SYMBOL:
			case Character.OTHER_SYMBOL:
			case Character.INITIAL_QUOTE_PUNCTUATION:
			case Character.FINAL_QUOTE_PUNCTUATION:
				return GSEWordCharacterType.SYMBOL;
			// Unspecified
			case Character.UNASSIGNED:
			case Character.NON_SPACING_MARK:
			case Character.ENCLOSING_MARK:
			case Character.COMBINING_SPACING_MARK:
			case Character.SPACE_SEPARATOR:
			case Character.LINE_SEPARATOR:
			case Character.PARAGRAPH_SEPARATOR:
			case Character.CONTROL:
			case Character.FORMAT:
			case Character.PRIVATE_USE:
			case Character.SURROGATE:
			default:
				return GSEWordCharacterType.OTHER;
			}
		}
		return null;
	}
	
	/**
	 * Formats the given date-time using the system date-time format and time-zone.
	 * 
	 * @param date - the date (and time) to be formatted
	 * 
	 * @see #formatZonedDateTime(ZonedDateTime)
	 */
	public static String formatDate(Date date) {
		return formatInstant(date.toInstant());
	}
	
	/**
	 * Formats the given instant using the system date-time format and time-zone.
	 * 
	 * @param instant - the {@code instant} representing seconds and nanoseconds fraction
	 *                  from the epoch of 1970-01-01T00:00:00Z UTC.
	 * 
	 * @see #formatZonedDateTime(ZonedDateTime)
	 */
	public static String formatInstant(Instant instant) {
		return formatZonedDateTime(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()));
	}

	/**
	 * Formats the given date-time using the system date-time format and time-zone.
	 * The formatting is handled as in the {@link #formatZonedDateTime(ZonedDateTime)}
	 * method. If the given local date-time is unable to be represented in the system
	 * time-zone, then this method attempts to match it as close as possible. For more
	 * info, see {@link LocalDateTime#atZone(ZoneId)}.
	 * 
	 * @param dateTime - the date-time to be formatted
	 * 
	 * @return the formatted date-time, using mostly the system date-time format.
	 * 
	 * @see #formatZonedDateTime(ZonedDateTime)
	 */
	public static String formatLocalDateTime(LocalDateTime dateTime) {
		return formatZonedDateTime(dateTime.atZone(ZoneId.systemDefault()));
	}
	
	/**
	 * Formats the given date using the system date-time format. If the given
	 * {@code dateTime} represents a date that is today, it will be formatted as
	 * {@code "Today at <TimeFormat>"}, and similarly if the it is yesterday it is
	 * formatted as {@code "Yesterday at <TimeFormat>"}, where TimeFormat is the
	 * system time format.
	 * 
	 * @param dateTime - the zoned date-time to be formatted
	 * 
	 * @return the formatted date-time, using mostly the system date-time format.
	 * 
	 * @see #formatLocalDateTime(LocalDateTime)
	 * @see #formatLocalDate(LocalTime)
	 * @see #formatLocalTime(LocalTime)
	 */
	public static String formatZonedDateTime(ZonedDateTime dateTime) {
		String dateAtKey;
		switch (getRelativeDate(dateTime.toLocalDate(), dateTime.getZone())) {
		case DATE_TODAY:
			dateAtKey = TODAY_AT_KEY;
			break;
		case DATE_YESTERDAY:
			dateAtKey = YESTERDAY_AT_KEY;
			break;
		case DATE_TOMORROW:
			dateAtKey = TOMORROW_AT_KEY;
			break;
		case DATE_OTHER:
		default:
			dateAtKey = null;
			break;
		}
		StringBuilder sb = new StringBuilder();
		if (dateAtKey != null) {
			sb.append(GSPanelContext.i18nTranslate(dateAtKey));
		} else {
			DATE_FORMAT.formatTo(dateTime, sb);
		}
		sb.append(' ');
		TIME_FORMAT.formatTo(dateTime, sb);
		return sb.toString();
	}
	
	/**
	 * Formats the given date using the system date format and time-zone. If the
	 * given {@code date} represents a date that is today or yesterday, it will be
	 * formatted as {@code "Today"} or {@code "Yesterday"}, respectively.
	 * 
	 * @param date - the date to be formatted
	 * 
	 * @return the formatted date, using mostly the system date format.
	 * 
	 * @see #formatZonedDateTime(ZonedDateTime)
	 */
	public static String formatLocalDate(LocalDate date) {
		String dateKey;
		switch (getRelativeDate(date, ZoneId.systemDefault())) {
		case DATE_TODAY:
			dateKey = TODAY_KEY;
			break;
		case DATE_YESTERDAY:
			dateKey = YESTERDAY_KEY;
			break;
		case DATE_TOMORROW:
			dateKey = TOMORROW_KEY;
			break;
		case DATE_OTHER:
		default:
			dateKey = null;
			break;
		}
		if (dateKey != null)
			return GSPanelContext.i18nTranslate(dateKey);
		return DATE_FORMAT.format(date);
	}
	
	/* Checks if day is today (0), yesterday (1), tomorrow (2), or other (-1) */
	private static int getRelativeDate(LocalDate date, ZoneId zone) {
		LocalDate dateToday = LocalDate.now(zone);
		if (date.equals(dateToday))
			return DATE_TODAY;
		LocalDate dateYesterday = dateToday.minusDays(1);
		if (date.equals(dateYesterday))
			return DATE_YESTERDAY;
		LocalDate dateTomorrow = dateToday.plusDays(1);
		if (date.equals(dateTomorrow))
			return DATE_TOMORROW;
		return DATE_OTHER;
	}
	
	/**
	 * Formats the given time using the system time format.
	 * 
	 * @param time - the time to be formatted
	 * 
	 * @see #formatZonedDateTime(ZonedDateTime)
	 */
	public static String formatLocalTime(LocalTime time) {
		return TIME_FORMAT.format(time);
	}
	
	public static GSLocation getViewLocation(GSPanel panel) {
		int x = 0, y = 0;
		while (panel != null) {
			x += panel.getX();
			y += panel.getY();
			panel = panel.getParent();
		}
		
		return new GSLocation(x, y);
	}
	
	public static int getScrollX(GSPanel panel) {
		GSPanel parent = panel.getParent();
		if (parent instanceof GSViewport)
			return ((GSViewport)parent).getOffsetX();
		return 0;
	}

	public static int getScrollY(GSPanel panel) {
		GSPanel parent = panel.getParent();
		if (parent instanceof GSViewport)
			return ((GSViewport)parent).getOffsetY();
		return 0;
	}
	
	public static void setScrollX(GSPanel panel, int scrollX) {
		GSScrollPanel scrollPanel = getScrollPanel(panel);
		if (scrollPanel != null)
			scrollPanel.getHorizontalScrollBar().setScroll(scrollX);
	}
	
	public static void setScrollY(GSPanel panel, int scrollY) {
		GSScrollPanel scrollPanel = getScrollPanel(panel);
		if (scrollPanel != null)
			scrollPanel.getVerticalScrollBar().setScroll(scrollY);
	}
	
	public static GSScrollPanel getScrollPanel(GSPanel panel) {
		GSPanel parent = panel.getParent();
		if (parent != null) {
			parent = parent.getParent();
			if (parent instanceof GSScrollPanel)
				return (GSScrollPanel)parent;
		}
		return null;
	}

	public static GSDimension getViewportSize(GSPanel panel) {
		GSPanel parent = panel.getParent();
		if (parent instanceof GSViewport)
			return ((GSViewport)parent).getSize();
		return panel.getSize();
	}
	
	private static enum GSEWordCharacterType {
		
		LETTER_OR_DIGIT, SYMBOL, OTHER;
		
	}
}
