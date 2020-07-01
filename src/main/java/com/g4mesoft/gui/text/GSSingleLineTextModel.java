package com.g4mesoft.gui.text;

/**
 * A single-line text-model used for storing and handling model data in simple
 * panels such as the {@link GSTextField}. This model ensures that characters
 * which represent a new line will be discarded. Doing this ensures that the model
 * will always stay single-lined. The new-line characters that are considered by
 * this model are the following:
 * <ul>
 * <li> Unicode line separator {@code '\u2028'}.
 * <li> Unicode paragraph separator {@code '\u2029'}.
 * <li> ASCII line feed {@code '\n'}.
 * <li> ASCII carriage return {@code '\r'}.
 * <li> ASCII vertical tab {@code '\u000B'}.
 * <li> ASCII form feed {@code '\f'}.
 * </ul>
 * If one wishes to discard more characters, or others, than the ones listed
 * above, it is possible to extend the {@code GSSingleLineTextModel} and override
 * the function {@link #shouldDiscardCharacter(char)}. A simple example of this
 * is a model that discards all whitespace characters (including the ones above)
 * that are inserted:
 * <pre>
 * public class NoWhitespaceTextModel extends GSSingleLineTextModel {
 * 
 *     {@literal @}Override
 *     protected boolean shouldDiscardCharacter(char c) {
 *         return Character.isWhitespace(c);
 *     }
 * }
 * </pre>
 * <b>NOTE:</b> the above example will only still discard new-line characters
 * because the {@link java.lang.Character#isWhitespace(char) isWhitespace(char)}
 * function considers these. If one still wishes to discard new-line characters,
 * but their implementation of the above method does not consider it, they can
 * call the sub-implementation {@code super.shouldDiscardCharacter(c)} to test
 * for new-line.
 * 
 * @author Christian
 */
public class GSSingleLineTextModel extends GSAbstractTextModel {

	private static final char LINE_SEPARATOR = '\u2028';
	private static final char PARAGRAPH_SEPARATOR = '\u2029';
	
	private static final char LINE_FEED = '\n';
	private static final char CARRIAGE_RETURN = '\r';
	
	private static final char VERTICAL_TAB = '\u000B';
	private static final char FORM_FEED = '\f';
	
	private static final int DEFAULT_INITIAL_CAPACITY = 16;
	
	private char[] buffer;
	private int length;
	
	private String cacheText;

	public GSSingleLineTextModel() {
		this(DEFAULT_INITIAL_CAPACITY);
	}
	
	public GSSingleLineTextModel(int initialCapacity) {
		if (initialCapacity <= 0)
			throw new IllegalArgumentException("initialCapacity is <= 0");
		
		buffer = new char[initialCapacity];
		length = 0;
	}
	
	/**
	 * Ensures that there is capacity in the buffer to fit the given amount of
	 * characters.
	 * 
	 * @param count - the amount of characters that must fit inside the buffer.
	 */
	private void ensureCapacity(int count) {
		int capacity = getCapacity();
		
		int newLength = length + count;
		if (capacity >= newLength)
			return;

		// Double capacity
		capacity <<= 1;
		
		if (newLength > capacity)
			capacity = newLength;

		char[] newBuffer = new char[capacity];
		System.arraycopy(buffer, 0, newBuffer, 0, length);
		buffer = newBuffer;
	}
	
	/**
	 * Returns the amount of characters that are able to fit into the model
	 * without having to grow the size of the buffer. The amount of characters
	 * that can still be inserted into this model without the growing occurring
	 * is therefore equal to {@code padding = capacity - length}. If the padding
	 * is zero then the buffer is full, and will need to grow if more characters
	 * have to fit inside.
	 * 
	 * @return The capacity of this model.
	 */
	public int getCapacity() {
		return buffer.length;
	}
	
	@Override
	public int getLength() {
		return length;
	}

	/**
	 * Returns true, if the given character should be discarded by this model.
	 * The default characters which are discarded are described in the class
	 * {@link com.g4mesoft.gui.text.GSSingleLineTextModel doc}.
	 * 
	 * @param c - the character that should be checked.
	 * 
	 * @return True, if the character should be discarded by this model.
	 */
	protected boolean shouldDiscardCharacter(char c) {
		// Check Unicode separators
		if (c == LINE_SEPARATOR || c == PARAGRAPH_SEPARATOR)
			return true;
		
		// The usual new-line characters that are
		// available on modern keyboards.
		if (c == LINE_FEED || c == CARRIAGE_RETURN)
			return true;
		
		// The less usual line-separators
		return c == VERTICAL_TAB || c == FORM_FEED;
	}
	
