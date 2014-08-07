/**
 * Copyright 2014 Ricardo Padilha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dsys.commons.impl.lang;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Formatter;

import net.dsys.commons.api.exception.Bug;

import sun.misc.Unsafe;

/**
 * Wrapper for sun.misc.Unsafe for faster byte[] manipulation.
 * 
 * @author Ricardo Padilha
 */
@SuppressWarnings("restriction")
public final class FastArrays {

	private static final Unsafe UNSAFE = getUnsafeOrNull();
	private static final boolean FAST = UNSAFE != null;

	private static final int BYTE_LENGTH = Byte.SIZE / Byte.SIZE;
	private static final int BOOLEAN_LENGTH = BYTE_LENGTH;
	private static final int SHORT_LENGTH = Short.SIZE / Byte.SIZE;
	private static final int INT_LENGTH = Integer.SIZE / Byte.SIZE;
	private static final int LONG_LENGTH = Long.SIZE / Byte.SIZE;

	private static final int BYTE_MASK = 0xFF;
	private static final int SHORT_MASK = 0xFFFF;
	private static final long INT_MASK = 0xFFFF_FFFFL;
	private static final byte BOOLEAN_TRUE = 1;
	private static final byte BOOLEAN_FALSE = 0;

	/**
	 * This boolean indicates whether or not data read with the Unsafe needs to
	 * be reversed byte-wise, because the VM is not big endian.
	 */
	private static final boolean REVERSE = ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN;
	private static final long BYTE_ARRAY_OFFSET = Unsafe.ARRAY_BYTE_BASE_OFFSET;
	//private static final long SHORT_ARRAY_OFFSET = Unsafe.ARRAY_SHORT_BASE_OFFSET;
	//private static final long INT_ARRAY_OFFSET = Unsafe.ARRAY_INT_BASE_OFFSET;
	private static final long LONG_ARRAY_OFFSET = Unsafe.ARRAY_LONG_BASE_OFFSET;
	private static final long BB_ADDRESS_OFFSET = fieldOffsetOrError(UNSAFE, Buffer.class, "address");

	private FastArrays() {
		// no instantiation allowed
		return;
	}

