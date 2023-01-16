package com.g4mesoft.util;

import static com.g4mesoft.util.GSEncodeBuffer.BIT_SHIFT_SECTION_X;
import static com.g4mesoft.util.GSEncodeBuffer.BIT_SHIFT_SECTION_Y;
import static com.g4mesoft.util.GSEncodeBuffer.BIT_SHIFT_SECTION_Z;
import static com.g4mesoft.util.GSEncodeBuffer.BIT_SHIFT_X;
import static com.g4mesoft.util.GSEncodeBuffer.BIT_SHIFT_Y;
import static com.g4mesoft.util.GSEncodeBuffer.BIT_SHIFT_Z;
import static com.g4mesoft.util.GSEncodeBuffer.BIT_SIZE_SECTION_X;
import static com.g4mesoft.util.GSEncodeBuffer.BIT_SIZE_SECTION_Y;
import static com.g4mesoft.util.GSEncodeBuffer.BIT_SIZE_SECTION_Z;
import static com.g4mesoft.util.GSEncodeBuffer.BIT_SIZE_X;
import static com.g4mesoft.util.GSEncodeBuffer.BIT_SIZE_Y;
import static com.g4mesoft.util.GSEncodeBuffer.BIT_SIZE_Z;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCounted;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

public class GSDecodeBuffer implements ReferenceCounted {

	private final ByteBuf buffer;
	
	private GSDecodeBuffer(ByteBuf buffer) {
		this.buffer = buffer;
	}
	
	public boolean readBoolean() {
		return buffer.readBoolean();
	}

	public byte readByte() {
		return buffer.readByte();
	}

	public void readBytes(byte[] dst) {
		readBytes(dst, dst.length);
	}

	public void readBytes(byte[] dst, int length) {
		readBytes(dst, 0, length);
	}

	public void readBytes(byte[] dst, int dstOffset, int length) {
		buffer.readBytes(dst, dstOffset, length);
	}
	
	public void readBytes(ByteBuf dst) {
		buffer.readBytes(dst);
	}

	public void readBytes(ByteBuffer dst) {
		buffer.readBytes(dst);
	}
	
	public void skipBytes(int length) {
		buffer.skipBytes(length);
	}

	public short readShort() {
		return buffer.readShort();
	}
	
	public int readMedium() {
		return buffer.readMedium();
	}
	
	public int readInt() {
		return buffer.readInt();
	}

	public long readLong() {
		return buffer.readLong();
	}
	
	public float readFloat() {
		return buffer.readFloat();
	}

	public double readDouble() {
		return buffer.readDouble();
	}

	public short readUnsignedByte() {
		return buffer.readUnsignedByte();
	}
	
	public int readUnsignedShort() {
		return buffer.readUnsignedShort();
	}
	
	public int readUnsignedMedium() {
		return buffer.readUnsignedMedium();
	}
	
	public long readUnsignedInt() {
		return buffer.readUnsignedInt();
	}

	private int readVarInt() {
		byte data;
		int value = 0;
		int count = 0;
		do {
			if (count >= 5)
				throw new DecoderException("VarInt too big");
			data = readByte();
			value |= (data & 0x7F) << (7 * count);
			count++;
		} while ((data & 0x80) != 0);
		return value;
	}

	public byte[] readByteArray() {
		return readByteArray(null);
	}
	
	public byte[] readByteArray(byte[] dst) {
		int length = readVarInt();
		if (dst == null || length != dst.length)
			dst = new byte[length];
		readBytes(dst);
		return dst;
	}

	public short[] readShortArray() {
		return readShortArray(null);
	}
	
	public short[] readShortArray(short[] dst) {
		int length = readVarInt();
		if (dst == null || length != dst.length)
			dst = new short[length];
		for (int i = 0; i < length; i++)
			dst[i] = readShort();
		return dst;
	}

	public int[] readIntArray() {
		return readIntArray(null);
	}
	
	public int[] readIntArray(int[] dst) {
		int length = readVarInt();
		if (dst == null || length != dst.length)
			dst = new int[length];
		for (int i = 0; i < length; i++)
			dst[i] = readInt();
		return dst;
	}

	public long[] readLongArray() {
		return readLongArray(null);
	}
	
	public long[] readLongArray(long[] dst) {
		int length = readVarInt();
		if (dst == null || length != dst.length)
			dst = new long[length];
		for (int i = 0; i < length; i++)
			dst[i] = readLong();
		return dst;
	}

	public String readString() {
		return readString((int)Short.MAX_VALUE);
	}

	public String readString(int lengthLimit) {
		return readString(CharsetUtil.UTF_8, lengthLimit);
	}

	public String readString(Charset charset) {
		return readString(charset, (int)Short.MAX_VALUE);
	}

