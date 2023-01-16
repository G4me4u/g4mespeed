package com.g4mesoft.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCounted;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

public class GSEncodeBuffer implements ReferenceCounted {

	/* visible for GSDecodeBuffer */
	static final int BIT_SIZE_X = 26; /* largest signed power of two above 3M */
	static final int BIT_SIZE_Z = BIT_SIZE_X;
	static final int BIT_SIZE_Y = 64 - BIT_SIZE_X - BIT_SIZE_Z;
	static final int BIT_SHIFT_Y = 0;
	static final int BIT_SHIFT_Z = BIT_SHIFT_Y + BIT_SIZE_Y;
	static final int BIT_SHIFT_X = BIT_SHIFT_Z + BIT_SIZE_Z;
	private static final long BIT_MASK_X = (1L << BIT_SIZE_X) - 1L;
	private static final long BIT_MASK_Y = (1L << BIT_SIZE_Y) - 1L;
	private static final long BIT_MASK_Z = (1L << BIT_SIZE_Z) - 1L;
	
	/* visible for GSDecodeBuffer */
	static final int BIT_SIZE_SECTION_X = BIT_SIZE_X - 4; /* x & z are shifted by 4 */
	static final int BIT_SIZE_SECTION_Z = BIT_SIZE_Z - 4;
	static final int BIT_SIZE_SECTION_Y = 64 - BIT_SIZE_SECTION_X - BIT_SIZE_SECTION_Z;
	static final int BIT_SHIFT_SECTION_Y = 0;
	static final int BIT_SHIFT_SECTION_Z = BIT_SHIFT_SECTION_Y + BIT_SIZE_SECTION_Y;
	static final int BIT_SHIFT_SECTION_X = BIT_SHIFT_SECTION_Z + BIT_SIZE_SECTION_Z;
	private static final long BIT_MASK_SECTION_X = (1L << BIT_SIZE_SECTION_X) - 1L;
	private static final long BIT_MASK_SECTION_Y = (1L << BIT_SIZE_SECTION_Y) - 1L;
	private static final long BIT_MASK_SECTION_Z = (1L << BIT_SIZE_SECTION_Z) - 1L;
	
	private final ByteBuf buffer;
	
	private GSEncodeBuffer(ByteBuf buffer) {
		this.buffer = buffer;
	}
	
	public void writeBoolean(boolean value) {
		buffer.writeBoolean(value);
	}

	public void writeByte(byte value) {
		buffer.writeByte(value);
	}
	
	public void writeBytes(byte[] src) {
		writeBytes(src, src.length);
	}

	public void writeBytes(byte[] src, int length) {
		writeBytes(src, 0, length);
	}

	public void writeBytes(byte[] src, int srcOffset, int length) {
		buffer.writeBytes(src, srcOffset, length);
	}
	
	public void writeBytes(ByteBuf src) {
		buffer.writeBytes(src);
	}

	public void writeBytes(ByteBuffer src) {
		buffer.writeBytes(src);
	}
	
	public void writeZero(int length) {
		buffer.writeZero(length);
	}

	public void writeShort(short value) {
		buffer.writeShort(value);
	}
	
	public void writeMedium(int value) {
		buffer.writeMedium(value);
	}
	
	public void writeInt(int value) {
		buffer.writeInt(value);
	}

	public void writeLong(long value) {
		buffer.writeLong(value);
	}
	
	public void writeFloat(float value) {
		buffer.writeFloat(value);
	}

	public void writeDouble(double value) {
		buffer.writeDouble(value);
	}

	public void writeUnsignedByte(short value) {
		buffer.writeByte(value);
	}
	
	public void writeUnsignedShort(int value) {
		buffer.writeShort(value);
	}
	
	public void writeUnsignedMedium(int value) {
		buffer.writeMedium(value);
	}
	
	public void writeUnsignedInt(long value) {
		buffer.writeInt((int)value);
	}

	private void writeVarInt(int value) {
		while (true) {
			if ((value & 0xFFFFFF80) == 0) {
				writeByte((byte)value);
				return;
			}
			writeByte((byte)((value & 0x7F) | 0x80));
			value >>>= 7;
		}
	}
	
	public void writeByteArray(byte[] arr) {
		writeVarInt(arr.length);
		writeBytes(arr);
	}

	public void writeShortArray(short[] arr) {
		writeVarInt(arr.length);
		for (int i = 0; i < arr.length; i++)
			writeShort(arr[i]);
	}

	public void writeIntArray(int[] arr) {
		writeVarInt(arr.length);
		for (int i = 0; i < arr.length; i++)
			writeInt(arr[i]);
	}

	public void writeLongArray(long[] arr) {
		writeVarInt(arr.length);
		for (int i = 0; i < arr.length; i++)
			writeLong(arr[i]);
	}

	public void writeString(String str) {
		writeString(str, (int)Short.MAX_VALUE);
	}

	public void writeString(String str, int lengthLimit) {
		writeString(str, CharsetUtil.UTF_8, lengthLimit);
	}

