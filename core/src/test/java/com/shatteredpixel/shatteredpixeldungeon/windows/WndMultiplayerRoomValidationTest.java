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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class WndMultiplayerRoomValidationTest {

	@Test
	public void roomEntryRequiresMinimumRoomNameAndPasswordLengths() {
		assertEquals("room_name_short",
				WndMultiplayerRoom.validationMessageKey("abc12", "secret", "p"));
		assertEquals("password_short",
				WndMultiplayerRoom.validationMessageKey("abcdef", "12345", "p"));
		assertNull(WndMultiplayerRoom.validationMessageKey("abcdef", "123456", "p"));
	}

	@Test
	public void roomEntryKeepsExistingCharacterAndMaximumLimits() {
		assertEquals("room_name_chars",
				WndMultiplayerRoom.validationMessageKey("abc 12", "123456", "p"));
		assertEquals("room_name_long",
				WndMultiplayerRoom.validationMessageKey("abcdefghijklmnopqrstuvwxyzabcdefg", "123456", "p"));
		assertEquals("password_long",
				WndMultiplayerRoom.validationMessageKey("abcdef",
						"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrs", "p"));
		assertEquals("player_name_required",
				WndMultiplayerRoom.validationMessageKey("abcdef", "123456", ""));
		assertEquals("player_name_long",
				WndMultiplayerRoom.validationMessageKey("abcdef", "123456", "abcdefghijklmnopqrstuvwxyz"));
	}

	@Test
	public void tabCyclesBetweenRoomEntryInputs() {
		assertEquals(WndMultiplayerRoom.InputTarget.PASSWORD,
				WndMultiplayerRoom.nextInputTarget(WndMultiplayerRoom.InputTarget.ROOM_NAME, false));
		assertEquals(WndMultiplayerRoom.InputTarget.PLAYER_NAME,
				WndMultiplayerRoom.nextInputTarget(WndMultiplayerRoom.InputTarget.PASSWORD, false));
		assertEquals(WndMultiplayerRoom.InputTarget.ROOM_NAME,
				WndMultiplayerRoom.nextInputTarget(WndMultiplayerRoom.InputTarget.PLAYER_NAME, false));

		assertEquals(WndMultiplayerRoom.InputTarget.PLAYER_NAME,
				WndMultiplayerRoom.nextInputTarget(WndMultiplayerRoom.InputTarget.ROOM_NAME, true));
		assertEquals(WndMultiplayerRoom.InputTarget.PASSWORD,
				WndMultiplayerRoom.nextInputTarget(WndMultiplayerRoom.InputTarget.PLAYER_NAME, true));
		assertEquals(WndMultiplayerRoom.InputTarget.ROOM_NAME,
				WndMultiplayerRoom.nextInputTarget(WndMultiplayerRoom.InputTarget.PASSWORD, true));
	}
}