	public String readString(Charset charset, int encodedLimit) {
		int length = readVarInt();
		if (length > encodedLimit)
			throw new DecoderException("encoded length exceeds limit");
		String value = buffer.toString(getLocation(), length, charset);
		setLocation(getLocation() + length);
		return value;
	}
	
	public Identifier readIdentifier() {
		return new Identifier(readString());
	}

	public UUID readUUID() {
		long mostSigBits = readLong();
		long leastSigBits = readLong();
		return new UUID(mostSigBits, leastSigBits);
	}
	
	public BlockPos readBlockPos() {
		long value = readLong();
		int x = (int)((value << (64 - BIT_SHIFT_X - BIT_SIZE_X)) >> (64 - BIT_SIZE_X));
		int y = (int)((value << (64 - BIT_SHIFT_Y - BIT_SIZE_Y)) >> (64 - BIT_SIZE_Y));
		int z = (int)((value << (64 - BIT_SHIFT_Z - BIT_SIZE_Z)) >> (64 - BIT_SIZE_Z));
		return new BlockPos(x, y, z);
	}
	
	public ChunkPos readChunkPos() {
		int z = readInt();
		int x = readInt();
		return new ChunkPos(x, z);
	}
	
	public ChunkSectionPos readChunkSectionPos() {
		long value = readLong();
		int x = (int)((value << (64 - BIT_SHIFT_SECTION_X - BIT_SIZE_SECTION_X)) >> (64 - BIT_SIZE_SECTION_X));
		int y = (int)((value << (64 - BIT_SHIFT_SECTION_Y - BIT_SIZE_SECTION_Y)) >> (64 - BIT_SIZE_SECTION_Y));
		int z = (int)((value << (64 - BIT_SHIFT_SECTION_Z - BIT_SIZE_SECTION_Z)) >> (64 - BIT_SIZE_SECTION_Z));
		return ChunkSectionPos.from(x, y, z);
	}
	
	public boolean getBoolean(int location) {
		return buffer.getBoolean(location);
	}

	public byte getByte(int location) {
		return buffer.getByte(location);
	}
	
	public void getBytes(int location, byte[] dst) {
		getBytes(location, dst, dst.length);
	}

	public void getBytes(int location, byte[] dst, int length) {
		getBytes(location, dst, 0, length);
	}

	public void getBytes(int location, byte[] dst, int dstOffset, int length) {
		buffer.getBytes(location, dst, dstOffset, length);
	}
	
	public short getShort(int location) {
		return buffer.getShort(location);
	}

	public int getMedium(int location) {
		return buffer.getMedium(location);
	}

	public int getInt(int location) {
		return buffer.getInt(location);
	}

	public long getLong(int location) {
		return buffer.getLong(location);
	}
	
	public float getFloat(int location) {
		return buffer.getFloat(location);
	}

	public double getDouble(int location) {
		return buffer.getDouble(location);
	}
	
	public short getUnsignedByte(int location) {
		return buffer.getUnsignedByte(location);
	}
	
	public int getUnsignedShort(int location) {
		return buffer.getUnsignedShort(location);
	}
	
	public int getUnsignedMedium(int location) {
		return buffer.getUnsignedMedium(location);
	}
	
	public long getUnsignedInt(int location) {
		return buffer.getUnsignedInt(location);
	}
	
	public int readableBytes() {
		return buffer.readableBytes();
	}

	public boolean isReadable() {
		return buffer.isReadable();
	}

	public boolean isReadable(int size) {
		return buffer.isReadable(size);
	}
	
	public int getLocation() {
		return buffer.readerIndex();
	}

	public void setLocation(int location) {
		buffer.readerIndex(location);
	}
	
	public void markLocation() {
		buffer.markReaderIndex();
	}

	public void resetLocation() {
		buffer.resetReaderIndex();
	}
	
	@Override
	public int refCnt() {
		return buffer.refCnt();
	}

	@Override
	public GSDecodeBuffer retain() {
		return new GSDecodeBuffer(buffer.retain());
	}

	@Override
	public GSDecodeBuffer retain(int increment) {
		return new GSDecodeBuffer(buffer.retain(increment));
	}

	@Override
	public GSDecodeBuffer touch() {
		buffer.touch();
		return this;
	}

	@Override
	public GSDecodeBuffer touch(Object hint) {
		buffer.touch(hint);
		return this;
	}

	@Override
	public boolean release() {
		return buffer.release();
	}

	@Override
	public boolean release(int decrement) {
		return buffer.release(decrement);
	}
	
	public ByteBuf getBuffer() {
		return buffer;
	}

	public static GSDecodeBuffer wrap(ByteBuf buffer) {
		return new GSDecodeBuffer(buffer);
	}
}