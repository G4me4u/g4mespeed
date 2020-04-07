package com.g4mesoft.mixin.client;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.g4mesoft.access.GSIVertexFormatElementAccess;
import com.g4mesoft.gui.GSClipRect;
import com.g4mesoft.util.GSMathUtils;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;

@Mixin(BufferBuilder.class)
public class GSBufferBuilderMixin implements GSIBufferBuilderAccess {

	@Shadow private ByteBuffer buffer;
	@Shadow private int drawMode;
	@Shadow private VertexFormat format;
	@Shadow private boolean building;
	
	@Shadow private int buildStart;
	@Shadow private int vertexCount;
	@Shadow private int elementOffset;
	
	private float offsetX;
	private float offsetY;
	private float offsetZ;
	
	private boolean offsetVertices;
	private boolean positionFormat;
	
	private GSClipRect clipRect;
	
	private final float[] clipXBuffer = new float[4];
	private final float[] clipYBuffer = new float[4];

	@Inject(method = "begin", at = @At("RETURN"))
	public void begin(int drawMode, VertexFormat format, CallbackInfo ci) {
		positionFormat = hasPosition(format);
	}
	
	@Inject(method = "next", at = @At("HEAD"))
	public void onNextHead(CallbackInfo ci) {
		if (building && positionFormat && offsetVertices)
			offsetVertexPosition(vertexCount);
	}
	
	@Inject(method = "next", at = @At("RETURN"))
	public void onNextReturn(CallbackInfo ci) {
		if (building && positionFormat && (vertexCount & 0x3 /* % 4 */) == 0)
			clipPreviousShape();
	}

	private static boolean hasPosition(VertexFormat format) {
		ImmutableList<VertexFormatElement> elements = format.getElements();
		return (elements.size() > 0 && elements.get(0).getType() == VertexFormatElement.Type.POSITION);
	}
	
	private void offsetVertexPosition(int vertexIndex) {
		int index = buildStart + vertexIndex * format.getVertexSize();
		buffer.putFloat(index + 0, buffer.getFloat(index + 0) + offsetX);
		buffer.putFloat(index + 4, buffer.getFloat(index + 4) + offsetY);
		buffer.putFloat(index + 8, buffer.getFloat(index + 8) + offsetZ);
	}
	
	private void clipPreviousShape() {
		if (clipRect == null || drawMode != GL11.GL_QUADS)
			return;
		
		int vertexStart = vertexCount - 4;
		if (vertexStart < 0)
			return;
		
		int vertexSize = format.getVertexSize();
		int startIndex = buildStart + vertexStart * vertexSize;
		
		// Assume the quad is on the x-y plane where z = z0 for
		// all the vertices. Also assume that the sides of the
		// quad are all parallel with the x and y axis.
		clipXBuffer[0] = buffer.getFloat(startIndex + 0);
		clipYBuffer[0] = buffer.getFloat(startIndex + 4);

		float minimumSum = clipXBuffer[0] + clipYBuffer[0];
		int bli = 0;
		
		int index = startIndex + vertexSize;
		for (int i = 1; i < 4; i++, index += vertexSize) {
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
		
		// Check if quad is within clip bounds.
		if (x1 < clipRect.x0 || x0 >= clipRect.x1 ||
		    y1 < clipRect.y0 || y0 >= clipRect.y1) {
		
			vertexCount -= 4;
			elementOffset -= 4 * format.getVertexSize();
			
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
		
		bli = startIndex + bli * format.getVertexSize();
		bri = startIndex + bri * format.getVertexSize();
		tri = startIndex + tri * format.getVertexSize();
		tli = startIndex + tli * format.getVertexSize();
		
		float w = (x1 - x0);
		float t0 = (clipRect.x0 - x0) / w;
		float t1 = (clipRect.x1 - x0) / w;
		if (t0 > 0.0f || t1 < 1.0f) {
			interpolateClipped(bli, bri, t0, t1);
			interpolateClipped(tli, tri, t0, t1);
		}
		
		float h = (y1 - y0);
		t0 = (clipRect.y0 - y0) / h;
		t1 = (clipRect.y1 - y0) / h;
		if (t0 > 0.0f || t1 < 1.0f) {
			interpolateClipped(bli, tli, t0, t1);
			interpolateClipped(bri, tri, t0, t1);
		}
	}
	
	public void interpolateClipped(int i0, int i1, float t0, float t1) {
		for (VertexFormatElement vertexElement : format.getElements()) {
			if (vertexElement.getType() != VertexFormatElement.Type.PADDING) {
				VertexFormatElement.Format vertexElementFormat = vertexElement.getFormat();
				for (int i = 0; i < ((GSIVertexFormatElementAccess)vertexElement).getCount(); i++) {
					float v0 = getVertexElement(i0, vertexElementFormat);
					float v1 = getVertexElement(i1, vertexElementFormat);
	
					float dv = v1 - v0;
					if (t0 > 0.0f)
						setVertexElement(i0, vertexElementFormat, v0 + dv * t0);
					if (t1 < 1.0f)
						setVertexElement(i1, vertexElementFormat, v0 + dv * t1);
				
					i0 += vertexElementFormat.getSize();
					i1 += vertexElementFormat.getSize();
				}
			} else {
				i0 += vertexElement.getSize();
				i1 += vertexElement.getSize();
			}
		}
	}
	
	private float getVertexElement(int index, VertexFormatElement.Format vertexElementFormat) {
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

	private void setVertexElement(int index, VertexFormatElement.Format vertexElementFormat, float value) {
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

	@Override
	public void setClip(float x0, float y0, float x1, float y1) {
		if (building)
			throw new IllegalStateException("Buffer Builder is building.");

		if (clipRect == null) {
			clipRect = new GSClipRect(x0, y0, x1, y1);
		} else {
			clipRect.setClipBounds(x0, y0, x1, y1);
		}
	}

	@Override
	public void setClip(GSClipRect clip) {
		if (building)
			throw new IllegalStateException("Buffer Builder is building.");

		if (clip == null) {
			clipRect = null;
		} else if (clipRect == null) {
			clipRect = new GSClipRect(clip);
		} else {
			clipRect.setClipBounds(clip);
		}
	}

	@Override
	public GSClipRect getClip() {
		return (clipRect == null) ? null : new GSClipRect(clipRect);
	}

	@Override
	public void setOffset(float offsetX, float offsetY, float offsetZ) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		
		offsetVertices = !GSMathUtils.equalsApproximate(offsetX, 0.0f) ||
		                 !GSMathUtils.equalsApproximate(offsetY, 0.0f) ||
		                 !GSMathUtils.equalsApproximate(offsetZ, 0.0f);
	}
	
	@Override
	public float getOffsetX() {
		return offsetX;
	}

	@Override
	public float getOffsetY() {
		return offsetY;
	}

	@Override
	public float getOffsetZ() {
		return offsetZ;
	}
}
