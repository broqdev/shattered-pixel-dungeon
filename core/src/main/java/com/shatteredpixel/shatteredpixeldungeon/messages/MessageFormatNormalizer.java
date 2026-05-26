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

final class MessageFormatNormalizer {

	private static final String FORMAT_CONVERSIONS = "bBhHsScCdoxXeEfgGaAtT";
	private static final String FORMAT_FLAGS = "-#+ 0,(<";

	private MessageFormatNormalizer() {
	}

	static String normalizeImplicitArgumentIndexes( String format ) {
		StringBuilder result = new StringBuilder(format.length());
		int ordinaryIndex = 1;

		for (int i = 0; i < format.length(); i++) {
			char c = format.charAt(i);
			if (c != '%') {
				result.append(c);
				continue;
			}

			int tokenStart = i;
			i++;
			if (i >= format.length()) {
				result.append(format, tokenStart, format.length());
				break;
			}

			char next = format.charAt(i);
			if (next == '%' || next == 'n') {
				result.append(format, tokenStart, i + 1);
				continue;
			}

			FormatToken token = parseConversion(format, tokenStart, i);
			if (token == null) {
				result.append('%');
				i = tokenStart;
				continue;
			}

			if (token.explicitlyIndexed || token.relative) {
				result.append(format, tokenStart, token.end);
			} else {
				result.append('%').append(ordinaryIndex++).append('$');
				result.append(format, tokenStart + 1, token.end);
			}
			i = token.end - 1;
		}

		return result.toString();
	}

	private static FormatToken parseConversion( String format, int tokenStart, int index ) {
		int i = index;
		while (i < format.length() && Character.isDigit(format.charAt(i))) {
			i++;
		}

		boolean explicitlyIndexed = i < format.length() && format.charAt(i) == '$' && i > index;
		if (explicitlyIndexed) {
			i++;
		}

		boolean relative = false;
		while (i < format.length() && FORMAT_FLAGS.indexOf(format.charAt(i)) >= 0) {
			relative = relative || format.charAt(i) == '<';
			i++;
		}

		while (i < format.length() && Character.isDigit(format.charAt(i))) {
			i++;
		}

		if (i < format.length() && format.charAt(i) == '.') {
			i++;
			while (i < format.length() && Character.isDigit(format.charAt(i))) {
				i++;
			}
		}

		if (i < format.length() && (format.charAt(i) == 't' || format.charAt(i) == 'T')) {
			i++;
		}

		if (i >= format.length() || FORMAT_CONVERSIONS.indexOf(format.charAt(i)) < 0) {
			return null;
		}

		return new FormatToken(i + 1, explicitlyIndexed, relative);
	}

	private static class FormatToken {
		private final int end;
		private final boolean explicitlyIndexed;
		private final boolean relative;

		private FormatToken( int end, boolean explicitlyIndexed, boolean relative ) {
			this.end = end;
			this.explicitlyIndexed = explicitlyIndexed;
			this.relative = relative;
		}
	}
}
