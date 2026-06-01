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

package com.shatteredpixel.shatteredpixeldungeon.multiplayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;

import org.junit.Test;

public class WebMultiplayerRoomLaunchTest {

	@Test
	public void parsesPlayerRoomLaunchEvent() {
		WebMultiplayer.RoomLaunch launch = WebMultiplayer.RoomLaunch.fromEvent(new String[]{
				"room-launch",
				"race-room",
				"p-1",
				"player",
				"MAGE",
				"seed-abc",
				"abc123",
				"30",
				"false",
				"7",
				"",
				"Alice",
				"ff6b6b",
				"p-2,p-1"
		});

		assertTrue(launch.valid);
		assertFalse(launch.watcher);
		assertEquals("race-room", launch.roomName);
		assertEquals("p-1", launch.participantId);
		assertEquals(HeroClass.MAGE, launch.heroClass);
		assertEquals("seed-abc", launch.sharedRunSeed);
		assertEquals("abc123", launch.seedChecksum);
		assertEquals(30, launch.floorChaseTurns);
		assertFalse(launch.floorChaseInfinite);
		assertEquals(7, launch.roomEpoch);
		assertEquals("Alice", launch.playerName);
		assertEquals("ff6b6b", launch.playerColor);
		assertEquals("p-2,p-1", launch.playerSeatOrder);
	}

	@Test
	public void parsesWatcherRoomLaunchEvent() {
		WebMultiplayer.RoomLaunch launch = WebMultiplayer.RoomLaunch.fromEvent(new String[]{
				"room-launch",
				"race-room",
				"p-watch",
				"watcher",
				"",
				"seed-abc",
				"abc123",
				"20",
				"true",
				"8",
				"p-1"
		});

		assertTrue(launch.valid);
		assertTrue(launch.watcher);
		assertEquals("p-1", launch.watchTargetId);
		assertTrue(launch.floorChaseInfinite);
		assertEquals(8, launch.roomEpoch);
	}

	@Test
	public void rejectsWatcherRoomLaunchWithoutTarget() {
		WebMultiplayer.RoomLaunch launch = WebMultiplayer.RoomLaunch.fromEvent(new String[]{
				"room-launch",
				"race-room",
				"p-watch",
				"watcher",
				"",
				"seed-abc",
				"abc123",
				"20",
				"true",
				"8",
				""
		});

		assertFalse(launch.valid);
	}

	@Test
	public void preparesFreshCompetitiveRunsWithSharedSeedAndPerSeatHero() {
		RoomLaunchState oldState = new RoomLaunchState();
		try {
			WebMultiplayer.RoomLaunch mageLaunch = WebMultiplayer.RoomLaunch.fromEvent(new String[]{
					"room-launch",
					"race-room",
					"p-1",
					"player",
					"MAGE",
					"room-seed-42",
					"seed42",
					"30",
					"false",
					"7",
					""
			});
			WebMultiplayer.RoomLaunch rogueLaunch = WebMultiplayer.RoomLaunch.fromEvent(new String[]{
					"room-launch",
					"race-room",
					"p-2",
					"player",
					"ROGUE",
					"room-seed-42",
					"seed42",
					"30",
					"false",
					"7",
					""
			});

			assertTrue(WebMultiplayer.prepareRoomRunForLaunch(mageLaunch, 2));
			long mageSeed = Dungeon.seed;
			long mageFloorOneSeed = Dungeon.seedForDepth(1, 0);
			long mageFloorTwoSeed = Dungeon.seedForDepth(2, 0);
			long mageBranchFloorSeed = Dungeon.seedForDepth(1, 1);
			assertEquals("room-seed-42", Dungeon.customSeedText);
			assertEquals(HeroClass.MAGE, GamesInProgress.selectedClass);
			assertFalse(GamesInProgress.randomizedClass);
			assertEquals(2, GamesInProgress.curSlot);
			assertEquals(InterlevelScene.Mode.DESCEND, InterlevelScene.mode);
			assertEquals(30, WebMultiplayer.floorChaseTurns());
			assertFalse(WebMultiplayer.floorChaseInfinite());

			assertTrue(WebMultiplayer.prepareRoomRunForLaunch(rogueLaunch, 3));
			assertEquals(mageSeed, Dungeon.seed);
			assertEquals(mageFloorOneSeed, Dungeon.seedForDepth(1, 0));
			assertEquals(mageFloorTwoSeed, Dungeon.seedForDepth(2, 0));
			assertEquals(mageBranchFloorSeed, Dungeon.seedForDepth(1, 1));
			assertEquals("room-seed-42", Dungeon.customSeedText);
			assertEquals(HeroClass.ROGUE, GamesInProgress.selectedClass);
			assertEquals(3, GamesInProgress.curSlot);
		} finally {
			oldState.restore();
		}
	}

