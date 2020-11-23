package com.g4mesoft.renderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

public class GSBasicRenderer2D implements GSIRenderer2D {

	private static final int LINE_SPACING = 2;
	private static final float DEFAULT_Z_OFFSET = 0.0f;
	
	private final MinecraftClient client;
	
	private BufferBuilder builder;
	private MatrixStack matrixStack;
	private int mouseX;
	private int mouseY;
	
	private boolean building;
	private int buildingShape;
	
	private GSTransform2D transform;
	private final LinkedList<GSTransform2D> transformStack;
	
	public GSBasicRenderer2D(MinecraftClient client) {
		this.client = client;
		
		transform = new GSTransform2D();
		transformStack = new LinkedList<>();
	}
	
	public void begin(BufferBuilder builder, MatrixStack matrixStack, int mouseX, int mouseY) {
		this.builder = builder;
		this.matrixStack = matrixStack;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}
	
	public void end() {
		if (building)
			throw new IllegalStateException("Renderer is still building");

		transformStack.clear();
		transform.reset();
		builder = null;
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
	public void pushMatrix() {
		transformStack.push(transform);
		transform = new GSTransform2D(transform);
		
		matrixStack.push();
	}

	@Override
	public void popMatrix() {
		if (transformStack.isEmpty())
			throw new IllegalStateException("Transform stack is empty!");
		
		transform = transformStack.pop();
		matrixStack.pop();

		onTransformChanged();
	}

	@Override
	public void translate(int x, int y) {
		transform.offsetX += x;
		transform.offsetY += y;

		matrixStack.translate(x, y, 0.0f);
		
		onTransformChanged();
	}
	
	private void onTransformChanged() {
		((GSIBufferBuilderAccess)builder).setClipOffset(transform.offsetX, transform.offsetY);
	}
	
	@Override
	public void pushClip(int x, int y, int width, int height) {
		((GSIBufferBuilderAccess)builder).pushClip(x, y, x + width, y + height);
	}

	@Override
	public void pushClip(GSClipRect clip) {
		((GSIBufferBuilderAccess)builder).pushClip(clip);
	}

	@Override
	public GSClipRect popClip() {
		return ((GSIBufferBuilderAccess)builder).popClip();
	}

	@Override
	public void fillRectGradient(int x, int y, int width, int height,
	                             float r0, float g0, float b0, float a0,
	                             float r1, float g1, float b1, float a1) {
		
		if (building && buildingShape != QUADS)
			throw new IllegalStateException("Building quads is required!");
		
		boolean wasBuilding = building;
		if (!wasBuilding)
			build(QUADS, VertexFormats.POSITION_COLOR);
		
		float x0 = (float)x;
		float y0 = (float)y;
		float x1 = x0 + width;
		float y1 = y0 + height;

		vert(x0, y1, DEFAULT_Z_OFFSET).color(r1, g1, b1, a1).next();
		vert(x1, y1, DEFAULT_Z_OFFSET).color(r1, g1, b1, a1).next();
		vert(x1, y0, DEFAULT_Z_OFFSET).color(r0, g0, b0, a0).next();
		vert(x0, y0, DEFAULT_Z_OFFSET).color(r0, g0, b0, a0).next();

		if (!wasBuilding)
			finish();
	}
	
	@Override
	public void drawRect(int x, int y, int width, int height, int color) {
		if (building && buildingShape != QUADS)
			throw new IllegalStateException("Building quads is required!");
		
		boolean wasBuilding = building;
		if (!wasBuilding)
			build(QUADS, VertexFormats.POSITION_COLOR);
		
		drawHLine(x, x + width, y, color);
		drawHLine(x, x + width, y + height - 1, color);
		
		drawVLine(x, y + 1, y + height - 1, color);
		drawVLine(x + width - 1, y + 1, y + height - 1, color);

		if (!wasBuilding)
			finish();
	}

	@Override
	public void drawTexture(GSTexture texture, int x, int y, int width, int height, int sx, int sy) {
		drawTexture(texture.getRegion(sx, sy, width, height), x, y);
	}

	@Override
	public void drawTexture(GSITextureRegion texture, int x, int y) {
		if (building)
			throw new IllegalStateException("Batches are not supported when drawing textures");
		
		RenderSystem.enableTexture();
		client.getTextureManager().bindTexture(texture.getTexture().getIdentifier());
		
		float x0 = (float)x;
		float y0 = (float)y;
		float x1 = x0 + texture.getRegionWidth();
		float y1 = y0 + texture.getRegionHeight();
		
		build(QUADS, VertexFormats.POSITION_TEXTURE);
		vert(x0, y1, DEFAULT_Z_OFFSET).tex(texture.getU0(), texture.getV1()).next();
		vert(x1, y1, DEFAULT_Z_OFFSET).tex(texture.getU1(), texture.getV1()).next();
		vert(x1, y0, DEFAULT_Z_OFFSET).tex(texture.getU1(), texture.getV0()).next();
		vert(x0, y0, DEFAULT_Z_OFFSET).tex(texture.getU0(), texture.getV0()).next();
		finish();
		
		RenderSystem.disableTexture();
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
		if (building && buildingShape != QUADS)
			throw new IllegalStateException("Building quads is required!");
		
		boolean wasBuilding = building;
		if (!wasBuilding)
			build(QUADS, VertexFormats.POSITION_COLOR);
		
		int n = (y1 - y0) / (length + spacing);
		
		for (int yl = 0; yl <= n; yl++) {
			int yl0 = y0 + yl * (length + spacing);
			int yl1 = Math.min(yl0 + length, y1);
			drawVLine(x, yl0, yl1, color);
		}
		
		if (!wasBuilding)
			finish();
	}
	
	@Override
	public void drawDottedHLine(int x0, int x1, int y, int length, int spacing, int color) {
		if (building && buildingShape != QUADS)
			throw new IllegalStateException("Building quads is required!");
		
		boolean wasBuilding = building;
		if (!wasBuilding)
			build(QUADS, VertexFormats.POSITION_COLOR);
		
		int n = (x1 - x0) / (length + spacing);
		
		for (int xl = 0; xl <= n; xl++) {
			int xl0 = x0 + xl * (length + spacing);
			int xl1 = Math.min(xl0 + length, x1);
			drawHLine(xl0, xl1, y, color);
		}
		
		if (!wasBuilding)
			finish();
	}
	
	@Override
	public int getTextHeight() {
		return client.textRenderer.fontHeight;
	}

	@Override
	public int getLineHeight() {
		return client.textRenderer.fontHeight + LINE_SPACING;
	}

	@Override
	public float getTextWidth(String text) {
		return client.textRenderer.getWidth(text);
	}

	@Override
	public void drawText(String text, int x, int y, int color, boolean shadowed) {
		if (shadowed) {
			client.textRenderer.drawWithShadow(matrixStack, text, x, y, color);
		} else {
			client.textRenderer.draw(matrixStack, text, x, y, color);
		}

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
	}
	
	@Override
	public float getTextWidth(OrderedText text) {
		return client.textRenderer.getWidth(text);
	}

	@Override
	public void drawText(OrderedText text, int x, int y, int color, boolean shadowed) {
		if (shadowed) {
			client.textRenderer.drawWithShadow(matrixStack, text, x, y, color);
		} else {
			client.textRenderer.draw(matrixStack, text, x, y, color);
		}
		
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
	}
	
	@Override
	public String trimString(String text, int availableWidth, String ellipsis) {
		int len = text.length();
		if (len <= 0)
			return text;

		// Text fits inside bounds.
		if (getTextWidth(text) <= availableWidth)
			return text;

		availableWidth -= getTextWidth(ellipsis);

		// No space for any other characters.
		if (availableWidth < 0)
			return ellipsis;

		String result = "";
		for (int i = 0; i < len; i++) {
			String substr = text.substring(0, i + 1);
			if (getTextWidth(substr) >= availableWidth)
				return result + ellipsis;
		
			result = substr;
		}

		// This should never happen.
		return result;
	}
	
	@Override
	public List<String> splitToLines(String text, int availableWidth) {
		List<String> result = new ArrayList<>();
		
		int len = text.length();
		if (len <= 0)
			return result;
		
		int lineBegin = 0;
		int lastSpaceIndex = -1;
		
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			int lineWidth = (int)Math.ceil(getTextWidth(text.substring(lineBegin, i)));
			
			if (c == ' ')
				lastSpaceIndex = i;
			
			if (lineWidth > availableWidth) {
				if (lastSpaceIndex != -1) {
					result.add(text.substring(lineBegin, lastSpaceIndex));
					
					i = lastSpaceIndex;
					lineBegin = lastSpaceIndex + 1;
					
					lastSpaceIndex = -1;
				} else {
					result.add(text.substring(lineBegin, i));
					lineBegin = i;
				}
			}
		}

		if (lineBegin != len)
			result.add(text.substring(lineBegin));
		
		return result;
	}
	
