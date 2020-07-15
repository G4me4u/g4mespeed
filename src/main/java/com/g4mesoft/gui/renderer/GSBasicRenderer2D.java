package com.g4mesoft.gui.renderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

public class GSBasicRenderer2D implements GSIRenderer2D {

	private static final int LINE_SPACING = 2;
	
	private static final char FORMATTING_CHAR = '\u00A7';
	
	private final MinecraftClient client;
	
	private MatrixStack matrixStack;
	private int mouseX;
	private int mouseY;
	private float partialTicks;
	
	private GSTransform2D transform;
	private final LinkedList<GSTransform2D> transformStack;
	
	public GSBasicRenderer2D(MinecraftClient client) {
		this.client = client;
		
		transform = new GSTransform2D();
		transformStack = new LinkedList<GSTransform2D>();
	}
	
	@Override
	public void beginRendering(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.matrixStack = matrixStack;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.partialTicks = partialTicks;
	}
	
	@Override
	public void endRendering() {
		transformStack.clear();
		resetTransform();
	}
	
	@Override
	public int getMouseX() {
		return mouseX - transform.offsetX;
	}

	@Override
	public int getMouseY() {
		return mouseY - transform.offsetY;
	}

	@Override
	public float getPartialTicks() {
		return partialTicks;
	}

	@Override
	public void fillRect(int x, int y, int width, int height, int color) {
		DrawableHelper.fill(matrixStack, x, y, x + width, y + height, color);
	}
	
	@Override
	public void fillRectGradient(int x, int y, int width, int height, int tlColor, int brColor) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		int a0 = (tlColor >>> 24) & 0xFF;
		int r0 = (tlColor >>> 16) & 0xFF;
		int g0 = (tlColor >>>  8) & 0xFF;
		int b0 = (tlColor >>>  0) & 0xFF;

		int a1 = (brColor >>> 24) & 0xFF;
		int r1 = (brColor >>> 16) & 0xFF;
		int g1 = (brColor >>>  8) & 0xFF;
		int b1 = (brColor >>>  0) & 0xFF;

		int x1 = x + width;
		int y1 = y + height;
		
		Matrix4f modlMat = matrixStack.peek().getModel();
		
		bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(modlMat,  x, y1, 0.0f).color(r1, g1, b1, a1).next();
		bufferBuilder.vertex(modlMat, x1, y1, 0.0f).color(r1, g1, b1, a1).next();
		bufferBuilder.vertex(modlMat, x1,  y, 0.0f).color(r0, g0, b0, a0).next();
		bufferBuilder.vertex(modlMat,  x,  y, 0.0f).color(r0, g0, b0, a0).next();
		
