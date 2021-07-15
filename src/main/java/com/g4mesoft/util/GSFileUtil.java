package com.g4mesoft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

public class GSFileUtil {

	public static void ensureDirectoryExists(File dir) throws IOException {
		if (dir != null && !dir.exists())
			dir.mkdirs();
	}
	
	public static void ensureFileExists(File file) throws IOException {
		if (file != null && !file.isFile()) {
			ensureDirectoryExists(file.getParentFile());
			file.createNewFile();
		}
	}
	
	public static <E> E readFile(File file, GSFileDecoder<E> decoder) throws IOException {
		E element;
		
		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] data = IOUtils.toByteArray(fis);
			PacketByteBuf buffer = new PacketByteBuf(Unpooled.wrappedBuffer(data));
			element = decoder.decode(buffer);
			buffer.release();
		} catch (Throwable throwable) {
			throw new IOException("Unable to read file", throwable);
		}
		
		return element;
	}
	
	public static <E> void writeFile(File file, E element, GSFileEncoder<E> encoder) throws IOException {
		GSFileUtil.ensureFileExists(file);
		
		try (FileOutputStream fos = new FileOutputStream(file)) {
			PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
			encoder.encode(buffer, element);
			if (buffer.hasArray()) {
				fos.write(buffer.array(), buffer.arrayOffset(), buffer.writerIndex());
			} else {
				fos.getChannel().write(buffer.nioBuffer());
			}
			buffer.release();
		} catch (Throwable throwable) {
			throw new IOException("Unable to write file", throwable);
		}
	}
	
	public static interface GSFileDecoder<E> {
		
		public E decode(PacketByteBuf buf) throws Exception;
		
	}

	public static interface GSFileEncoder<E> {
		
		public void encode(PacketByteBuf buf, E element) throws Exception;
		
	}
}
