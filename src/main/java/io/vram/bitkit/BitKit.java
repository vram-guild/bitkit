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

import java.util.function.IntConsumer;

public class BitKit {
	public static void forEachBit(long bits, IntConsumer consumer) {
		if (bits != 0) {
			forEachBit32((int) (bits & 0xFFFFFFFFL), 0, consumer);
			forEachBit32((int) ((bits >>> 32) & 0xFFFFFFFFL), 32, consumer);
		}
	}

	private static void forEachBit32(int bits, int baseIndex, IntConsumer consumer) {
		if (bits != 0) {
			forEachBit16((bits & 0xFFFF), baseIndex, consumer);
			forEachBit16(((bits >>> 16) & 0xFFFF), baseIndex + 16, consumer);
		}
	}

	private static void forEachBit16(int bits, int baseIndex, IntConsumer consumer) {
		if (bits != 0) {
			forEachBit8((bits & 0xFF), baseIndex, consumer);
			forEachBit8(((bits >>> 8) & 0xFF), baseIndex + 8, consumer);
		}
	}

	private static void forEachBit8(int bits, int baseIndex, IntConsumer consumer) {
		if (bits != 0) {
			forEachBit4((bits & 0xF), baseIndex, consumer);
			forEachBit4(((bits >>> 4) & 0xF), baseIndex + 4, consumer);
		}
	}

	private static void forEachBit4(int bits, int baseIndex, IntConsumer consumer) {
		switch (bits) {
			case 0:
				break;

			case 1:
				consumer.accept(baseIndex);
				break;

			case 2:
				consumer.accept(baseIndex + 1);
				break;

			case 3:
				consumer.accept(baseIndex);
				consumer.accept(baseIndex + 1);
				break;

			case 4:
				consumer.accept(baseIndex + 2);
				break;

			case 5:
				consumer.accept(baseIndex);
				consumer.accept(baseIndex + 2);
				break;

			case 6:
				consumer.accept(baseIndex + 1);
				consumer.accept(baseIndex + 2);
				break;

			case 7:
				consumer.accept(baseIndex);
				consumer.accept(baseIndex + 1);
				consumer.accept(baseIndex + 2);
				break;

			case 8:
				consumer.accept(baseIndex + 3);
				break;

			case 9:
				consumer.accept(baseIndex);
				consumer.accept(baseIndex + 3);
				break;

			case 10:
				consumer.accept(baseIndex + 1);
				consumer.accept(baseIndex + 3);
				break;

			case 11:
				consumer.accept(baseIndex);
				consumer.accept(baseIndex + 1);
				consumer.accept(baseIndex + 3);
				break;

			case 12:
				consumer.accept(baseIndex + 2);
				consumer.accept(baseIndex + 3);
				break;

			case 13:
				consumer.accept(baseIndex);
				consumer.accept(baseIndex + 2);
				consumer.accept(baseIndex + 3);
				break;

			case 14:
				consumer.accept(baseIndex + 1);
				consumer.accept(baseIndex + 2);
				consumer.accept(baseIndex + 3);
				break;

			case 15:
				consumer.accept(baseIndex);
				consumer.accept(baseIndex + 1);
				consumer.accept(baseIndex + 2);
				consumer.accept(baseIndex + 3);
				break;
		}
	}

	public static int bitCount8(int byteValue) {
		return bitCount4(byteValue & 0xF) + bitCount4((byteValue >>> 4) & 0xF);
	}

	public static int bitCount4(int halfByteValue) {
		switch (halfByteValue) {
			case 0:
				return 0;

			case 1:
			case 2:
			case 4:
			case 8:
				return 1;

			case 3:
			case 5:
			case 6:
			case 9:
			case 10:
			case 12:
				return 2;

			case 7:
			case 11:
			case 13:
			case 14:
				return 3;

			case 15:
				return 4;
		}

		assert false : "bad bitcount4 value";
		return 0;
	}

	/**
	 * Bit length needed to contain the given value. Intended for unsigned values.
	 */
	public static int bitLength(long maxValue) {
		return Long.SIZE - Long.numberOfLeadingZeros(maxValue - 1);
	}

	/**
	 * Bit length needed to contain the given value. Intended for unsigned values.
	 */
	public static int bitLength(int maxValue) {
		if (maxValue == 0) {
			return 0;
		}

		return Integer.SIZE - Integer.numberOfLeadingZeros(maxValue - 1);
	}

	/**
	 * Returns bit mask for a value of given bit length.
	 */
	public static long longBitMask(int bitLength) {
		bitLength = bitLength < 0 ? 0 : (bitLength > Long.SIZE ? Long.SIZE : bitLength);

		// note: can't use mask = (1L << (bitLength+1)) - 1 here due to overflow &
		// signed values
		long mask = 0L;

		for (int i = 0; i < bitLength; i++) {
			mask |= (1L << i);
		}

		return mask;
	}

	/**
	 * Gives low bits of long value for serialization as two int values<br>
	 * . Use {@link #longFromInts(int, int)} to reconstruct.
	 */
	public static final int longToIntLow(final long input) {
		return (int) input;
	}

	/**
	 * Gives high bits of long value for serialization as two int values<br>
	 * . Use {@link #longFromInts(int, int)} to reconstruct.
	 */
	public static final int longToIntHigh(final long input) {
		return (int) (input >> 32);
	}

	/**
	 * Reconstitutes long value from integer values given by
	 * {@link #longToIntLow(long)} and {@link #longToIntHigh(long)}.
	 */
	public static final long longFromInts(final int high, final int low) {
		return (long) high << 32 | (low & 0xFFFFFFFFL);
	}

	/**
	 * Returns bit mask for a value of given bit length.
	 */
	public static int intBitMask(int bitLength) {
		bitLength = bitLength < 0 ? 0 : (bitLength > Integer.SIZE ? Integer.SIZE : bitLength);

		// note: can't use mask = (1L << (bitLength+1)) - 1 here due to overflow &
		// signed values
		int mask = 0;

		for (int i = 0; i < bitLength; i++) {
			mask |= (1L << i);
		}

		return mask;
	}
}