	public void writeString(String str, Charset charset) {
		writeString(str, charset, (int)Short.MAX_VALUE);
	}

	public void writeString(String str, Charset charset, int encodedLimit) {
        byte[] buf = str.getBytes(charset);
        if (buf.length > encodedLimit)
        	throw new EncoderException("encoded length exceeds limit");
        writeVarInt(buf.length);
        writeBytes(buf);
	}
	
	public void writeIdentifier(Identifier value) {
		writeString(value.toString());
	}

	public void writeUUID(UUID value) {
		writeLong(value.getMostSignificantBits());
		writeLong(value.getLeastSignificantBits());
	}
	
	public void writeBlockPos(BlockPos value) {
		writeBlockPos(value.getX(), value.getY(), value.getZ());
	}
	
	private void writeBlockPos(int x, int y, int z) {
		// The encoding of a block position is as follows:
		//   - y stored in bits <0:11>
		//   - z stored in bits <12:37>
		//   - x stored in bits <38:63>
		long value = 0L;
		value |= ((long)x & BIT_MASK_X) << BIT_SHIFT_X;
		value |= ((long)y & BIT_MASK_Y) << BIT_SHIFT_Y;
		value |= ((long)z & BIT_MASK_Z) << BIT_SHIFT_Z;
		writeLong(value);
	}

	public void writeChunkPos(ChunkPos value) {
		// In order to store a chunk pos we need 4 bits less
		// than when storing block x- & z- coordinates. This
		// is 22 bits each, which is still >32. So might as
		// well just store the coordinates individually.
		//
		// Note: stored to match vanilla long-encoding endianess.
		writeInt(value.z);
		writeInt(value.x);
	}
	
	public void writeChunkSectionPos(ChunkSectionPos value) {
		writeChunkSectionPos(value.getSectionX(), value.getSectionY(), value.getSectionZ());
	}
	
	private void writeChunkSectionPos(int x, int y, int z) {
		// The encoding of a chunk section position is as follows:
		//   - section y stored in bits <0:19>
		//   - section z stored in bits <20:41>
		//   - section x stored in bits <42:63>
        long value = 0L;
        value |= ((long)x & BIT_MASK_SECTION_X) << BIT_SHIFT_SECTION_X;
        value |= ((long)y & BIT_MASK_SECTION_Y) << BIT_SHIFT_SECTION_Y;
        value |= ((long)z & BIT_MASK_SECTION_Z) << BIT_SHIFT_SECTION_Z;
        writeLong(value);
	}
	
	public void setBoolean(int location, boolean value) {
		buffer.setBoolean(location, value);
	}

	public void setByte(int location, byte value) {
		buffer.setByte(location, value);
	}
	
	public void setBytes(int location, byte[] src) {
		setBytes(location, src, src.length);
	}

	public void setBytes(int location, byte[] src, int length) {
		setBytes(location, src, 0, length);
	}

	public void setBytes(int location, byte[] src, int srcOffset, int length) {
		buffer.setBytes(location, src, srcOffset, length);
	}
	
	public void setShort(int location, short value) {
		buffer.setShort(location, value);
	}

	public void setMedium(int location, int value) {
		buffer.setMedium(location, value);
	}

	public void setInt(int location, int value) {
		buffer.setInt(location, value);
	}

	public void setLong(int location, long value) {
		buffer.setLong(location, value);
	}
	
	public void setFloat(int location, float value) {
		buffer.setFloat(location, value);
	}

	public void setDouble(int location, double value) {
		buffer.setDouble(location, value);
	}
	
	public void setUnsignedByte(int location, short value) {
		buffer.setByte(location, value);
	}
	
	public void setUnsignedShort(int location, int value) {
		buffer.setShort(location, value);
	}
	
	public void setUnsignedMedium(int location, int value) {
		buffer.setMedium(location, value);
	}
	
	public void setUnsignedInt(int location, long value) {
		buffer.setInt(location, (int)value);
	}
	
	public int writableBytes() {
		return buffer.writableBytes();
	}

	public boolean isWritable() {
		return buffer.isWritable();
	}

	public boolean isWritable(int size) {
		return buffer.isWritable(size);
	}
	
	public int getLocation() {
		return buffer.writerIndex();
	}

	public void setLocation(int location) {
		buffer.writerIndex(location);
	}
	
	public void markLocation() {
		buffer.markWriterIndex();
	}

	public void resetLocation() {
		buffer.resetWriterIndex();
	}

	@Override
	public int refCnt() {
		return buffer.refCnt();
	}

	@Override
	public GSEncodeBuffer retain() {
		return new GSEncodeBuffer(buffer.retain());
	}

	@Override
	public GSEncodeBuffer retain(int increment) {
		return new GSEncodeBuffer(buffer.retain(increment));
	}

	@Override
	public GSEncodeBuffer touch() {
		buffer.touch();
		return this;
	}

	@Override
	public GSEncodeBuffer touch(Object hint) {
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
	
	public static GSEncodeBuffer wrap(ByteBuf buffer) {
		return new GSEncodeBuffer(buffer);
	}
}