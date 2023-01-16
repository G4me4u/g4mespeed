package com.g4mesoft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.g4mesoft.core.GSCoreOverride;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GSFileUtil {

	public static final Object IGNORE = null;
	
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
		if (!file.isFile())
			throw new IOException("File does not exist, or is a directory.");

		E element;
		
		ByteBuf buffer = null;
		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] data = IOUtils.toByteArray(fis);
			buffer = Unpooled.wrappedBuffer(data);
			element = decoder.decode(GSDecodeBuffer.wrap(buffer));
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
		
		ByteBuf buffer = null;
		try (FileOutputStream fos = new FileOutputStream(file)) {
			buffer = Unpooled.buffer();
			encoder.encode(GSEncodeBuffer.wrap(buffer), element);
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
		public E decode(GSDecodeBuffer buf) throws Exception;
		
	}

	public static interface GSFileEncoder<E> {
		
		@GSCoreOverride
		public void encode(GSEncodeBuffer buf, E element) throws Exception;
		
	}
}