	@Override
	public void insertText(int offset, String text) {
		if (offset < 0 || offset > length)
			throw new GSTextModelIndexOutOfBoundsException(offset);
		
		int count = text.length();
		int n = count;
		
		for (int i = 0; i < count; i++) {
			if (shouldDiscardCharacter(text.charAt(i)))
				n--;
		}

		if (n != 0) {
			// Only ensure what is needed
			ensureCapacity(n);

			if (offset != length)
				System.arraycopy(buffer, offset, buffer, offset + n, length - offset);

			if (n != count) {
				// We need to check every character again.
				for (int i = 0, p = offset; i < count; i++) {
					char c = text.charAt(i);
					if (!shouldDiscardCharacter(c))
						buffer[p++] = c;
				}
			} else {
				text.getChars(0, n, buffer, offset);
			}
	
			length += n;
			// Discard the cache
			cacheText = null;
		}
		
		dispatchTextInsertedEvent(offset, n);
	}

	@Override
	public void insertChars(int offset, int count, char[] charBuffer, int bufferOffset) {
		if (offset < 0 ||offset > length)
			throw new GSTextModelIndexOutOfBoundsException(offset);

		// Same implementation as insertText(int, String), but
		// slightly altered to handle character-arrays.
		int bufferEnd = bufferOffset + count;
		int n = count;
		
		for (int i = bufferOffset; i < bufferEnd; i++) {
			if (shouldDiscardCharacter(charBuffer[i]))
				n--;
		}

		if (n != 0) {
			ensureCapacity(n);

			if (offset != length)
				System.arraycopy(buffer, offset, buffer, offset + n, length - offset);
			
			if (n != count) {
				for (int i = bufferOffset, p = offset; i < bufferEnd; i++) {
					char c = charBuffer[i];
					if (!shouldDiscardCharacter(c))
						buffer[p++] = c;
				}
			} else {
				System.arraycopy(charBuffer, bufferOffset, buffer, offset, n);
			}
			
			length += n;
			cacheText = null;
		}

		dispatchTextInsertedEvent(offset, n);
	}
	
	@Override
	public void insertChar(int offset, char c) {
		if (offset < 0 || offset > length)
			throw new GSTextModelIndexOutOfBoundsException(offset);

		if (!shouldDiscardCharacter(c)) {
			ensureCapacity(1);
		
			if (offset != length)
				System.arraycopy(buffer, offset, buffer, offset + 1, length - offset);
			
			buffer[offset] = c;
			
			length++;
			cacheText = null;
			
			dispatchTextInsertedEvent(offset, 1);
		} else {
			dispatchTextInsertedEvent(offset, 0);
		}
	}

	@Override
	public void removeText(int offset, int count) {
		if (offset < 0 || offset >= length)
			throw new GSTextModelIndexOutOfBoundsException(offset);
		if (count < 0)
			throw new GSTextModelIndexOutOfBoundsException(count);
		
		int end = offset + count;
		if (end > length)
			throw new GSTextModelIndexOutOfBoundsException(length);
		
		// Shift model to the left
		while (end != length) {
			buffer[end - count] = buffer[end];
			end++;
		}

		length -= count;
		cacheText = null;
		
		dispatchTextRemovedEvent(offset, count);
	}

	@Override
	public String getText(int offset, int count) {
		if (offset == 0 && count == length) {
			// Usually a user of the GSTextField will want to
			// retrieve the entire model at once. We cache the
			// model in a string to save allocations
			if (cacheText == null)
				cacheText = new String(buffer, 0, length);
			return cacheText;
		}
		
		if (offset < 0 || offset >= length)
			throw new GSTextModelIndexOutOfBoundsException(offset);
		if (count < 0)
			throw new GSTextModelIndexOutOfBoundsException(count);
		if (offset + count > length)
			throw new GSTextModelIndexOutOfBoundsException(length);
		
		return new String(buffer, offset, count);
	}

	@Override
	public void getChars(int offset, int count, char[] charBuffer, int bufferOffset) {
		if (offset < 0 || offset >= length)
			throw new GSTextModelIndexOutOfBoundsException(offset);
		if (count < 0)
			throw new GSTextModelIndexOutOfBoundsException(count);
		if (offset + count > length)
			throw new GSTextModelIndexOutOfBoundsException(length);
		
		System.arraycopy(buffer, offset, charBuffer, bufferOffset, count);
	}

	@Override
	public char getChar(int offset) {
		if (offset < 0 || offset >= length)
			throw new GSTextModelIndexOutOfBoundsException(offset);

		return buffer[offset];
	}
}
