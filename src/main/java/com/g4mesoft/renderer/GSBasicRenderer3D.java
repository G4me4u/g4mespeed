package com.g4mesoft.renderer;

import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

public class GSBasicRenderer3D implements GSIRenderer3D {

	private BufferBuilder builder;
	private MatrixStack matrixStack;
	
	private boolean building;
	private DrawMode buildingDrawMode;
	
	public void begin(BufferBuilder builder, MatrixStack matrixStack) {
		this.builder = builder;
		this.matrixStack = matrixStack;
	}
	
	public void end() {
		if (building)
			throw new IllegalStateException("Renderer is still building");
		
		matrixStack = null;
		builder = null;
	}
	
	@Override
	public void pushMatrix() {
		matrixStack.push();
	}

	@Override
	public void popMatrix() {
		matrixStack.pop();
	}

	@Override
	public void translate(float tx, float ty, float tz) {
		matrixStack.translate(tx, ty, tz);
	}

	@Override
	public void rotate(float rx, float ry, float rz) {
		matrixStack.multiply(new Quaternionf().rotateXYZ(rx, ry, rz));
	}

	@Override
	public void scale(float sx, float sy, float sz) {
		matrixStack.scale(sx, sy, sz);
	}
	
	@Override
	public void fillCuboid(float x0, float y0, float z0,
	                       float x1, float y1, float z1,
	                       float r, float g, float b, float a) {
		
		if (building && buildingDrawMode != DrawMode.QUADS)
			throw new IllegalStateException("Building quads is required!");
		
		boolean wasBuilding = building;
		if (!wasBuilding)
			build(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		
		// Back Face
		vert(x0, y0, z0).color(r, g, b, a).next();
		vert(x0, y0, z1).color(r, g, b, a).next();
		vert(x0, y1, z1).color(r, g, b, a).next();
		vert(x0, y1, z0).color(r, g, b, a).next();

		// Front Face
		vert(x1, y0, z0).color(r, g, b, a).next();
		vert(x1, y1, z0).color(r, g, b, a).next();
		vert(x1, y1, z1).color(r, g, b, a).next();
		vert(x1, y0, z1).color(r, g, b, a).next();

		// Left Face
		vert(x0, y0, z0).color(r, g, b, a).next();
		vert(x0, y1, z0).color(r, g, b, a).next();
		vert(x1, y1, z0).color(r, g, b, a).next();
		vert(x1, y0, z0).color(r, g, b, a).next();

		// Right Face
		vert(x0, y0, z1).color(r, g, b, a).next();
		vert(x1, y0, z1).color(r, g, b, a).next();
		vert(x1, y1, z1).color(r, g, b, a).next();
		vert(x0, y1, z1).color(r, g, b, a).next();

		// Bottom Face
		vert(x0, y0, z0).color(r, g, b, a).next();
		vert(x1, y0, z0).color(r, g, b, a).next();
		vert(x1, y0, z1).color(r, g, b, a).next();
		vert(x0, y0, z1).color(r, g, b, a).next();

		// Top Face
		vert(x0, y1, z0).color(r, g, b, a).next();
		vert(x0, y1, z1).color(r, g, b, a).next();
		vert(x1, y1, z1).color(r, g, b, a).next();
		vert(x1, y1, z0).color(r, g, b, a).next();
		
		if (!wasBuilding)
			finish();
	}

	@Override
	public void drawCuboidOutline(float x0, float y0, float z0,
	                              float x1, float y1, float z1,
	                              float r, float g, float b, float a) {
		
		if (building && buildingDrawMode != DrawMode.LINES)
			throw new IllegalStateException("Building lines is required!");
		
		boolean wasBuilding = building;
		if (!wasBuilding)
			build(DrawMode.LINES, VertexFormats.POSITION_COLOR);
		
		// Lines on X-axis
		vert(x0, y0, z0).color(r, g, b, a).next();
		vert(x1, y0, z0).color(r, g, b, a).next();
		vert(x0, y1, z0).color(r, g, b, a).next();
		vert(x1, y1, z0).color(r, g, b, a).next();
		vert(x0, y1, z1).color(r, g, b, a).next();
		vert(x1, y1, z1).color(r, g, b, a).next();
		vert(x0, y0, z1).color(r, g, b, a).next();
		vert(x1, y0, z1).color(r, g, b, a).next();

		// Lines on Y-axis
		vert(x0, y0, z0).color(r, g, b, a).next();
		vert(x0, y1, z0).color(r, g, b, a).next();
		vert(x1, y0, z0).color(r, g, b, a).next();
		vert(x1, y1, z0).color(r, g, b, a).next();
		vert(x1, y0, z1).color(r, g, b, a).next();
		vert(x1, y1, z1).color(r, g, b, a).next();
		vert(x0, y0, z1).color(r, g, b, a).next();
		vert(x0, y1, z1).color(r, g, b, a).next();

		// Lines on Z-axis
		vert(x0, y0, z0).color(r, g, b, a).next();
		vert(x0, y0, z1).color(r, g, b, a).next();
		vert(x1, y0, z0).color(r, g, b, a).next();
		vert(x1, y0, z1).color(r, g, b, a).next();
		vert(x1, y1, z0).color(r, g, b, a).next();
		vert(x1, y1, z1).color(r, g, b, a).next();
		vert(x0, y1, z0).color(r, g, b, a).next();
		vert(x0, y1, z1).color(r, g, b, a).next();
		
		if (!wasBuilding)
			finish();
	}

	@Override
	public void build(DrawMode drawMode, VertexFormat format) {
		if (building)
			throw new IllegalStateException("Already building!");
		
		if (format == VertexFormats.POSITION) {
			RenderSystem.setShader(GameRenderer::getPositionProgram);
		} else if (format == VertexFormats.POSITION_COLOR) {
			RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		} else if (format == VertexFormats.POSITION_COLOR_LIGHT) {
			RenderSystem.setShader(GameRenderer::getPositionColorLightmapProgram);
		} else if (format == VertexFormats.POSITION_COLOR_TEXTURE) {
			RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
		} else if (format == VertexFormats.POSITION_COLOR_TEXTURE_LIGHT) {
			RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);
		} else if (format == VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL) {
			RenderSystem.setShader(GameRenderer::getBlockProgram);
		} else if (format == VertexFormats.POSITION_TEXTURE) {
			RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		} else if (format == VertexFormats.POSITION_TEXTURE_COLOR) {
			RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		} else if (format == VertexFormats.POSITION_TEXTURE_COLOR_NORMAL) {
			RenderSystem.setShader(GameRenderer::getPositionTexColorNormalProgram);
		} else {
			throw new IllegalArgumentException("Unsupported vertex format!");
		}
		
		builder.begin(drawMode, format);
		
		buildingDrawMode = drawMode;
		building = true;
	}

	@Override
	public GSBasicRenderer3D vert(float x, float y, float z) {
		builder.vertex(matrixStack.peek().getPositionMatrix(), x, y, z);
		return this;
	}

	@Override
	public GSBasicRenderer3D color(float r, float g, float b, float a) {
		builder.color(r, g, b, a);
		return this;
	}

	@Override
	public GSBasicRenderer3D tex(float u, float v) {
		builder.texture(u, v);
		return this;
	}

	@Override
	public GSBasicRenderer3D next() {
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
}
