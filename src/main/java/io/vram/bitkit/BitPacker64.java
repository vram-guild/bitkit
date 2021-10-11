/*
 * Copyright © Contributing Authors (see ATTRIBUTION.md)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.vram.bitkit;

import java.util.Arrays;
import java.util.function.ObjLongConsumer;
import java.util.function.ToLongFunction;

public class BitPacker64<T> {
	protected int totalBitLength;
	protected long bitMask;
	protected final ObjLongConsumer<T> writer;
	protected final ToLongFunction<T> reader;

	public BitPacker64(ToLongFunction<T> reader, ObjLongConsumer<T> writer) {
		this.reader = reader;
		this.writer = writer;
	}

	protected void addElement(BitElement element) {
		element.shift = this.totalBitLength;
		element.shiftedMask = element.mask << element.shift;
		element.shiftedInverseMask = ~element.shiftedMask;

		this.totalBitLength += element.bitLength;
		this.bitMask = BitKit.longBitMask(totalBitLength);

		if (totalBitLength > 64) {
			if (totalBitLength > 32) {
				throw new IndexOutOfBoundsException(String.format("BitPacker length %d exceeds limit of 64", totalBitLength));
			}
		}
	}

	public final int bitLength() {
		return this.totalBitLength;
	}

	public final long bitMask() {
		return this.bitMask;
	}

	public <V extends Enum<?>> EnumElement<V> createEnumElement(Class<V> e) {
		final EnumElement<V> result = new EnumElement<>(e);
		this.addElement(result);
		return result;
	}

	public <V extends Enum<?>> NullableEnumElement<V> createNullableEnumElement(Class<V> e) {
		final NullableEnumElement<V> result = new NullableEnumElement<>(e);
		this.addElement(result);
		return result;
	}

	public IntElement createIntElement(int minValue, int maxValue) {
		final IntElement result = new IntElement(minValue, maxValue);
		this.addElement(result);
		return result;
	}

	/**
	 * use this when you just need zero-based positive integers. Same as
	 * createIntElement(0, count-1)
	 */
	public IntElement createIntElement(int valueCount) {
		final IntElement result = new IntElement(0, valueCount - 1);
		this.addElement(result);
		return result;
	}

	public LongElement createLongElement(long minValue, long maxValue) {
		final LongElement result = new LongElement(minValue, maxValue);
		this.addElement(result);
		return result;
	}

	/**
	 * use this when you just need zero-based positive (long) integers. Same as
	 * createLongElement(0, count-1)
	 */
	public LongElement createLongElement(long valueCount) {
		final LongElement result = new LongElement(0, valueCount - 1);
		this.addElement(result);
		return result;
	}

	public BooleanElement createBooleanElement() {
		final BooleanElement result = new BooleanElement();
		this.addElement(result);
		return result;
	}

	protected abstract class BitElement {
		protected final int bitLength;
		protected long mask;
		protected int shift;
		protected long shiftedMask;
		protected long shiftedInverseMask;

		private BitElement(long valueCount) {
			this.bitLength = BitKit.bitLength(valueCount);
			this.mask = BitKit.longBitMask(this.bitLength);
		}

		/**
		 * Mask that isolates bits for this element. Useful to compare this and other
		 * elements simultaneously
		 */
		public final long comparisonMask() {
			return this.shiftedMask;
		}
	}

	public class EnumElement<V extends Enum<?>> extends BitElement {
		private final V[] values;

		private EnumElement(Class<V> e) {
			super(e.getEnumConstants().length);
			this.values = e.getEnumConstants();
		}

		public final long getBits(V e) {
			return (e.ordinal() & mask) << shift;
		}

		public final void setValue(V e, T inObject) {
			writer.accept(inObject, setValue(e, reader.applyAsLong(inObject)));
		}

		public final long setValue(V e, long inBits) {
			return ((inBits & shiftedInverseMask) | getBits(e));
		}

		public final V getValue(T fromObject) {
			return getValue(reader.applyAsLong(fromObject));
		}

		public final V getValue(long fromBits) {
			return values[(int) ((fromBits >> shift) & mask)];
		}
	}

	public class NullableEnumElement<V extends Enum<?>> extends BitElement {
		private final V[] values;

		final int nullOrdinal;

		private NullableEnumElement(Class<V> e) {
			super(e.getEnumConstants().length + 1);
			final V[] constants = e.getEnumConstants();
			this.values = Arrays.copyOf(constants, constants.length + 1);
			this.nullOrdinal = constants.length;
		}

		public final long getBits(V e) {
			final int ordinal = e == null ? this.nullOrdinal : e.ordinal();
			return (ordinal & mask) << shift;
		}

		public final void setValue(V e, T inObject) {
			writer.accept(inObject, setValue(e, reader.applyAsLong(inObject)));
		}

		public final long setValue(V e, long inBits) {
			return ((inBits & shiftedInverseMask) | getBits(e));
		}

		public final V getValue(T fromObject) {
			return getValue(reader.applyAsLong(fromObject));
		}

		public final V getValue(long fromBits) {
			return values[(int) ((fromBits >> shift) & mask)];
		}
	}

	/** Stores values in given range as bits. Handles negative values */
	public class IntElement extends BitElement {
		private final int minValue;

		private IntElement(int minValue, int maxValue) {
			super(maxValue - minValue + 1);
			this.minValue = minValue;
		}

		public final long getBits(int i) {
			return (((long) i - minValue) & mask) << shift;
		}

		public final void setValue(int i, T inObject) {
			writer.accept(inObject, setValue(i, reader.applyAsLong(inObject)));
		}

		public final long setValue(int i, long inBits) {
			return ((inBits & shiftedInverseMask) | getBits(i));
		}

		public final int getValue(T fromObject) {
			return getValue(reader.applyAsLong(fromObject));
		}

		public final int getValue(long fromBits) {
			return (int) (((fromBits >> shift) & mask) + minValue);
		}
	}

	/**
	 * Stores values in given range as bits. Handles negative values but if range is
	 * too large will overflow due lack of unsigned numeric types.
	 */
	public class LongElement extends BitElement {
		private final long minValue;

		private LongElement(long minValue, long maxValue) {
			super(maxValue - minValue + 1);
			this.minValue = minValue;
		}

		public final long getBits(long i) {
			return ((i - minValue) & mask) << shift;
		}

		public final void setValue(long i, T inObject) {
			writer.accept(inObject, setValue(i, reader.applyAsLong(inObject)));
		}

		public final long setValue(long i, long inBits) {
			return ((inBits & shiftedInverseMask) | getBits(i));
		}

		public final long getValue(T fromObject) {
			return getValue(reader.applyAsLong(fromObject));
		}

		public final long getValue(long fromBits) {
			return ((fromBits >> shift) & mask) + minValue;
		}
	}

	public class BooleanElement extends BitElement {
		private BooleanElement() {
			super(2);
		}

		public final long getBits(boolean b) {
			return ((b ? 1 : 0) & mask) << shift;
		}

		public final void setValue(boolean b, T inObject) {
			writer.accept(inObject, setValue(b, reader.applyAsLong(inObject)));
		}

		public final long setValue(boolean b, long inBits) {
			return ((inBits & shiftedInverseMask) | getBits(b));
		}

		public final boolean getValue(T fromObject) {
			return getValue(reader.applyAsLong(fromObject));
		}

		public final boolean getValue(long fromBits) {
			return ((fromBits >> shift) & mask) == 1;
		}
	}
}
