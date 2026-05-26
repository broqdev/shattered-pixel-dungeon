/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.messages;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MessageFormatNormalizerTest {

	@Test
	public void literalPercentBeforeImplicitArgumentIsNormalized() {
		assertEquals(
				"drop rate 20%%, max %1$d item(s)",
				MessageFormatNormalizer.normalizeImplicitArgumentIndexes("drop rate 20%%, max %d item(s)"));
	}

	@Test
	public void sequentialImplicitArgumentsKeepJvmArgumentOrder() {
		assertEquals(
				"%1$s %2$d %3$.1f",
				MessageFormatNormalizer.normalizeImplicitArgumentIndexes("%s %d %.1f"));
	}

	@Test
	public void flagsWidthPrecisionAndDateTimeArePreserved() {
		assertEquals(
				"%1$+05d %2$8.2f %3$tH",
				MessageFormatNormalizer.normalizeImplicitArgumentIndexes("%+05d %8.2f %tH"));
	}

	@Test
	public void explicitEscapedNewlineAndRelativeTokensArePreserved() {
		assertEquals(
				"%%%1$s%n%<s %2$d",
				MessageFormatNormalizer.normalizeImplicitArgumentIndexes("%%%1$s%n%<s %2$d"));
	}
}
