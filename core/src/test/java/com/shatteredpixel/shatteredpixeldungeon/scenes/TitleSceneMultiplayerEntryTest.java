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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.windows.WndMultiplayerRoom;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TitleSceneMultiplayerEntryTest {

	@Test
	public void multiplayerEntryFollowsPlatformRoomBridgeAvailability() {
		assertFalse(TitleScene.shouldShowMultiplayerEntry(false, false));
		assertFalse(TitleScene.shouldShowMultiplayerEntry(false, true));
		assertFalse(TitleScene.shouldShowMultiplayerEntry(true, false));
		assertTrue(TitleScene.shouldShowMultiplayerEntry(true, true));
	}

	@Test
	public void multiplayerMenuChoicesMapToRoomEntryModes() {
		assertEquals(WndMultiplayerRoom.Mode.CREATE, TitleScene.multiplayerRoomModeForMenuIndex(0));
		assertEquals(WndMultiplayerRoom.Mode.JOIN, TitleScene.multiplayerRoomModeForMenuIndex(1));
		assertNull(TitleScene.multiplayerRoomModeForMenuIndex(-1));
		assertNull(TitleScene.multiplayerRoomModeForMenuIndex(2));
	}

	@Test
	public void multiplayerMenuCanExposeRejoinAsTopLevelAction() {
		assertEquals(TitleScene.MultiplayerRoomMenuAction.REJOIN,
				TitleScene.multiplayerRoomActionForMenuIndex(0, true));
		assertEquals(TitleScene.MultiplayerRoomMenuAction.CREATE,
				TitleScene.multiplayerRoomActionForMenuIndex(1, true));
		assertEquals(TitleScene.MultiplayerRoomMenuAction.JOIN,
				TitleScene.multiplayerRoomActionForMenuIndex(2, true));
		assertNull(TitleScene.multiplayerRoomActionForMenuIndex(2, false));
		assertNull(TitleScene.multiplayerRoomActionForMenuIndex(3, true));
	}
}
