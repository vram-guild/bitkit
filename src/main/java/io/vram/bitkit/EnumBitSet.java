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

import java.lang.reflect.Array;

/**
 * Used for fast mapping of a enum to boolean values serialized to a numeric
 * primitive.
 */
public class EnumBitSet<T extends Enum<?>> {
	private final T[] values;

	private final Class<T> clazz;

	public EnumBitSet(Class<T> clazz) {
		this.clazz = clazz;
		this.values = clazz.getEnumConstants();
	}

	/**
	 * Number of distinct values for flag values produced and consumed by this
	 * instance. Derivation is trivially simple. Main use is for clarity.
	 */
	public final int combinationCount() {
		return 2 << (values.length - 1);
	}

	public final int getFlagsForIncludedValues(@SuppressWarnings("unchecked") T... included) {
		int result = 0;

		for (final T e : included) {
			result |= (1 << e.ordinal());
		}

		return result;
	}

	public int getFlagsForIncludedValues(T v0, T v1, T v2, T v3) {
		return (1 << v0.ordinal()) | (1 << v1.ordinal()) | (1 << v2.ordinal()) | (1 << v3.ordinal());
	}

	public int getFlagsForIncludedValues(T v0, T v1, T v2) {
		return (1 << v0.ordinal()) | (1 << v1.ordinal()) | (1 << v2.ordinal());
	}

	public final int getFlagsForIncludedValues(T v0, T v1) {
		return (1 << v0.ordinal()) | (1 << v1.ordinal());
	}

	public final int getFlagForValue(T v0) {
		return (1 << v0.ordinal());
	}

	public final int setFlagForValue(T v, int flagsIn, boolean isSet) {
		if (isSet) {
			return flagsIn | (1 << v.ordinal());
		} else {
			return flagsIn & ~(1 << v.ordinal());
		}
	}

	public final boolean isFlagSetForValue(T v, int flagsIn) {
		return (flagsIn & (1 << v.ordinal())) != 0;
	}

	public final T[] getValuesForSetFlags(int flagsIn) {
		@SuppressWarnings("unchecked")
		final
		T[] result = (T[]) Array.newInstance(clazz, Integer.bitCount(flagsIn));

		final int bitCount = Integer.SIZE - Integer.numberOfLeadingZeros(flagsIn);
		int j = 0;

		for (int i = 0; i < bitCount; i++) {
			if ((flagsIn & (1 << i)) != 0) {
				result[j++] = values[i];
			}
		}

		return result;
	}
}