	@Test
	public void preparesCompetitiveRoomRunsInTransientMultiplayerSlot() {
		RoomLaunchState oldState = new RoomLaunchState();
		try {
			WebMultiplayer.RoomLaunch launch = WebMultiplayer.RoomLaunch.fromEvent(new String[]{
					"room-launch",
					"race-room",
					"p-1",
					"player",
					"MAGE",
					"room-seed-42",
					"seed42",
					"40",
					"false",
					"7",
					""
			});

			assertTrue(WebMultiplayer.prepareRoomRunForLaunch(launch, GamesInProgress.TRANSIENT_MULTIPLAYER_SLOT));
			assertEquals(GamesInProgress.TRANSIENT_MULTIPLAYER_SLOT, GamesInProgress.curSlot);
			assertTrue(WebMultiplayer.transientRun());
			assertEquals(HeroClass.MAGE, GamesInProgress.selectedClass);
			assertEquals(InterlevelScene.Mode.DESCEND, InterlevelScene.mode);
		} finally {
			oldState.restore();
			WebMultiplayer.clearTransientRunForTests();
		}
	}

	@Test
	public void doesNotPrepareWatcherAsFreshPlayerRun() {
		RoomLaunchState oldState = new RoomLaunchState();
		try {
			WebMultiplayer.RoomLaunch launch = WebMultiplayer.RoomLaunch.fromEvent(new String[]{
					"room-launch",
					"race-room",
					"p-watch",
					"watcher",
					"",
					"room-seed-42",
					"seed42",
					"20",
					"true",
					"8",
					"p-1"
			});

			assertFalse(WebMultiplayer.prepareRoomRunForLaunch(launch, 2));
			assertEquals(oldState.curSlot, GamesInProgress.curSlot);
			assertEquals(oldState.selectedClass, GamesInProgress.selectedClass);
			assertEquals(oldState.seed, Dungeon.seed);
			assertEquals(oldState.customSeedText, Dungeon.customSeedText);
		} finally {
			oldState.restore();
		}
	}

	private static class RoomLaunchState {

		private final int curSlot = GamesInProgress.curSlot;
		private final HeroClass selectedClass = GamesInProgress.selectedClass;
		private final boolean randomizedClass = GamesInProgress.randomizedClass;
		private final long seed = Dungeon.seed;
		private final String customSeedText = Dungeon.customSeedText;
		private final boolean daily = Dungeon.daily;
		private final boolean dailyReplay = Dungeon.dailyReplay;
		private final InterlevelScene.Mode interlevelMode = InterlevelScene.mode;

		private void restore() {
			GamesInProgress.curSlot = curSlot;
			GamesInProgress.selectedClass = selectedClass;
			GamesInProgress.randomizedClass = randomizedClass;
			Dungeon.seed = seed;
			Dungeon.customSeedText = customSeedText;
			Dungeon.daily = daily;
			Dungeon.dailyReplay = dailyReplay;
			InterlevelScene.mode = interlevelMode;
		}
	}
}
