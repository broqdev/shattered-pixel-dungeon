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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WndMultiplayerLobbyStateTest {

	@Test
	public void parsesSnapshotEventSeatsAndRules() {
		WndMultiplayerLobby.LobbyState state = WndMultiplayerLobby.LobbyState.fromEvent(new String[]{
				"room-lobby",
				"race-room",
				"p-2",
				"false",
				"abc123",
				"3",
					"4",
				"30",
				"false",
				"true",
				"9999999999999",
				"p-1", "Alice", "ff6b6b", "true", "WARRIOR", "true", "true",
				"p-2", "Bob", "4dabf7", "false", "MAGE", "false", "true",
				"", "", "", "false", "", "false", "true",
				"", "", "", "false", "", "false", "true",
				"1",
				"p-3", "Cam", "69db7c", "false", "true"
		});

		assertEquals("race-room", state.roomName);
		assertEquals("p-2", state.selfParticipantId);
		assertFalse(state.selfOwner);
		assertEquals("abc123", state.seedChecksum);
		assertEquals(3, state.participantCount);
			assertEquals(4, state.maxConnections);
		assertEquals(30, state.floorChaseTurns);
		assertFalse(state.floorChaseInfinite);
		assertTrue(state.countdownActive);
		assertEquals(9999999999999L, state.countdownDeadlineMs);
		assertEquals("Alice", state.seats[0].name);
		assertTrue(state.seats[0].owner);
		assertTrue(state.seats[0].ready);
		assertEquals("MAGE", state.seats[1].heroChoice);
		assertEquals("Bob", state.selfParticipant().name);
		assertFalse(state.selfIsWatcher());
		assertFalse(state.selfReady());
			assertFalse(state.readyToStart());
			assertEquals(1, state.watcherCount);
			assertEquals("Cam", state.watchers[0].name);
		}

	@Test
	public void parsesFullPlayerCapacityWithoutWatcherRows() {
		WndMultiplayerLobby.LobbyState state = WndMultiplayerLobby.LobbyState.fromEvent(new String[]{
				"room-lobby",
				"full-room",
				"p-4",
				"false",
				"def456",
				"4",
				"4",
				"40",
				"false",
				"false",
				"",
				"p-1", "Owner", "ff6b6b", "true", "WARRIOR", "false", "true",
				"p-2", "P2", "4dabf7", "false", "MAGE", "false", "true",
				"p-3", "P3", "69db7c", "false", "ROGUE", "false", "true",
				"p-4", "P4", "ffd43b", "false", "HUNTRESS", "false", "true",
				"0"
		});

		assertEquals(4, state.participantCount);
		assertEquals(4, state.maxConnections);
		assertEquals(4, state.playerCount());
		assertEquals(0, state.watcherCount);
		assertFalse(state.selfIsWatcher());
		assertEquals("P4", state.selfParticipant().name);
		assertFalse(state.readyToStart());
	}

	@Test
	public void ownerPlayerCanStartWhenOtherPlayersAreReady() {
		WndMultiplayerLobby.LobbyState state = WndMultiplayerLobby.LobbyState.fromEvent(new String[]{
				"room-lobby",
				"race-room",
				"p-1",
				"true",
				"abc123",
				"2",
					"4",
				"20",
				"false",
				"false",
				"",
				"p-1", "Alice", "ff6b6b", "true", "WARRIOR", "false", "true",
				"p-2", "Bob", "4dabf7", "false", "MAGE", "true", "true",
				"", "", "", "false", "", "false", "true",
				"", "", "", "false", "", "false", "true",
				"0"
		});

		assertTrue(state.selfOwner);
		assertEquals(2, state.playerCount());
		assertTrue(state.readyToStart());
		assertTrue(state.startButtonEnabled());
	}

	@Test
	public void mapsBridgeRejectionMessagesToLobbyText() {
		assertEquals("reject_stale_room", WndMultiplayerLobby.statusMessageKey("stale room state"));
		assertEquals("reject_seats_full", WndMultiplayerLobby.statusMessageKey("player seats full"));
		assertEquals("reject_invalid_hero", WndMultiplayerLobby.statusMessageKey("invalid hero"));
		assertEquals("reject_rejoin_available", WndMultiplayerLobby.statusMessageKey("room rejoin available"));
		assertEquals("custom bridge note", WndMultiplayerLobby.statusMessage("custom bridge note"));
		assertEquals("", WndMultiplayerLobby.statusMessage(null));
		assertTrue(WndMultiplayerLobby.statusClosesLobby("Left room."));
		assertFalse(WndMultiplayerLobby.statusClosesLobby("Leaving room..."));
		assertFalse(WndMultiplayerLobby.statusClosesLobby("Room joined."));
	}

	@Test
	public void pendingStatusExpiresAfterShortWindow() {
		assertFalse(WndMultiplayerLobby.pendingStatusExpired(4f, 3.99f));
		assertTrue(WndMultiplayerLobby.pendingStatusExpired(4f, 4f));
		assertFalse(WndMultiplayerLobby.pendingStatusExpired(0f, 100f));
	}

	@Test
	public void lobbyStopsPollingAfterEventsThatReplaceOrCloseIt() {
		assertFalse(WndMultiplayerLobby.shouldContinuePollingAfterRoomEvent("room-lobby", false));
		assertFalse(WndMultiplayerLobby.shouldContinuePollingAfterRoomEvent("room-launch", true));
		assertTrue(WndMultiplayerLobby.shouldContinuePollingAfterRoomEvent("room-launch", false));
		assertTrue(WndMultiplayerLobby.shouldContinuePollingAfterRoomEvent("room-status", false));
	}
}