		RenderSystem.disableTexture();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);

		if (a0 != 0xFF || a1 != 0xFF) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();		
		}
		
		tessellator.draw();

		RenderSystem.disableBlend();

		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.enableTexture();
	}
	
	@Override
	public void drawRect(int x, int y, int width, int height, int color) {
		drawHLine(x, x + width, y, color);
		drawHLine(x, x + width, y + height - 1, color);
		
		drawVLine(x, y + 1, y + height - 1, color);
		drawVLine(x + width - 1, y + 1, y + height - 1, color);
	}

	@Override
	public void drawTexture(GSTexture texture, int x, int y, int width, int height, int sx, int sy) {
		drawTexture(texture.getRegion(sx, sy, width, height), x, y);
	}

	@Override
	public void drawTexture(GSITextureRegion texture, int x, int y) {
		client.getTextureManager().bindTexture(texture.getTexture().getIdentifier());
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		int x1 = x + texture.getRegionWidth();
		int y1 = y + texture.getRegionHeight();
		
		Matrix4f modlMat = matrixStack.peek().getModel();
		
		bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		bufferBuilder.vertex(modlMat,  x, y1, 0.0f).texture(texture.getU0(), texture.getV1()).next();
		bufferBuilder.vertex(modlMat, x1, y1, 0.0f).texture(texture.getU1(), texture.getV1()).next();
		bufferBuilder.vertex(modlMat, x1,  y, 0.0f).texture(texture.getU1(), texture.getV0()).next();
		bufferBuilder.vertex(modlMat,  x,  y, 0.0f).texture(texture.getU0(), texture.getV0()).next();
		
		RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		
		tessellator.draw();

		RenderSystem.disableAlphaTest();
	}

	@Override
	public void drawVLine(int x, int y0, int y1, int color) {
		fillRect(x, y0, 1, y1 - y0, color);
	}

	@Override
	public void drawHLine(int x0, int x1, int y, int color) {
		fillRect(x0, y, x1 - x0, 1, color);
	}
	
	@Override
	public void drawDottedVLine(int x, int y0, int y1, int length, int spacing, int color) {
		int n = (y1 - y0) / (length + spacing);
		
		for (int yl = 0; yl <= n; yl++) {
			int yl0 = y0 + yl * (length + spacing);
			int yl1 = Math.min(yl0 + length, y1);
			drawVLine(x, yl0, yl1, color);
		}
	}
	
	@Override
	public void drawDottedHLine(int x0, int x1, int y, int length, int spacing, int color) {
		int n = (x1 - x0) / (length + spacing);
		
		for (int xl = 0; xl <= n; xl++) {
			int xl0 = x0 + xl * (length + spacing);
			int xl1 = Math.min(xl0 + length, x1);
			drawHLine(xl0, xl1, y, color);
		}
	}
	
	@Override
	public int getFontHeight() {
		return client.textRenderer.fontHeight;
	}

	@Override
	public int getLineHeight() {
		return client.textRenderer.fontHeight + LINE_SPACING;
	}

	@Override
	public float getStringWidth(String str) {
		return client.textRenderer.getWidth(str);
	}

	@Override
	public String trimString(String str, int availableWidth, String ellipsis) {
		int len = str.length();
		if (len <= 0)
			return str;

		// Text fits inside bounds.
		if (getStringWidth(str) <= availableWidth)
			return str;

		availableWidth -= getStringWidth(ellipsis);

		// No space for any other
		// characters.
		if (availableWidth < 0)
			return ellipsis;

		String result = "";
		for (int i = 0; i < len; i++) {
			String substr = str.substring(0, i + 1);
			if (getStringWidth(substr) >= availableWidth)
				return result + ellipsis;
		
			result = substr;
		}

		// This should never happen.
		return result;
	}
	
	@Override
	public List<String> splitToLines(String str, int availableWidth) {
		List<String> result = new ArrayList<String>();
		
		int len = str.length();
		if (len <= 0)
			return result;
		
		int lineBegin = 0;
		int lastSpaceIndex = -1;
		
		String formattingNextLine = "";
		String formattingThisLine = formattingNextLine;
		
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			if (c == FORMATTING_CHAR) {
				i++;
				
				if (i < len) {
					c = str.charAt(i);
					if (c == 'r') {
						formattingNextLine = "";
					} else {
						formattingNextLine += Character.toString(FORMATTING_CHAR) + c;
					}
				}
			} else {
				int lineWidth = (int)Math.ceil(getStringWidth(str.substring(lineBegin, i)));
				
				if (c == ' ')
					lastSpaceIndex = i;
				
				if (lineWidth > availableWidth) {
					if (lastSpaceIndex != -1) {
						result.add(formattingThisLine + str.substring(lineBegin, lastSpaceIndex));
						formattingThisLine = formattingNextLine;
						
						i = lastSpaceIndex;
						lineBegin = lastSpaceIndex + 1;
						
						lastSpaceIndex = -1;
					} else {
						result.add(str.substring(lineBegin, i));
						lineBegin = i;
					}
				}
			}
		}

		if (lineBegin != len)
			result.add(formattingThisLine + str.substring(lineBegin));
		
		return result;
	}

	@Override
	public void drawString(String str, int x, int y, int color, boolean shadowed) {
		if ((color >>> 24) != 0xFF) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
		}
		
		if (shadowed) {
			client.textRenderer.drawWithShadow(matrixStack, str, x, y, color);
		} else {
			client.textRenderer.draw(matrixStack, str, x, y, color);
		}

		RenderSystem.disableBlend();
	}

	@Override
	public void pushClip(int x, int y, int width, int height) {
		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		((GSIBufferBuilderAccess)builder).pushClip(x, y, x + width, y + height);
	}

	@Override
	public void pushClip(GSClipRect clip) {
		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		((GSIBufferBuilderAccess)builder).pushClip(clip);
	}

	@Override
	public GSClipRect popClip() {
		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		return ((GSIBufferBuilderAccess)builder).popClip();
	}
	
	@Override
	public void pushTransform() {
		transformStack.push(transform);
		transform = new GSTransform2D(transform);
		
		matrixStack.push();
	}

	@Override
	public void popTransform() {
		if (transformStack.isEmpty())
			throw new IllegalStateException("Transform stack is empty!");
		
		transform = transformStack.pop();
		onTransformChanged();

		matrixStack.pop();
	}

	public void resetTransform() {
		transform.reset();
		
		matrixStack.peek().getModel().loadIdentity();
	}
	
	@Override
	public void translate(int x, int y) {
		transform.offsetX += x;
		transform.offsetY += y;
		
		matrixStack.translate(x, y, 0.0);

		onTransformChanged();
	}
	
	private void onTransformChanged() {
		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		((GSIBufferBuilderAccess)builder).setClipOffset(transform.offsetX, transform.offsetY);
	}

	private class GSTransform2D {
		
		private int offsetX;
		private int offsetY;
		
		public GSTransform2D() {
			reset();
		}

		public GSTransform2D(GSTransform2D other) {
			offsetX = other.offsetX;
			offsetY = other.offsetY;
		}
		
		private void reset() {
			offsetX = offsetY = 0;
		}
	}
}
