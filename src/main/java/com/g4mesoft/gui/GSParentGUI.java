package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class GSParentGUI extends Screen {

	private static final String TRIMMED_TEXT_ELLIPSIS = "...";
	private static final char FORMATTING_CHAR = '\u00A7';
	
	private boolean selected;
	
	private int x;
	private int y;
	
	protected GSParentGUI(Text title) {
		super(title);
		
		setSelected(true);
	}
	
	public void initBounds(MinecraftClient client, int x, int y, int width, int height) {
		super.init(client, width, height);
	
		this.x = x;
		this.y = y;
	}
	
	@Override
	public final void init(MinecraftClient client, int width, int height) {
		initBounds(client, 0, 0, width, height);
	}
	
	public void setBounds(int x, int y, int width, int height) {
		super.setSize(width, height);
		
		this.x = x;
		this.y = y;
	}

	@Override
	public final void setSize(int width, int height) {
		setBounds(x, y, width, height);
	}
	
	protected int getTranslationX() {
		return x;
	}

	protected int getTranslationY() {
		return y;
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		int tx = getTranslationX();
		int ty = getTranslationY();
		
		GlStateManager.translatef( tx,  ty, 0.0f);
		renderTranslated(mouseX - tx, mouseY - ty, partialTicks);
		GlStateManager.translatef(-tx, -ty, 0.0f);
	}
	
	protected void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}
	
	private double translateMouseX(double mouseX) {
		return mouseX - getTranslationX();
	}

	private double translateMouseY(double mouseY) {
		return mouseY - getTranslationY();
	}
	
	@Override
	public final boolean mouseClicked(double mouseX, double mouseY, int button) {
		return selected && mouseClickedTranslated(translateMouseX(mouseX), translateMouseY(mouseY), button);
	}

	protected boolean mouseClickedTranslated(double mouseX, double mouseY, int button) {
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public final boolean mouseReleased(double mouseX, double mouseY, int button) {
		return selected && mouseReleasedTranslated(translateMouseX(mouseX), translateMouseY(mouseY), button);
	}

	protected boolean mouseReleasedTranslated(double mouseX, double mouseY, int button) {
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public final boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return selected && mouseDraggedTranslated(translateMouseX(mouseX), translateMouseY(mouseY), button, dragX, dragY);
	}
	
	protected boolean mouseDraggedTranslated(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean keyPressed(int key, int scancode, int mods) {
		return selected && super.keyPressed(key, scancode, mods);
	}

	@Override
	public boolean keyReleased(int key, int scancode, int mods) {
		return selected && super.keyReleased(key, scancode, mods);
	}

	@Override
	public boolean charTyped(char c, int mods) {
		return selected && super.charTyped(c, mods);
	}

	@Override
	public final boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		if (!selected)
			return false;
		return mouseScrolledTranslated(translateMouseX(mouseX), translateMouseY(mouseY), scroll);
	}
	
	protected boolean mouseScrolledTranslated(double mouseX, double mouseY, double scroll) {
		return super.mouseScrolled(mouseX, mouseY, scroll);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (!selected)
			return false;
		
		return mouseX >= x && mouseX < x + width && 
		       mouseY >= y && mouseY < y + height;
	}
	
	protected String trimText(String text, int availableWidth) {
		int len = text.length();
		if (len <= 0)
			return text;

		// Text fits inside bounds.
		if (font.getStringWidth(text) <= availableWidth)
			return text;

		availableWidth -= font.getStringWidth(TRIMMED_TEXT_ELLIPSIS);

		// No space for any other
		// characters.
		if (availableWidth < 0)
			return TRIMMED_TEXT_ELLIPSIS;

		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			// Should probably use getStringWidth
			// and substring instead, but for
			// optimization we use getCharWidth.
			availableWidth -= font.getCharWidth(c);

			if (availableWidth < 0)
				return text.substring(0, i) + TRIMMED_TEXT_ELLIPSIS;
		}

		// This should never happen.
		return text;
	}
	
	public List<String> splitToLines(String text, int availableWidth) {
		List<String> result = new ArrayList<String>();
		
		int len = text.length();
		if (len <= 0)
			return result;
		
		int lineWidth = 0;
		int lineBegin = 0;
		
		int lastSpaceIndex = -1;
		
		String formattingNextLine = "";
		String formattingThisLine = formattingNextLine;
		
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			if (c == FORMATTING_CHAR) {
				i++;
				
				if (i < len) {
					c = text.charAt(i);
					if (c == 'r') {
						formattingNextLine = "";
					} else {
						formattingNextLine += Character.toString(FORMATTING_CHAR) + c;
					}
				}
			} else {
				lineWidth += font.getCharWidth(c);
				
				if (c == ' ')
					lastSpaceIndex = i;
				
				if (lineWidth > availableWidth) {
					if (lastSpaceIndex != -1) {
						result.add(formattingThisLine + text.substring(lineBegin, lastSpaceIndex));
						formattingThisLine = formattingNextLine;
						
						i = lastSpaceIndex;
						lineBegin = lastSpaceIndex + 1;
						
						lastSpaceIndex = -1;
					} else {
						result.add(text.substring(lineBegin, i));
						lineBegin = i;
					}
					
					lineWidth = 0;
				}
			}
		}

		if (lineBegin != len)
			result.add(formattingThisLine + text.substring(lineBegin));
		
		return result;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public GSTranslationModule getTranslationModule() {
		return GSControllerClient.getInstance().getTranslationModule();
	}
}