package ergate.dict.imp;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public final class DataInput implements Cloneable, Closeable {

	private InputStream input;

	public DataInput(InputStream input) {
		if(input==null)
			throw new IllegalArgumentException("input stream must be not null!");
		this.input = input;
	}

	public byte readByte() throws IOException {
		int val=input.read();
		if(val<0)
			throw new IOException("has reached the stream end!");
		return (byte) val;
	}

	public void readBytes(byte[] b, int offset, int len) throws IOException {
		input.read(b, offset, len);
	}

	/**
	 * Reads two bytes and returns a short.
	 * 
	 * @see DataOutput#writeByte(byte)
	 */
	public short readShort() throws IOException {
		return (short) (((readByte() & 0xFF) << 8) | (readByte() & 0xFF));
	}

	/**
	 * Reads four bytes and returns an int.
	 * 
	 * @see DataOutput#writeInt(int)
	 */
	public int readInt() throws IOException {
		return ((readByte() & 0xFF) << 24) | ((readByte() & 0xFF) << 16)
				| ((readByte() & 0xFF) << 8) | (readByte() & 0xFF);
	}

	/**
	 * Reads an int stored in variable-length format. Reads between one and five
	 * bytes. Smaller values take fewer bytes. Negative numbers are not
	 * supported.
	 * <p>
	 * The format is described further in {@link DataOutput#writeVInt(int)}.
	 * 
	 * @see DataOutput#writeVInt(int)
	 */
	public int readVInt() throws IOException {
		/*
		 * This is the original code of this method, but a Hotspot bug (see
		 * LUCENE-2975) corrupts the for-loop if readByte() is inlined. So the
		 * loop was unwinded! byte b = readByte(); int i = b & 0x7F; for (int
		 * shift = 7; (b & 0x80) != 0; shift += 7) { b = readByte(); i |= (b &
		 * 0x7F) << shift; } return i;
		 */
		byte b = readByte();
		if (b >= 0)
			return b;
		int i = b & 0x7F;
		b = readByte();
		i |= (b & 0x7F) << 7;
		if (b >= 0)
			return i;
		b = readByte();
		i |= (b & 0x7F) << 14;
		if (b >= 0)
			return i;
		b = readByte();
		i |= (b & 0x7F) << 21;
		if (b >= 0)
			return i;
		b = readByte();
		// Warning: the next ands use 0x0F / 0xF0 - beware copy/paste errors:
		i |= (b & 0x0F) << 28;
		if ((b & 0xF0) == 0)
			return i;
		throw new IOException("Invalid vInt detected (too many bits)");
	}

	/**
	 * Reads eight bytes and returns a long.
	 * 
	 * @see DataOutput#writeLong(long)
	 */
	public long readLong() throws IOException {
		return (((long) readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
	}

	/**
	 * Reads a long stored in variable-length format. Reads between one and nine
	 * bytes. Smaller values take fewer bytes. Negative numbers are not
	 * supported.
	 * <p>
	 * The format is described further in {@link DataOutput#writeVInt(int)}.
	 * 
	 * @see DataOutput#writeVLong(long)
	 */
	public long readVLong() throws IOException {
		/*
		 * This is the original code of this method, but a Hotspot bug (see
		 * LUCENE-2975) corrupts the for-loop if readByte() is inlined. So the
		 * loop was unwinded! byte b = readByte(); long i = b & 0x7F; for (int
		 * shift = 7; (b & 0x80) != 0; shift += 7) { b = readByte(); i |= (b &
		 * 0x7FL) << shift; } return i;
		 */
		byte b = readByte();
		if (b >= 0)
			return b;
		long i = b & 0x7FL;
		b = readByte();
		i |= (b & 0x7FL) << 7;
		if (b >= 0)
			return i;
		b = readByte();
		i |= (b & 0x7FL) << 14;
		if (b >= 0)
			return i;
		b = readByte();
		i |= (b & 0x7FL) << 21;
		if (b >= 0)
			return i;
		b = readByte();
		i |= (b & 0x7FL) << 28;
		if (b >= 0)
			return i;
		b = readByte();
		i |= (b & 0x7FL) << 35;
		if (b >= 0)
			return i;
		b = readByte();
		i |= (b & 0x7FL) << 42;
		if (b >= 0)
			return i;
		b = readByte();
		i |= (b & 0x7FL) << 49;
		if (b >= 0)
			return i;
		b = readByte();
		i |= (b & 0x7FL) << 56;
		if (b >= 0)
			return i;
		throw new IOException(
				"Invalid vLong detected (negative values disallowed)");
	}

	/**
	 * Reads a string.
	 * 
	 * @see DataOutput#writeString(String)
	 */
	public String readString() throws IOException {
		int length = readVInt();
		final byte[] bytes = new byte[length];
		readBytes(bytes, 0, length);
		return new String(bytes, 0, length, "UTF-8");
	}

	/**
	 * Returns a clone of this stream.
	 * 
	 * <p>
	 * Clones of a stream access the same data, and are positioned at the same
	 * point as the stream they were cloned from.
	 * 
	 * <p>
	 * Expert: Subclasses must ensure that clones may be positioned at different
	 * points in the input from each other and from the stream they were cloned
	 * from.
	 */
	@Override
	public DataInput clone() {
		try {
			return (DataInput) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("This cannot happen: Failing to clone DataInput");
		}
	}

	@Override
	public void close() throws IOException {
		input.close();
	}
	
	public double readDouble() throws IOException{
		return Double.longBitsToDouble(readLong());
	}
}
