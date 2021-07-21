package com.g4mesoft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.g4mesoft.core.GSCoreOverride;

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
		
		PacketByteBuf buffer = null;
		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] data = IOUtils.toByteArray(fis);
			buffer = new PacketByteBuf(Unpooled.wrappedBuffer(data));
			element = decoder.decode(buffer);
		} catch (Throwable throwable) {
			throw new IOException("Unable to read file", throwable);
		} finally {
			if (buffer != null)
				buffer.release();
		}
		
		return element;
	}
	
	public static <E> void writeFile(File file, E element, GSFileEncoder<E> encoder) throws IOException {
		GSFileUtil.ensureFileExists(file);
		
		PacketByteBuf buffer = null;
		try (FileOutputStream fos = new FileOutputStream(file)) {
			buffer = new PacketByteBuf(Unpooled.buffer());
			encoder.encode(buffer, element);
			if (buffer.hasArray()) {
				fos.write(buffer.array(), buffer.arrayOffset(), buffer.writerIndex());
			} else {
				fos.getChannel().write(buffer.nioBuffer());
			}
		} catch (Throwable throwable) {
			throw new IOException("Unable to write file", throwable);
		} finally {
			if (buffer != null)
				buffer.release();
		}
	}
	
	public static interface GSFileDecoder<E> {
		
		@GSCoreOverride
		public E decode(PacketByteBuf buf) throws Exception;
		
	}

	public static interface GSFileEncoder<E> {
		
		@GSCoreOverride
		public void encode(PacketByteBuf buf, E element) throws Exception;
		
	}
}
