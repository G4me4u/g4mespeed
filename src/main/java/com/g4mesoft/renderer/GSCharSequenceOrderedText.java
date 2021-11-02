package com.g4mesoft.renderer;

import com.g4mesoft.core.GSCoreOverride;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

public class GSCharSequenceOrderedText implements OrderedText {

	private final CharSequence sequence;
	private final Style style;

	public GSCharSequenceOrderedText(CharSequence sequence) {
		this(sequence, Style.EMPTY);
	}
	
	public GSCharSequenceOrderedText(CharSequence sequence, Style style) {
		this.sequence = sequence;
		this.style = style;
	}
	
	@Override
	@GSCoreOverride
	public boolean accept(CharacterVisitor visitor) {
		for (int i = 0, len = sequence.length(); i < len; ) {
			char c = sequence.charAt(i);
			if (Character.isHighSurrogate(c) && i + 1 < len) {
				// Character is UTF-16, and we have a trailing surrogate.
				char low = sequence.charAt(i + 1);
				int codePoint = Character.toCodePoint(c, low);
				if (!visitor.accept(i, style, codePoint))
					return false;
				i += 2;
			} else {
				// Character is not UTF-16, simply cast it.
				if (!visitor.accept(i, style, (int)c))
					return false;
				i++;
			}
		}
		return true;
	}
}
