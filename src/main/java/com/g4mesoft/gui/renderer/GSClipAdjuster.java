package com.g4mesoft.gui.renderer;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import org.lwjgl.opengl.GL11;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.g4mesoft.mixin.client.GSIVertexFormatElementAccess;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;

public class GSClipAdjuster {

	private final float[] clipXBuffer;
	private final float[] clipYBuffer;
	
	private float clipOffsetX;
	private float clipOffsetY;
	
	private LinkedList<GSClipRect> clipRectStack;
	
	public GSClipAdjuster() {
		clipXBuffer = new float[4];
		clipYBuffer = new float[4];
	
		clipOffsetX = clipOffsetY = 0.0f;
	
		clipRectStack = new LinkedList<GSClipRect>();
	}
	
	public void clipPreviousShape(BufferBuilder builder) {
		if (clipRectStack.isEmpty() || ((GSIBufferBuilderAccess)builder).getDrawMode() != GL11.GL_QUADS)
			return;
		
		int vertexStart = ((GSIBufferBuilderAccess)builder).getVertexCount() - 4;
		if (vertexStart < 0)
			return;

		VertexFormat format = ((GSIBufferBuilderAccess)builder).getVertexFormat();
		ByteBuffer buffer = ((GSIBufferBuilderAccess)builder).getByteBuffer();
		
		int startIndex = ((GSIBufferBuilderAccess)builder).getBuildStart() + vertexStart * format.getVertexSize();
		
		// Assume the quad is on the x-y plane where z = z0 for
		// all the vertices. Also assume that the sides of the
		// quad are all parallel with the x and y axis.
		clipXBuffer[0] = buffer.getFloat(startIndex + 0);
		clipYBuffer[0] = buffer.getFloat(startIndex + 4);

		float minimumSum = clipXBuffer[0] + clipYBuffer[0];
		int bli = 0;
		
		int index = startIndex + format.getVertexSize();
		for (int i = 1; i < 4; i++, index += format.getVertexSize()) {
			clipXBuffer[i] = buffer.getFloat(index + 0);
			clipYBuffer[i] = buffer.getFloat(index + 4);
		
			float sum = clipXBuffer[i] + clipYBuffer[i];
			if (sum < minimumSum) {
				minimumSum = sum;
				bli = i;
			}
		}
		
		int tri = (bli + 2) & 0x3;

		float x0 = clipXBuffer[bli];
		float y0 = clipYBuffer[bli];
		float x1 = clipXBuffer[tri];
		float y1 = clipYBuffer[tri];
		
		GSClipRect clipRect = clipRectStack.peek();
		
		// Check if quad is within clip bounds.
		if (x1 < clipRect.x0 || x0 >= clipRect.x1 || 
		    y1 < clipRect.y0 || y0 >= clipRect.y1) {
			
			((GSIBufferBuilderAccess)builder).setVertexCount(vertexStart);
			((GSIBufferBuilderAccess)builder).setElementOffset(startIndex);
			
			return;
		}


		// Check what rotation anti-clockwise orientation would require
		// of the corners tli and bri.
		int bri = (bli + 1) & 0x3;
		int tli;
		if (GSMathUtils.equalsApproximate(x0, clipXBuffer[bri])) {
			tli = bri;
			bri = (bli - 1) & 0x3;
		} else {
			tli = (bli - 1) & 0x3;
		}
		
		// Verify that the quad is actually aligned with the x and y axis.
		if (!GSMathUtils.equalsApproximate(x0, clipXBuffer[tli]) ||
		    !GSMathUtils.equalsApproximate(x1, clipXBuffer[bri]) ||
		    !GSMathUtils.equalsApproximate(y0, clipYBuffer[bri]) ||
		    !GSMathUtils.equalsApproximate(y1, clipYBuffer[tli])) {
			
			return;
		}
		
		bli = (vertexStart + bli) * format.getVertexSize();
		bri = (vertexStart + bri) * format.getVertexSize();
		tri = (vertexStart + tri) * format.getVertexSize();
		tli = (vertexStart + tli) * format.getVertexSize();
		
		float w = (x1 - x0);
		float t0 = (clipRect.x0 - x0) / w;
		float t1 = (clipRect.x1 - x0) / w;
		if (t0 > 0.0f || t1 < 1.0f) {
			interpolateClipped(buffer, format, bli, bri, t0, t1);
			interpolateClipped(buffer, format, tli, tri, t0, t1);
		}
		
		float h = (y1 - y0);
		t0 = (clipRect.y0 - y0) / h;
		t1 = (clipRect.y1 - y0) / h;
		if (t0 > 0.0f || t1 < 1.0f) {
			interpolateClipped(buffer, format, bli, tli, t0, t1);
			interpolateClipped(buffer, format, bri, tri, t0, t1);
		}
	}
	
	private void interpolateClipped(ByteBuffer buffer, VertexFormat format, int i0, int i1, float t0, float t1) {
		for (VertexFormatElement vertexElement : format.getElements()) {
			if (vertexElement.getType() != VertexFormatElement.Type.PADDING) {
				VertexFormatElement.Format vertexElementFormat = vertexElement.getFormat();
				for (int i = 0; i < ((GSIVertexFormatElementAccess)vertexElement).getCount(); i++) {
					float v0 = getVertexElement(buffer, i0, vertexElementFormat);
					float v1 = getVertexElement(buffer, i1, vertexElementFormat);
	
					float dv = v1 - v0;
					if (t0 > 0.0f)
						setVertexElement(buffer, i0, vertexElementFormat, v0 + dv * t0);
					if (t1 < 1.0f)
						setVertexElement(buffer, i1, vertexElementFormat, v0 + dv * t1);
				
					i0 += vertexElementFormat.getSize();
					i1 += vertexElementFormat.getSize();
				}
			} else {
				i0 += vertexElement.getSize();
				i1 += vertexElement.getSize();
			}
		}
	}
	
	private float getVertexElement(ByteBuffer buffer, int index, VertexFormatElement.Format vertexElementFormat) {
		switch (vertexElementFormat) {
		case FLOAT:
			return buffer.getFloat(index);
		case UINT:
		case INT:
			return buffer.getInt(index);
		case USHORT:
		case SHORT:
			return buffer.getShort(index);
		case UBYTE:
		case BYTE:
			return buffer.get(index);
		default:
			throw new IllegalStateException("Invalid or missing format");
		}
	}

	private void setVertexElement(ByteBuffer buffer, int index, VertexFormatElement.Format vertexElementFormat, float value) {
		switch (vertexElementFormat) {
		case FLOAT:
			buffer.putFloat(index, value);
			break;
		case UINT:
		case INT:
			buffer.putInt(index, (int)value);
			break;
		case USHORT:
		case SHORT:
			buffer.putShort(index, (short)value);
			break;
		case UBYTE:
		case BYTE:
			buffer.put(index, (byte)value);
			break;
		default:
			throw new IllegalStateException("Invalid or missing format");
		}
	}
	
	public void pushClip(GSClipRect clipRect) {
		clipRectStack.push(clipRect.offset(clipOffsetX, clipOffsetY));
	}

	public GSClipRect popClip() {
		return clipRectStack.remove();
	}
	
	public void setClipOffset(float offsetX, float offsetY) {
		clipOffsetX = offsetX;
		clipOffsetY = offsetY;
	}
	
	public float getClipOffsetX() {
		return clipOffsetX;
	}

	public float getClipOffsetY() {
		return clipOffsetY;
	}
}