	private static Unsafe getUnsafe() throws PrivilegedActionException {
		return AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>() {
			@Override public Unsafe run() throws Exception {
				final Field f = Unsafe.class.getDeclaredField("theUnsafe");
				f.setAccessible(true);
				return (Unsafe) f.get(null);
			}
		});
	}

	private static Unsafe getUnsafeOrNull() {
		try {
			return getUnsafe();
		} catch (final PrivilegedActionException e) {
			return null;
		}
	}

	private static long fieldOffset(final Unsafe unsafe, final Class<?> cl, final String field)
			throws NoSuchFieldException {
		final Field f = cl.getDeclaredField(field);
		return unsafe.objectFieldOffset(f);
	}

	private static long fieldOffsetOrError(final Unsafe unsafe, final Class<?> cl, final String field) {
		try {
			return fieldOffset(unsafe, cl, field);
		} catch (final NoSuchFieldException | SecurityException e) {
			throw new Bug(e);
		}
	}

	/**
	 * Get a byte from an array.
	 * Uses fast methods if available.
	 */
	public static int getUnsignedByte(final byte[] array, final int offset) {
		return array[offset] & BYTE_MASK;
	}

	/**
	 * Put a byte in an array.
	 * Uses fast methods if available.
	 */
	public static int putUnsignedByte(final byte[] array, final int offset, final int value) {
		if (value > BYTE_MASK || value < 0) {
			throw new IllegalArgumentException();
		}
		array[offset] = (byte) value;
		return BYTE_LENGTH;
	}

	/**
	 * Get a short from an array.
	 * Uses fast methods if available.
	 */
	public static int getUnsignedShort(final byte[] array, final int offset) {
		return getShort(array, offset) & SHORT_MASK;
	}

	/**
	 * Put a short in an array.
	 * Uses fast methods if available.
	 */
	public static int putUnsignedShort(final byte[] array, final int offset, final int value) {
		if (value > SHORT_MASK || value < 0) {
			throw new IllegalArgumentException();
		}
		putShort(array, offset, (short) value);
		return SHORT_LENGTH;
	}

	/**
	 * Get an int from an array.
	 * Uses fast methods if available.
	 */
	public static long getUnsignedInt(final byte[] array, final int offset) {
		return getInt(array, offset) & INT_MASK;
	}

	/**
	 * Put an int in an array.
	 * Uses fast methods if available.
	 */
	public static int putUnsignedInt(final byte[] array, final int offset, final long value) {
		if (value > INT_MASK || value < 0) {
			throw new IllegalArgumentException();
		}
		putInt(array, offset, (int) value);
		return INT_LENGTH;
	}

	/**
	 * Get a boolean from an array.
	 */
	public static boolean getBoolean(final byte[] array, final int offset) {
		return array[offset] != 0;
	}

	/**
	 * Put a boolean in an array.
	 */
	public static int putBoolean(final byte[] array, final int offset, final boolean value) {
		if (value) {
			array[offset] = BOOLEAN_TRUE;
		} else {
			array[offset] = BOOLEAN_FALSE;
		}
		return BOOLEAN_LENGTH;
	}

	/**
	 * Get a short from an array.
	 * Uses fast methods if available.
	 */
	public static short getShort(final byte[] array, final int offset) {
		if (FAST) {
			if (REVERSE) {
				return Short.reverseBytes(UNSAFE.getShort(array, BYTE_ARRAY_OFFSET + offset));
			}
			return UNSAFE.getShort(array, BYTE_ARRAY_OFFSET + offset);
		}
		// fall-back to plain Java
		return (short) (((array[offset]  & BYTE_MASK) << 8)
					+ (array[offset + 1] & BYTE_MASK));
	}

	/**
	 * Put a short in an array.
	 * Uses fast methods if available.
	 */
	public static int putShort(final byte[] array, final int offset, final short value) {
		if (FAST) {
			if (REVERSE) {
				UNSAFE.putShort(array, BYTE_ARRAY_OFFSET + offset, Short.reverseBytes(value));
				return SHORT_LENGTH;
			}
			UNSAFE.putShort(array, BYTE_ARRAY_OFFSET + offset, value);
			return SHORT_LENGTH;
		}
		// fall-back to plain Java
		array[offset]     = (byte) (value >>> 8);
		array[offset + 1] = (byte) (value);
		return SHORT_LENGTH;
	}

	/**
	 * Get an int from an array.
	 * Uses fast methods if available.
	 */
	public static int getInt(final byte[] array, final int offset) {
		if (FAST) {
			if (REVERSE) {
				return Integer.reverseBytes(UNSAFE.getInt(array, BYTE_ARRAY_OFFSET + offset));
			}
			return UNSAFE.getInt(array, BYTE_ARRAY_OFFSET + offset);
		}
		// fall-back to plain Java
		return (array[offset]                  << 24)
			+ ((array[offset + 1] & BYTE_MASK) << 16)
			+ ((array[offset + 2] & BYTE_MASK) << 8)
			+ (array[offset  + 3] & BYTE_MASK);
	}

	/**
	 * Put an int in an array.
	 * Uses fast methods if available.
	 */
	public static int putInt(final byte[] array, final int offset, final int value) {
		if (FAST) {
			if (REVERSE) {
				UNSAFE.putInt(array, BYTE_ARRAY_OFFSET + offset, Integer.reverseBytes(value));
				return INT_LENGTH;
			}
			UNSAFE.putInt(array, BYTE_ARRAY_OFFSET + offset, value);
			return INT_LENGTH;
		}
		// fall-back to plain Java
		array[offset]     = (byte) (value >>> 24);
		array[offset + 1] = (byte) (value >>> 16);
		array[offset + 2] = (byte) (value >>> 8);
		array[offset + 3] = (byte) (value);
		return INT_LENGTH;
	}

	/**
	 * Get a long from an array.
	 * Uses fast methods if available.
	 */
	public static long getLong(final byte[] array, final int offset) {
		if (FAST) {
			if (REVERSE) {
				return Long.reverseBytes(UNSAFE.getLong(array, BYTE_ARRAY_OFFSET + offset));
			}
			return UNSAFE.getLong(array, BYTE_ARRAY_OFFSET + offset);
		}
		// fall-back to plain Java
		return ((long) (array[offset])                << 56)
			+ ((long) (array[offset + 1] & BYTE_MASK) << 48)
			+ ((long) (array[offset + 2] & BYTE_MASK) << 40)
			+ ((long) (array[offset + 3] & BYTE_MASK) << 32)
			+ ((array[offset + 4]        & BYTE_MASK) << 24)
			+ ((array[offset + 5]        & BYTE_MASK) << 16)
			+ ((array[offset + 6]        & BYTE_MASK) <<  8)
			+ (array[offset + 7]         & BYTE_MASK);
	}

	/**
	 * Put a long in an array.
	 * Uses fast methods if available.
	 */
	public static int putLong(final byte[] array, final int offset, final long value) {
		if (FAST) {
			if (REVERSE) {
				UNSAFE.putLong(array, BYTE_ARRAY_OFFSET + offset, Long.reverseBytes(value));
				return LONG_LENGTH;
			}
			UNSAFE.putLong(array, BYTE_ARRAY_OFFSET + offset, value);
			return LONG_LENGTH;
		}
		// fall-back to plain Java
		array[offset]     = (byte) (value >>> 56);
		array[offset + 1] = (byte) (value >>> 48);
		array[offset + 2] = (byte) (value >>> 40);
		array[offset + 3] = (byte) (value >>> 32);
		array[offset + 4] = (byte) (value >>> 24);
		array[offset + 5] = (byte) (value >>> 16);
		array[offset + 6] = (byte) (value >>>  8);
		array[offset + 7] = (byte) (value);
		return LONG_LENGTH;
	}

	/**
	 * Commodity method to wrap a short in a byte array.
	 */
	public static byte[] toArray(final short value) {
		final byte[] array = new byte[SHORT_LENGTH];
		putShort(array, 0, value);
		return array;
	}

	/**
	 * Commodity method to wrap an int in a byte array.
	 */
	public static byte[] toArray(final int value) {
		final byte[] array = new byte[INT_LENGTH];
		putInt(array, 0, value);
		return array;
	}

	/**
	 * Commodity method to wrap a long in a byte array.
	 */
	public static byte[] toArray(final long value) {
		final byte[] array = new byte[LONG_LENGTH];
		putLong(array, 0, value);
		return array;
	}

	public static int fill(final byte[] array, final int offset, final int length, final byte value) {
		if (FAST) {
			UNSAFE.setMemory(array, BYTE_ARRAY_OFFSET + offset, length, value);
			return length;
		}
		// fall-back to plain Java
		Arrays.fill(array, offset, offset + length, value);
		return length;
	}

	/**
	 * @see System#arraycopy(Object, int, Object, int, int)
	 */
	public static void arrayCopy(final byte[] src, final int srcPos, final byte[] dst, final int dstPos,
			final int length) {
		if (FAST) {
			if (srcPos < 0 || dstPos < 0 || length < 0 || (srcPos + length) > src.length
					|| (dstPos + length) > dst.length) {
				throw new IndexOutOfBoundsException();
			}
			UNSAFE.copyMemory(src, BYTE_ARRAY_OFFSET + srcPos, dst, BYTE_ARRAY_OFFSET + dstPos, length);
			return;
		}
		// fall-back to plain Java
		System.arraycopy(src, srcPos, dst, dstPos, length);
	}

	/**
	 * This method copies and casts between arrays.
	 * @param length the number of bytes to be copied. Must be a multiple {@link #LONG_LENGTH}.
	 * @see System#arraycopy(Object, int, Object, int, int)
	 */
	public static void arrayCopy(final byte[] src, final int srcPos, final long[] dst, final int dstPos,
			final int length) {
		if (FAST) {
			if (srcPos < 0 || dstPos < 0 || length < 0) {
				throw new IllegalArgumentException();
			}
			if ((srcPos + length) > src.length || (dstPos + length) > dst.length * LONG_LENGTH) {
				throw new ArrayIndexOutOfBoundsException();
			}
			if (length % LONG_LENGTH != 0) {
				throw new IllegalArgumentException();
			}
			UNSAFE.copyMemory(src, BYTE_ARRAY_OFFSET + srcPos, dst, LONG_ARRAY_OFFSET + dstPos, length);
			return;
		}
		// fall-back to plain Java
		final LongBuffer srcbuf = ByteBuffer.wrap(src, srcPos, length).asLongBuffer();
		final LongBuffer dstbuf = LongBuffer.wrap(dst, dstPos, length / LONG_LENGTH);
		dstbuf.put(srcbuf);
	}

	/**
	 * This method copies and casts between arrays.
	 * @param length number of longs to be copied.
	 * @see System#arraycopy(Object, int, Object, int, int)
	 */
	public static void arrayCopy(final long[] src, final int srcPos, final byte[] dst, final int dstPos,
			final int length) {
		if (FAST) {
			if (srcPos < 0 || dstPos < 0 || length < 0) {
				throw new IllegalArgumentException();
			}
			if ((srcPos + length) > src.length || (dstPos + length * LONG_LENGTH) > dst.length) {
				throw new ArrayIndexOutOfBoundsException();
			}
			final long len = ((long) length) * LONG_LENGTH;
			UNSAFE.copyMemory(src, LONG_ARRAY_OFFSET + srcPos, dst, BYTE_ARRAY_OFFSET + dstPos, len);
			return;
		}
		// fall-back to plain Java
		final LongBuffer srcbuf = LongBuffer.wrap(src, srcPos, length);
		final LongBuffer dstbuf = ByteBuffer.wrap(dst, dstPos, length * LONG_LENGTH).asLongBuffer();
		dstbuf.put(srcbuf);
	}

	/**
	 * @see System#arraycopy(Object, int, Object, int, int)
	 * @see ByteBuffer#get(byte[], int, int)
	 */
	public static void arrayCopy(final ByteBuffer src, final byte[] dst, final int dstPos, final int length) {
		if (FAST) {
			if (src.isDirect()) {
				final int srcPos = src.position();
				if (dstPos < 0 || length < 0) {
					throw new IllegalArgumentException();
				}
				if ((srcPos + length) > src.limit()) {
					throw new BufferUnderflowException();
				}
				if ((dstPos + length) > dst.length) {
					throw new ArrayIndexOutOfBoundsException(dstPos + length);
				}
				final long address = UNSAFE.getLong(src, BB_ADDRESS_OFFSET);
				UNSAFE.copyMemory(null, address + srcPos, dst, BYTE_ARRAY_OFFSET + dstPos, length);
				src.position(srcPos + length);
				return;
			} else if (src.hasArray()) {
				arrayCopy(src.array(), src.position(), dst, dstPos, length);
				return;
			}
			throw new IllegalArgumentException();
		}
		// fall-back to plain Java
		src.get(dst, dstPos, length);
	}

	/**
	 * @see System#arraycopy(Object, int, Object, int, int)
	 * @see ByteBuffer#get(byte[], int, int)
	 */
	public static void arrayCopy(final byte[] src, final int srcPos, final ByteBuffer dst, final int length) {
		if (FAST) {
			if (dst.isDirect()) {
				final int dstPos = dst.position();
				if (srcPos < 0 || length < 0) {
					throw new IllegalArgumentException();
				}
				if ((srcPos + length) > src.length) {
					throw new ArrayIndexOutOfBoundsException(srcPos + length);
				}
				if ((dstPos + length) > dst.remaining()) {
					throw new BufferOverflowException();
				}
				final long address = UNSAFE.getLong(src, BB_ADDRESS_OFFSET);
				UNSAFE.copyMemory(src, address + srcPos, dst, BYTE_ARRAY_OFFSET + dstPos, length);
				dst.position(dstPos + length);
				return;
			} else if (dst.hasArray()) {
				arrayCopy(src, srcPos, dst.array(), dst.position(), length);
				return;
			}
			throw new IllegalArgumentException();
		}
		// fall-back to plain Java
		dst.put(src, srcPos, length);
	}

	/**
	 * Lexicographical (unsigned) comparison.
	 * Same as <code>compareArrays(left, 0, left.length, right, 0, right.length)</code>.
	 */
	public static int compareArrays(final byte[] left, final byte[] right) {
		return compareArrays(left, 0, left.length, right, 0, right.length);
	}

	/**
	 * Lexicographical (unsigned) comparison.
	 */
	public static int compareArrays(final byte[] left, final int leftOffset, final int leftLength,
			final byte[] right, final int rightOffset, final int rightLength) {
		if (leftOffset < 0 || leftLength < 0 || rightOffset < 0 || rightLength < 0) {
			throw new IllegalArgumentException();
		}
		if (left == right && leftOffset == rightOffset && leftLength == rightLength) {
			return 0; // null == null
		} else if (left == null) {
			return -1;
		} else if (right == null) {
			return 1;
		}
		if (leftOffset + leftLength > left.length || rightOffset + rightLength > right.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		final int minLength = Math.min(leftLength, rightLength);
		int pos = 0;

		// first round with longs
		if (minLength >= LONG_LENGTH) {
			final int longs = minLength / LONG_LENGTH;
			for (final int end = longs * LONG_LENGTH; pos < end; pos += LONG_LENGTH) {
				final long lw = getLong(left, leftOffset + pos);
				final long rw = getLong(right, rightOffset + pos);
				if (lw != rw) {
					if ((lw < rw) ^ (lw < 0) ^ (rw < 0)) {
						return -1;
					}
					return 1;
				}
			}
		}

		// second round with ints
		if (minLength - pos >= INT_LENGTH) {
			final int ints = (minLength - pos) / INT_LENGTH;
			for (final int end = pos + (ints * INT_LENGTH); pos < end; pos += INT_LENGTH) {
				final int lw = getInt(left, leftOffset + pos);
				final int rw = getInt(right, rightOffset + pos);
				if (lw != rw) {
					if ((lw < rw) ^ (lw < 0) ^ (rw < 0)) {
						return -1;
					}
					return 1;
				}
			}
		}

		// third round with bytes
		for (; pos < minLength; pos++) {
			final int lw = left[leftOffset + pos];
			final int rw = right[rightOffset + pos];
			if (lw != rw) {
				if ((lw < rw) ^ (lw < 0) ^ (rw < 0)) {
					return -1;
				}
				return 1;
			}
		}
		return Integer.signum(leftLength - rightLength);
	}

	/**
	 * Lexicographical (unsigned) comparison.
	 */
	public static int compareBuffers(final ByteBuffer left, final ByteBuffer right) {
		if (right.isDirect() || left.isDirect()) {
			throw new IllegalArgumentException("buffer is direct");
		}
		return compareArrays(left.array(), left.arrayOffset() + left.position(), left.remaining(),
				right.array(), right.arrayOffset() + right.position(), right.remaining());
	}

	/**
	 * Byte[] hash code using unsafe methods if available. Same as
	 * <code>hashCode(array, 0, array.length)</code>.
	 */
	public static int hashCode(final byte[] array) {
		return hashCode(array, 0, array.length);
	}

	/**
	 * Byte[] hash code using unsafe methods if available.
	 */
	public static int hashCode(final byte[] array, final int offset, final int length) {
		if (array == null) {
			return 0;
		}
		final int end = offset + length;
		if (offset < 0 || length < 0 || end > array.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		// small optimization
		if (length == INT_LENGTH) {
			return getInt(array, offset);
		}

		int index = offset;
        int hashcode = 1;

		// first round with ints
		if (length >= INT_LENGTH) {
			final int ints = length / INT_LENGTH;
			final int intend = index + (ints * INT_LENGTH);
			for (; index < intend; index += INT_LENGTH) {
				hashcode = 31 * hashcode + getInt(array, index);
			}
		}

		// third round with bytes
		for (; index < end; index++) {
			hashcode = 31 * hashcode + array[index];
		}
		return hashcode;
	}

	/**
	 * Commodity method to convert a byte array into a string of hex values.
	 */
	public static String toString(final byte[] value, final int offset, final int length) {
		if (value == null) {
			return "null";
		}
		if (length <= 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder(length * 2);
		try (final Formatter fmt = new Formatter(sb)) {
			final int k = offset + length;
			for (int i = offset; i < k; i++) {
				fmt.format("%02X", Byte.valueOf(value[i]));
			}
		}
		return sb.toString();
	}

}