	@Override
	public OrderedText trimString(Text text, int availableWidth, String ellipsis) {
		if (getTextWidth(text) <= availableWidth)
			return text.asOrderedText();
		
		availableWidth -= (int)Math.ceil(getTextWidth(ellipsis));
		
		StringVisitable trimmed = client.textRenderer.trimToWidth(text, availableWidth);
		StringVisitable result = StringVisitable.concat(trimmed, new LiteralText(ellipsis));
		
		return Language.getInstance().reorder(result);
	}
	
	@Override
	public List<OrderedText> splitToLines(Text text, int availableWidth) {
		return client.textRenderer.wrapLines(text, availableWidth);
	}
	
	@Override
	public void build(int shape, VertexFormat format) {
		if (building)
			throw new IllegalStateException("Already building!");
		
		builder.begin(shape, format);
		
		buildingShape = shape;
		building = true;
	}

	@Override
	public GSBasicRenderer2D vert(float x, float y, float z) {
		builder.vertex(x + transform.offsetX, y + transform.offsetY, z);
		return this;
	}

	@Override
	public GSBasicRenderer2D color(float r, float g, float b, float a) {
		builder.color(r, g, b, a);
		return this;
	}

	@Override
	public GSBasicRenderer2D tex(float u, float v) {
		builder.texture(u, v);
		return this;
	}

	@Override
	public GSBasicRenderer2D next() {
		builder.next();
		return this;
	}
	
	@Override
	public void finish() {
		if (!building)
			throw new IllegalStateException("Not building!");
		
		Tessellator.getInstance().draw();
		building = false;
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
