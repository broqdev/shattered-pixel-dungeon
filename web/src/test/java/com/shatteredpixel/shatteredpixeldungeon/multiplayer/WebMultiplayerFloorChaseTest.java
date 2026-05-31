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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Clipboard;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.utils.PlatformSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WebMultiplayerFloorChaseTest {

	private Application previousApp;
	private Files previousFiles;
	private Game previousGame;
	private Class<? extends Scene> previousSceneClass;
	private PlatformSupport previousPlatform;
	private String previousVersion;
	private Hero previousHero;
	private int previousDepth;
	private int previousBranch;
	private int previousSlot;
	private TestPlatform platform;

	@Before
	public void setUp() {
		previousApp = Gdx.app;
		previousFiles = Gdx.files;
		previousGame = Game.instance;
		previousSceneClass = TestGame.sceneClassForTesting();
		previousPlatform = Game.platform;
		previousVersion = Game.version;
		previousHero = Dungeon.hero;
		previousDepth = Dungeon.depth;
		previousBranch = Dungeon.branch;
		previousSlot = GamesInProgress.curSlot;

		Gdx.app = new TestApplication();
		Gdx.files = new TestFiles();
		platform = new TestPlatform();
		Game.platform = platform;
		Game.version = "test";
		Dungeon.hero = new Hero();
		WebMultiplayer.resetForTesting();
	}

	@After
	public void tearDown() {
		WebMultiplayer.resetForTesting();
		TestGame.restoreGameForTesting(previousGame, previousSceneClass);
		Game.platform = previousPlatform;
		Game.version = previousVersion;
		Dungeon.hero = previousHero;
		Dungeon.depth = previousDepth;
		Dungeon.branch = previousBranch;
		GamesInProgress.curSlot = previousSlot;
		Gdx.files = previousFiles;
		Gdx.app = previousApp;
	}

	@Test
	public void caughtFloorStaysCaughtWhenPlayerReturnsUpstairs() {
		moveLocalTo(1);
		WebMultiplayer.receiveRemoteFloor("2", 2, 0, 0);
		assertTrue(WebMultiplayer.chaseActive());
		assertEquals(2, WebMultiplayer.chaseTargetDepth());

		moveLocalTo(2);
		assertFalse(WebMultiplayer.chaseActive());
		assertEquals(0, WebMultiplayer.chaseTargetDepth());

		moveLocalTo(1);
		assertFalse(WebMultiplayer.chaseActive());
		assertEquals(0, WebMultiplayer.chaseTargetDepth());
	}

	@Test
	public void supportPromptsOnlySuppressDuringMultiplayerRoomContext() {
		platform.multiplayerEnabled = false;
		assertFalse(WebMultiplayer.suppressSupportPrompts());

		platform.multiplayerEnabled = true;
		assertTrue(WebMultiplayer.suppressSupportPrompts());
	}

	@Test
	public void chaseTargetsNextUnreachedFloorInsteadOfCurrentFloor() {
		moveLocalTo(1);
		WebMultiplayer.receiveRemoteFloor("2", 3, 0, 0);
		assertTrue(WebMultiplayer.chaseActive());
		assertEquals(2, WebMultiplayer.chaseTargetDepth());
		assertEquals(40, WebMultiplayer.chaseTurnsLeft());

		moveLocalTo(2);
		assertTrue(WebMultiplayer.chaseActive());
		assertEquals(3, WebMultiplayer.chaseTargetDepth());
		assertEquals(40, WebMultiplayer.chaseTurnsLeft());

		moveLocalTo(1);
		assertTrue(WebMultiplayer.chaseActive());
		assertEquals(3, WebMultiplayer.chaseTargetDepth());
	}

	@Test
	public void roomConfiguredFiniteChaseTurnsReplaceDefault() {
		WebMultiplayer.configureFloorChase(30, false);

		moveLocalTo(1);
		WebMultiplayer.receiveRemoteFloor("2", 2, 0, 0);

		assertTrue(WebMultiplayer.chaseActive());
		assertEquals(30, WebMultiplayer.floorChaseTurns());
		assertEquals(30, WebMultiplayer.chaseTurnsLeft());
	}

	@Test
	public void debugTurnEventAdvancesExistingFloorChaseTurns() {
		WebMultiplayer.configureFloorChase(10, false);
		moveLocalTo(1);
		WebMultiplayer.receiveRemoteFloor("2", 2, 0, 0);

		platform.nextEvent = "debug-turns|3";
		WebMultiplayer.update();

		assertEquals(3, WebMultiplayer.localTurns());
		assertTrue(WebMultiplayer.chaseActive());
		assertEquals(7, WebMultiplayer.chaseTurnsLeft());
	}

	@Test
	public void roomConfiguredInfiniteDisablesFloorChase() {
		WebMultiplayer.configureFloorChase(20, true);

		moveLocalTo(1);
		WebMultiplayer.receiveRemoteFloor("2", 3, 0, 0);

		assertTrue(WebMultiplayer.floorChaseInfinite());
		assertFalse(WebMultiplayer.chaseActive());
		assertEquals(0, WebMultiplayer.chaseTargetDepth());
		assertEquals(0, WebMultiplayer.chaseTurnsLeft());
	}

	@Test
	public void peerMirrorTracksRemoteReplayState() {
		WebMultiplayer.receiveReplayEvent("2", 1, "move", 1, 0, 42, "moved");
		WebMultiplayer.PeerMirrorState state = onlyPeerMirror();
		assertEquals("2", state.playerId);
		assertEquals(1, state.sequence);
		assertEquals(1, state.depth);
		assertEquals(0, state.branch);
		assertEquals(42, state.cell);
		assertEquals("move", state.lastKind);

		WebMultiplayer.receiveReplayEvent("2", 2, "status", 1, 0, 42, "buffs");
		state = onlyPeerMirror();
		assertEquals(2, state.sequence);
		assertEquals("status", state.lastKind);
		assertEquals("buffs", state.status);

		WebMultiplayer.receiveReplayEvent("2", 3, "transition", 2, 0, 7, "floor");
		state = onlyPeerMirror();
		assertEquals(3, state.sequence);
		assertEquals(2, state.depth);
		assertEquals(7, state.cell);
		assertEquals("transition", state.lastKind);
		assertEquals("buffs", state.status);

		WebMultiplayer.receiveReplayEvent("2", 2, "move", 1, 0, 99, "stale");
		state = onlyPeerMirror();
		assertEquals(3, state.sequence);
		assertEquals(2, state.depth);
		assertEquals(7, state.cell);
	}

	@Test
	public void peerMirrorStoresRemoteAppearanceFromStatusPayload() {
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerAppearance\":{\"heroClass\":\"HUNTRESS\",\"tier\":4},\"peerBuffs\":[]}");

		WebMultiplayer.PeerMirrorState state = onlyPeerMirror();
		assertEquals(HeroClass.HUNTRESS, state.heroClass);
		assertEquals(4, state.armorTier);

		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerAppearance\":{\"heroClass\":\"MAGE\",\"tier\":2},\"peerBuffs\":[]}");

		state = onlyPeerMirror();
		assertEquals("stale appearance payloads must not roll back mirror state", HeroClass.HUNTRESS, state.heroClass);
		assertEquals(4, state.armorTier);
	}

	@Test
	public void peerMirrorStoresRemoteIdentityAndVitalsFromStatusPayload() {
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerIdentity\":{\"name\":\"Alice\",\"color\":\"ff6b6b\"},"
						+ "\"peerVitals\":{\"hp\":7,\"ht\":20},\"peerBuffs\":[]}");

		WebMultiplayer.PeerMirrorState state = onlyPeerMirror();
		assertEquals("Alice", state.playerName);
		assertEquals(0xFF6B6B, state.playerColor);
		assertEquals(7, state.hp);
		assertEquals(20, state.ht);
	}

	@Test
	public void peerMirrorStoresRemoteBuffRecordsFromStatusPayload() {
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerBuffs\":["
						+ "{\"className\":\"haste\",\"icon\":" + BuffIndicator.HASTE
						+ ",\"type\":\"NEGATIVE\",\"text\":\"7\\\"x\",\"fade\":0.25},"
						+ "{\"className\":\"hidden\",\"icon\":" + BuffIndicator.NONE
						+ ",\"type\":\"POSITIVE\",\"text\":\"\",\"fade\":1}"
						+ "]}");

		WebMultiplayer.PeerMirrorState state = onlyPeerMirror();
		assertEquals(1, state.peerBuffs.size());
		WebMultiplayer.PeerBuffState buff = state.peerBuffs.get(0);
		assertEquals("haste", buff.className);
		assertEquals(BuffIndicator.HASTE, buff.icon);
		assertEquals(Buff.buffType.NEGATIVE, buff.type);
		assertEquals("7\"x", buff.text);
		assertEquals(0.25f, buff.fade, 0.001f);

		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42, "{\"peerBuffs\":[]}");
		state = onlyPeerMirror();
		assertEquals("stale Peer Buff payloads must not roll back mirror state", 1, state.peerBuffs.size());

		WebMultiplayer.receiveReplayEvent("2", 2, "status", 1, 0, 42, "{\"peerBuffs\":[]}");
		state = onlyPeerMirror();
		assertTrue(state.peerBuffs.isEmpty());
	}

	@Test
	public void peerStatusTracksRemoteGameOverState() {
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":true},\"peerBuffs\":[]}");

		WebMultiplayer.PeerMirrorState state = onlyPeerMirror();
		assertTrue(state.connected);
		assertTrue(state.gameOver);

		WebMultiplayer.receiveReplayEvent("2", 2, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");

		state = onlyPeerMirror();
		assertFalse(state.gameOver);
	}

	@Test
	public void peerMirrorIgnoresLocalReplayAndMarksLeftPeerIdle() {
		WebMultiplayer.receiveReplayEvent("1", 1, "move", 1, 0, 42, "local");
		assertTrue(WebMultiplayer.peerMirrors().isEmpty());

		WebMultiplayer.receiveReplayEvent("2", 1, "move", 1, 0, 42, "remote");
		assertFalse(WebMultiplayer.peerMirrors().isEmpty());

		WebMultiplayer.markPeerDisconnected("2");
		WebMultiplayer.PeerMirrorState state = onlyPeerMirror();
		assertFalse(state.connected);
		assertEquals(1, state.sequence);
		assertEquals(42, state.cell);

		WebMultiplayer.receiveReplayEvent("2", 2, "move", 1, 0, 43, "remote returned");
		state = onlyPeerMirror();
		assertFalse(state.connected);
		assertEquals(2, state.sequence);
		assertEquals(43, state.cell);
	}

	@Test
	public void peerReturnedResetsDisconnectedReplayStream() {
		WebMultiplayer.receiveReplayEvent("2", 12, "move", 1, 0, 42, "remote");
		WebMultiplayer.markPeerDisconnected("2");
		WebMultiplayer.markPeerReturned("2");

		WebMultiplayer.PeerMirrorState state = onlyPeerMirror();
		assertTrue(state.connected);
		assertEquals(0, state.sequence);

		WebMultiplayer.receiveReplayEvent("2", 1, "move", 1, 0, 43, "remote returned");
		state = onlyPeerMirror();
		assertTrue(state.connected);
		assertFalse(state.gameOver);
		assertEquals(1, state.sequence);
		assertEquals(43, state.cell);
	}

	@Test
	public void peerTimeoutMarksPeerGameOverAndPreventsLateRevival() {
		WebMultiplayer.receiveReplayEvent("2", 1, "move", 1, 0, 42, "remote");
		WebMultiplayer.markPeerDisconnected("2");
		WebMultiplayer.markPeerTimedOut("2");

		WebMultiplayer.PeerMirrorState state = onlyPeerMirror();
		assertFalse(state.connected);
		assertTrue(state.gameOver);

		WebMultiplayer.receiveReplayEvent("2", 2, "move", 1, 0, 43, "late remote");
		state = onlyPeerMirror();
		assertFalse(state.connected);
		assertTrue(state.gameOver);
		assertEquals(2, state.sequence);
		assertEquals(43, state.cell);

		WebMultiplayer.receiveReplayEvent("2", 3, "status", 1, 0, 43,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		state = onlyPeerMirror();
		assertFalse(state.connected);
		assertTrue(state.gameOver);
	}

	@Test
	public void localGameOverPublishesEliminatedStateAndHidesContinueWatchWhenOnePlayerRemains() {
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.onLocalGameOver());
		assertEquals("status", platform.lastReplayKind);
		assertTrue(platform.lastReplayMessage.contains("\"peerState\""));
		assertTrue(platform.lastReplayMessage.contains("\"gameOver\":true"));
		assertTrue(platform.lastReplayMessage.contains("\"state\":\"game-over\""));
		assertEquals(0, platform.watchReturnEligibleCount);
		assertEquals(null, platform.watchReturnEligibleReason);
		assertFalse(WebMultiplayer.canContinueToWatch());

		assertFalse(WebMultiplayer.continueToWatch());
		assertEquals(null, platform.continueWatchTarget);
	}

	@Test
	public void continueToWatchIsDisabledWhileWatcherFlowIsPaused() {
		platform.playerSeatOrder = "3,2";
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.onLocalGameOver());
		assertFalse(WebMultiplayer.canContinueToWatch());
		assertFalse(WebMultiplayer.continueToWatch());

		assertEquals(null, platform.continueWatchTarget);
	}

	@Test
	public void knownRoomWinnerKeepsContinueToWatchDisabled() {
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 43,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.onLocalGameOver());
		assertFalse(WebMultiplayer.canContinueToWatch());

		sendWinnerClaim("2", 2, 8, "winner");

		assertTrue(WebMultiplayer.roomWinnerKnown());
		assertFalse(WebMultiplayer.canContinueToWatch());
	}

	@Test
	public void outcomeStatusAcceptsWinnerWithoutReplaySequence() {
		WebMultiplayer.receiveOutcomeStatus("2",
				"{\"peerState\":{\"gameOver\":false},"
						+ "\"roomWinner\":{\"participantId\":\"2\","
						+ "\"playerName\":\"Remote 2\",\"playerColor\":\"ff6b6b\","
						+ "\"heroClass\":\"ROGUE\",\"armorTier\":3,"
						+ "\"roomEpoch\":7,\"localTurns\":8,"
						+ "\"victoryMarker\":\"amulet\",\"finishNonce\":\"reconnected\"},"
						+ "\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.roomWinnerKnown());
		assertEquals("2", WebMultiplayer.roomWinner().participantId);
	}

	@Test
	public void outcomeStatusAcceptsNoWinnerWithoutReplaySequence() {
		WebMultiplayer.receiveOutcomeStatus("2",
				"{\"peerState\":{\"gameOver\":true},\"roomNoWinner\":true,\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.roomNoWinnerKnown());
	}

	@Test
	public void recreatedRoomLaunchClearsPreviousRoomOutcome() {
		WebMultiplayer.receiveOutcomeStatus("2",
				"{\"peerState\":{\"gameOver\":true},\"roomNoWinner\":true,\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.roomNoWinnerKnown());

		WebMultiplayer.RoomLaunch launch = WebMultiplayer.RoomLaunch.fromEvent(new String[]{
				"room-launch", "testt1", "1", "player", "WARRIOR", "9c6e0f", "seed",
				"40", "false", "8", "", "Local Alice", "69db7c", "1"
		});

		assertTrue(WebMultiplayer.prepareRoomRunForLaunch(launch, GamesInProgress.TRANSIENT_MULTIPLAYER_SLOT));
		assertFalse(WebMultiplayer.roomNoWinnerKnown());
		assertFalse(WebMultiplayer.roomWinnerKnown());
	}

	@Test
	public void knownRoomNoWinnerKeepsContinueToWatchDisabled() {
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 43,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.onLocalGameOver());
		assertFalse(WebMultiplayer.canContinueToWatch());

		WebMultiplayer.receiveReplayEvent("3", 2, "status", 1, 0, 43,
				"{\"peerState\":{\"gameOver\":true},\"roomNoWinner\":true,\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.roomNoWinnerKnown());
		assertFalse(WebMultiplayer.canContinueToWatch());
	}

	@Test
	public void knownRoomWinnerDoesNotSwitchScenes() {
		TestGame game = new TestGame();
		game.readyForOutcomeSwitch();
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		sendWinnerClaim("2", 2, 8, "winner");

		assertTrue(WebMultiplayer.roomWinnerKnown());
		assertFalse(WebMultiplayer.showRoomOutcomeIfKnown());
		assertEquals(PlainScene.class, TestGame.sceneClassForTesting());
		assertFalse(game.sceneSwitchRequested());
	}

	@Test
	public void knownRoomNoWinnerDoesNotSwitchScenes() {
		TestGame game = new TestGame();
		game.readyForOutcomeSwitch();
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":true},\"roomNoWinner\":true,\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.roomNoWinnerKnown());
		assertFalse(WebMultiplayer.showRoomOutcomeIfKnown());
		assertEquals(PlainScene.class, TestGame.sceneClassForTesting());
		assertFalse(game.sceneSwitchRequested());
	}

	@Test
	public void noWinnerWhenAllPlayersEliminatedWithoutWinner() {
		platform.playerSeatOrder = "1,2";
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":true},\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.onLocalGameOver());
		WebMultiplayer.update();

		assertTrue(WebMultiplayer.roomNoWinnerKnown());
		assertFalse(WebMultiplayer.roomWinnerKnown());
		assertFalse(WebMultiplayer.canContinueToWatch());
		WebMultiplayer.publishPeerBuffStatusIfChanged();
		assertTrue(platform.lastReplayMessage.contains("\"roomNoWinner\":true"));
	}

	@Test
	public void remoteNoWinnerStatusMarksRoomNoWinner() {
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":true},\"roomNoWinner\":true,\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.roomNoWinnerKnown());
		assertFalse(WebMultiplayer.roomWinnerKnown());
	}

	@Test
	public void disconnectedPeerInGracePreventsNoWinner() {
		platform.playerSeatOrder = "1,2";
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		WebMultiplayer.markPeerDisconnected("2");

		assertTrue(WebMultiplayer.onLocalGameOver());
		WebMultiplayer.update();

		assertFalse(WebMultiplayer.roomNoWinnerKnown());
	}

	@Test
	public void watcherMarksNoWinnerAfterAllSeatPlayersTimeOut() {
		platform.watcher = true;
		platform.playerSeatOrder = "2,3";

		WebMultiplayer.markPeerTimedOut("2");
		WebMultiplayer.markPeerTimedOut("3");
		WebMultiplayer.update();

		assertTrue(WebMultiplayer.roomNoWinnerKnown());
		assertFalse(WebMultiplayer.roomWinnerKnown());
	}

	@Test
	public void localVictoryClaimPublishesRoomWinnerState() {
		Dungeon.hero.heroClass = HeroClass.CLERIC;
		Dungeon.hero.HP = 12;
		Dungeon.hero.HT = 20;
		Dungeon.hero.pos = 42;

		assertTrue(WebMultiplayer.claimLocalVictory("amulet"));

		WebMultiplayer.RoomWinnerState winner = WebMultiplayer.roomWinner();
		assertNotNull(winner);
		assertEquals("1", winner.participantId);
		assertEquals("Local Alice", winner.playerName);
		assertEquals(0x69DB7C, winner.playerColor);
		assertEquals(HeroClass.CLERIC, winner.heroClass);
		assertEquals(7, winner.roomEpoch);
		assertEquals("amulet", winner.victoryMarker);
		assertFalse(winner.finishNonce.isEmpty());
		assertEquals("status", platform.lastReplayKind);
		assertTrue(platform.lastReplayMessage.contains("\"roomWinner\""));
		assertTrue(platform.lastReplayMessage.contains("\"participantId\":\"1\""));
		assertTrue(platform.lastReplayMessage.contains("\"playerName\":\"Local Alice\""));
		assertTrue(platform.lastReplayMessage.contains("\"playerColor\":\"69db7c\""));
		assertTrue(platform.lastReplayMessage.contains("\"heroClass\":\"CLERIC\""));
		assertTrue(platform.lastReplayMessage.contains("\"roomEpoch\":7"));
		assertTrue(platform.lastReplayMessage.contains("\"victoryMarker\":\"amulet\""));
	}

	@Test
	public void localVictoryClaimRequiresLivePlayer() {
		Dungeon.hero.HP = 0;

		assertFalse(WebMultiplayer.claimLocalVictory("amulet"));
		assertFalse(WebMultiplayer.roomWinnerKnown());
		assertEquals(0, platform.replayCount);
	}

	@Test
	public void eliminatedLocalPlayerCannotClaimVictory() {
		Dungeon.hero.HP = 12;

		assertTrue(WebMultiplayer.onLocalGameOver());
		int replayCount = platform.replayCount;

		assertFalse(WebMultiplayer.claimLocalVictory("amulet"));
		assertFalse(WebMultiplayer.roomWinnerKnown());
		assertEquals(replayCount, platform.replayCount);
	}

	@Test
	public void victoryMainMenuDisconnectsWithoutPublishingElimination() {
		Dungeon.hero.heroClass = HeroClass.CLERIC;
		Dungeon.hero.pos = 42;
		assertTrue(WebMultiplayer.claimLocalVictory("amulet"));
		int replayCount = platform.replayCount;

		WebMultiplayer.leaveRoomAfterVictory();

		assertEquals(1, platform.disconnectCount);
		assertFalse(WebMultiplayer.roomWinnerKnown());
		assertEquals(replayCount, platform.replayCount);
	}

	@Test
	public void lastLiveConnectedPlayerClaimsRoomWinnerAfterOthersEliminated() {
		platform.playerSeatOrder = "1,2,3";
		Dungeon.hero.heroClass = HeroClass.MAGE;
		Dungeon.hero.HP = 10;
		Dungeon.hero.HT = 20;
		Dungeon.hero.pos = 42;
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":true},\"peerBuffs\":[]}");
		WebMultiplayer.markPeerTimedOut("3");

		WebMultiplayer.update();

		assertTrue(WebMultiplayer.roomWinnerKnown());
		WebMultiplayer.RoomWinnerState winner = WebMultiplayer.roomWinner();
		assertNotNull(winner);
		assertEquals("1", winner.participantId);
		assertEquals("last-player-standing", winner.victoryMarker);
		assertEquals("status", platform.lastReplayKind);
		assertTrue(platform.lastReplayMessage.contains("\"victoryMarker\":\"last-player-standing\""));
	}

	@Test
	public void disconnectedPeerInGracePreventsLastPlayerStandingClaim() {
		platform.playerSeatOrder = "1,2";
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		WebMultiplayer.markPeerDisconnected("2");

		WebMultiplayer.update();

		assertFalse(WebMultiplayer.roomWinnerKnown());
	}

	@Test
	public void remoteVictoryClaimRequiresCurrentEpochAndLivePeer() {
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},"
						+ "\"roomWinner\":{\"participantId\":\"2\",\"playerName\":\"Remote Bob\","
						+ "\"playerColor\":\"ff6b6b\",\"heroClass\":\"ROGUE\",\"armorTier\":3,"
						+ "\"roomEpoch\":6,\"localTurns\":8,\"victoryMarker\":\"amulet\","
						+ "\"finishNonce\":\"old\"},\"peerBuffs\":[]}");
		assertFalse(WebMultiplayer.roomWinnerKnown());

		WebMultiplayer.receiveReplayEvent("2", 2, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},"
						+ "\"roomWinner\":{\"participantId\":\"2\",\"playerName\":\"Remote Bob\","
						+ "\"playerColor\":\"ff6b6b\",\"heroClass\":\"ROGUE\",\"armorTier\":3,"
						+ "\"roomEpoch\":7,\"localTurns\":8,\"victoryMarker\":\"amulet\","
						+ "\"finishNonce\":\"current\"},\"peerBuffs\":[]}");

		WebMultiplayer.RoomWinnerState winner = WebMultiplayer.roomWinner();
		assertNotNull(winner);
		assertEquals("2", winner.participantId);
		assertEquals("Remote Bob", winner.playerName);
		assertEquals(0xFF6B6B, winner.playerColor);
		assertEquals(HeroClass.ROGUE, winner.heroClass);
		assertEquals(3, winner.armorTier);
		assertEquals(7, winner.roomEpoch);
		assertEquals(8, winner.localTurns);
	}

	@Test
	public void lowerTurnVictoryClaimWinsIndependentOfReceiveOrder() {
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 43,
				"{\"peerState\":{\"gameOver\":false},"
						+ "\"roomWinner\":{\"participantId\":\"3\",\"playerName\":\"Remote Three\","
						+ "\"playerColor\":\"64b5f6\",\"heroClass\":\"MAGE\",\"armorTier\":2,"
						+ "\"roomEpoch\":7,\"localTurns\":12,\"victoryMarker\":\"amulet\","
						+ "\"finishNonce\":\"late\"},\"peerBuffs\":[]}");
		assertEquals("3", WebMultiplayer.roomWinner().participantId);

		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},"
						+ "\"roomWinner\":{\"participantId\":\"2\",\"playerName\":\"Remote Two\","
						+ "\"playerColor\":\"ff6b6b\",\"heroClass\":\"ROGUE\",\"armorTier\":3,"
						+ "\"roomEpoch\":7,\"localTurns\":5,\"victoryMarker\":\"amulet\","
						+ "\"finishNonce\":\"early\"},\"peerBuffs\":[]}");

		WebMultiplayer.RoomWinnerState winner = WebMultiplayer.roomWinner();
		assertNotNull(winner);
		assertEquals("2", winner.participantId);
		assertEquals(5, winner.localTurns);
	}

	@Test
	public void worseVictoryClaimDoesNotReplaceEstablishedWinner() {
		sendWinnerClaim("2", 1, 5, "winner");
		assertEquals("2", WebMultiplayer.roomWinner().participantId);

		sendWinnerClaim("3", 1, 12, "late");

		WebMultiplayer.RoomWinnerState winner = WebMultiplayer.roomWinner();
		assertNotNull(winner);
		assertEquals("2", winner.participantId);
		assertEquals(5, winner.localTurns);
	}

	@Test
	public void equalTurnVictoryClaimUsesDeterministicHashTieBreak() {
		String firstParticipant = "2";
		String firstNonce = "same-turn-one";
		String secondParticipant = "3";
		String secondNonce = "same-turn-two";
		String expectedWinner = roomWinnerTieBreakKey(firstParticipant, firstNonce)
				.compareTo(roomWinnerTieBreakKey(secondParticipant, secondNonce)) <= 0
				? firstParticipant
				: secondParticipant;

		sendWinnerClaim(firstParticipant, 1, 8, firstNonce);
		sendWinnerClaim(secondParticipant, 1, 8, secondNonce);

		WebMultiplayer.RoomWinnerState winner = WebMultiplayer.roomWinner();
		assertNotNull(winner);
		assertEquals(expectedWinner, winner.participantId);
		assertEquals(8, winner.localTurns);
	}

	@Test
	public void timedOutPeerCannotClaimVictoryUntilLiveAgain() {
		WebMultiplayer.receiveReplayEvent("2", 1, "move", 1, 0, 42, "remote");
		WebMultiplayer.markPeerTimedOut("2");

		WebMultiplayer.receiveReplayEvent("2", 2, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},"
						+ "\"roomWinner\":{\"participantId\":\"2\",\"playerName\":\"Remote Bob\","
						+ "\"playerColor\":\"ff6b6b\",\"heroClass\":\"ROGUE\",\"armorTier\":3,"
						+ "\"roomEpoch\":7,\"localTurns\":8,\"victoryMarker\":\"amulet\","
						+ "\"finishNonce\":\"timeout\"},\"peerBuffs\":[]}");

		assertFalse(WebMultiplayer.roomWinnerKnown());
	}

	@Test
	public void disconnectedPeerReplayDoesNotReconnectOrClaimVictory() {
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		WebMultiplayer.markPeerDisconnected("2");

		WebMultiplayer.receiveReplayEvent("2", 2, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},"
						+ "\"roomWinner\":{\"participantId\":\"2\",\"playerName\":\"Remote Bob\","
						+ "\"playerColor\":\"ff6b6b\",\"heroClass\":\"ROGUE\",\"armorTier\":3,"
						+ "\"roomEpoch\":7,\"localTurns\":8,\"victoryMarker\":\"amulet\","
						+ "\"finishNonce\":\"disconnected\"},\"peerBuffs\":[]}");

		assertFalse(WebMultiplayer.roomWinnerKnown());
		assertFalse(onlyPeerMirror().connected);
	}

	@Test
	public void returnedPeerCanResendVictoryClaim() {
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		WebMultiplayer.markPeerDisconnected("2");
		WebMultiplayer.markPeerReturned("2");

		WebMultiplayer.receiveReplayEvent("2", 2, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},"
						+ "\"roomWinner\":{\"participantId\":\"2\",\"playerName\":\"Remote Bob\","
						+ "\"playerColor\":\"ff6b6b\",\"heroClass\":\"ROGUE\",\"armorTier\":3,"
						+ "\"roomEpoch\":7,\"localTurns\":8,\"victoryMarker\":\"amulet\","
						+ "\"finishNonce\":\"returned\"},\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.roomWinnerKnown());
		assertTrue(onlyPeerMirror().connected);
	}

	@Test
	public void intentionalActiveLeavePublishesLeftStateAndDisconnectsRuntime() {
		assertTrue(WebMultiplayer.leaveActiveGameIntentionally());

		assertEquals("status", platform.lastReplayKind);
		assertTrue(platform.lastReplayMessage.contains("\"gameOver\":true"));
		assertTrue(platform.lastReplayMessage.contains("\"state\":\"left\""));
		assertEquals(0, platform.watchReturnEligibleCount);
		assertEquals(null, platform.watchReturnEligibleReason);
		assertEquals(1, platform.disconnectCount);
	}

	@Test
	public void peerBuffStatusPayloadPublishesIconBearingBuffsOnly() {
		Dungeon.depth = 2;
		Dungeon.branch = 0;
		Dungeon.hero.pos = 42;
		Dungeon.hero.heroClass = HeroClass.MAGE;
		Dungeon.hero.HP = 9;
		Dungeon.hero.HT = 20;
		new PeerMirrorTestBuff().attachTo(Dungeon.hero);
		new HiddenTestBuff().attachTo(Dungeon.hero);

		WebMultiplayer.publishPeerBuffStatusIfChanged();

		assertEquals(1, platform.replayCount);
		assertEquals("status", platform.lastReplayKind);
		assertEquals(2, platform.lastReplayDepth);
		assertEquals(0, platform.lastReplayBranch);
		assertEquals(42, platform.lastReplayCell);
		assertTrue(platform.lastReplayMessage.contains("\"peerAppearance\""));
		assertTrue(platform.lastReplayMessage.contains("\"peerIdentity\""));
		assertTrue(platform.lastReplayMessage.contains("\"name\":\"Local Alice\""));
		assertTrue(platform.lastReplayMessage.contains("\"color\":\"69db7c\""));
		assertTrue(platform.lastReplayMessage.contains("\"heroClass\":\"MAGE\""));
		assertTrue(platform.lastReplayMessage.contains("\"tier\":0"));
		assertTrue(platform.lastReplayMessage.contains("\"peerState\""));
		assertTrue(platform.lastReplayMessage.contains("\"gameOver\":false"));
		assertTrue(platform.lastReplayMessage.contains("\"peerVitals\""));
		assertTrue(platform.lastReplayMessage.contains("\"hp\":9"));
		assertTrue(platform.lastReplayMessage.contains("\"ht\":20"));
		assertTrue(platform.lastReplayMessage.contains("\"peerBuffs\""));
		assertTrue(platform.lastReplayMessage.contains(PeerMirrorTestBuff.class.getName()));
		assertTrue(platform.lastReplayMessage.contains("\"icon\":" + BuffIndicator.HASTE));
		assertTrue(platform.lastReplayMessage.contains("\"type\":\"NEGATIVE\""));
		assertTrue(platform.lastReplayMessage.contains("\"text\":\"7\\\"x\""));
		assertTrue(platform.lastReplayMessage.contains("\"fade\":0.25"));
		assertFalse(platform.lastReplayMessage.contains(HiddenTestBuff.class.getName()));

		WebMultiplayer.publishPeerBuffStatusIfChanged();
		assertEquals(1, platform.replayCount);
	}

	@Test
	public void missingWatcherResumeRequestsFreshKeyframe() {
		assertFalse(WebMultiplayer.resumeWatcherAfterRequested(false));
		assertEquals(1, platform.keyframeRequests);
	}

	@Test
	public void watcherSlotRemainsKnownAfterRuntimeCleanupDisablesTransport() {
		platform.multiplayerEnabled = false;
		platform.watcher = true;
		GamesInProgress.curSlot = GamesInProgress.WATCHER_SLOT;

		assertTrue(WebMultiplayer.watcherSlot());
		assertFalse(WebMultiplayer.watcherView());
	}

	@Test
	public void watcherKeyframeRequestFlushesPendingBrowserSnapshotAfterJavaPoll() {
		Dungeon.hero = null;
		platform.nextEvent = "watch-kf-req|watcher-2|request-9";

		WebMultiplayer.update();

		assertEquals(1, platform.sentWatcherKeyframeCount);
		assertEquals("watcher-2", platform.sentWatcherKeyframeWatcherId);
		assertEquals("request-9", platform.sentWatcherKeyframeRequestId);
	}

	@Test
	public void multiplayerJournalAvailabilityTreatsPagesAsReadWithoutMutating() {
		assertTrue(Document.ADVENTURERS_GUIDE.isPageFound(Document.GUIDE_SEARCHING));
		assertTrue(Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_SEARCHING));
		assertTrue(Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_SURPRISE_ATKS));
		assertTrue(Document.ADVENTURERS_GUIDE.anyPagesFound());
		assertTrue(Document.ADVENTURERS_GUIDE.allPagesFound());
		assertFalse(Document.ADVENTURERS_GUIDE.isPageFoundInProgress(Document.GUIDE_SEARCHING));
		assertFalse(Document.ADVENTURERS_GUIDE.isPageFound("missing"));
		assertFalse(Document.ADVENTURERS_GUIDE.findPage(Document.GUIDE_SEARCHING));
		assertFalse(Document.ADVENTURERS_GUIDE.readPage(Document.GUIDE_SEARCHING));
	}

	@Test
	public void playerListRowsIncludeLocalAndRemoteStatus() {
		Dungeon.hero.heroClass = HeroClass.MAGE;
		Dungeon.hero.HP = 11;
		Dungeon.hero.HT = 20;
		new PeerMirrorTestBuff().attachTo(Dungeon.hero);
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerIdentity\":{\"name\":\"Remote Bob\",\"color\":\"ff6b6b\"},"
						+ "\"peerAppearance\":{\"heroClass\":\"ROGUE\",\"tier\":3},"
						+ "\"peerVitals\":{\"hp\":7,\"ht\":18},"
						+ "\"peerBuffs\":[{\"className\":\"haste\",\"icon\":" + BuffIndicator.HASTE
						+ ",\"type\":\"POSITIVE\",\"text\":\"\",\"fade\":0.5}]}");

		assertEquals(2, WebMultiplayer.playerListRows().size());
		WebMultiplayer.PlayerListRow local = WebMultiplayer.playerListRows().get(0);
		WebMultiplayer.PlayerListRow remote = WebMultiplayer.playerListRows().get(1);
		assertTrue(local.localPlayer);
		assertEquals("1", local.playerId);
		assertEquals("Local Alice", local.playerName);
		assertEquals(0x69DB7C, local.playerColor);
		assertEquals(HeroClass.MAGE, local.heroClass);
		assertEquals(11, local.hp);
		assertEquals(20, local.ht);
		assertEquals(1, local.peerBuffs.size());
		assertFalse(local.selectable);

		assertFalse(remote.localPlayer);
		assertEquals("2", remote.playerId);
		assertEquals("Remote Bob", remote.playerName);
		assertEquals(0xFF6B6B, remote.playerColor);
		assertEquals(HeroClass.ROGUE, remote.heroClass);
		assertEquals(3, remote.armorTier);
		assertEquals(7, remote.hp);
		assertEquals(18, remote.ht);
		assertEquals(1, remote.peerBuffs.size());
		assertTrue(remote.live());
	}

	@Test
	public void playerListRowsUseSeatOrderAcrossLocalAndRemotePlayers() {
		platform.playerSeatOrder = "3,1,2";
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerIdentity\":{\"name\":\"Remote Two\",\"color\":\"ff6b6b\"},"
						+ "\"peerVitals\":{\"hp\":7,\"ht\":18},\"peerBuffs\":[]}");
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 43,
				"{\"peerIdentity\":{\"name\":\"Remote Three\",\"color\":\"64b5f6\"},"
						+ "\"peerVitals\":{\"hp\":8,\"ht\":18},\"peerBuffs\":[]}");

		assertEquals(3, WebMultiplayer.playerListRows().size());
		assertEquals("3", WebMultiplayer.playerListRows().get(0).playerId);
		assertEquals("1", WebMultiplayer.playerListRows().get(1).playerId);
		assertEquals("2", WebMultiplayer.playerListRows().get(2).playerId);
	}

	@Test
	public void watcherPlayerListRowsTrackWatchTargetWithoutLocalWatcherRow() {
		platform.watcher = true;
		platform.watchTargetId = "2";
		platform.playerSeatOrder = "3,2";
		GamesInProgress.curSlot = GamesInProgress.WATCHER_SLOT;
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerIdentity\":{\"name\":\"Remote Bob\",\"color\":\"ff6b6b\"},"
						+ "\"peerAppearance\":{\"heroClass\":\"ROGUE\",\"tier\":3},"
						+ "\"peerVitals\":{\"hp\":7,\"ht\":18},\"peerBuffs\":[]}");
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 43,
				"{\"peerIdentity\":{\"name\":\"Remote Three\",\"color\":\"64b5f6\"},"
						+ "\"peerVitals\":{\"hp\":8,\"ht\":18},\"peerBuffs\":[]}");

		assertEquals(2, WebMultiplayer.playerListRows().size());
		WebMultiplayer.PlayerListRow other = WebMultiplayer.playerListRows().get(0);
		WebMultiplayer.PlayerListRow row = WebMultiplayer.playerListRows().get(1);
		assertEquals("3", other.playerId);
		assertFalse(other.watchTarget);
		assertTrue(other.selectable);
		assertFalse(row.localPlayer);
		assertTrue(row.watchTarget);
		assertFalse(row.selectable);
		assertEquals("2", row.playerId);
		assertEquals("Remote Bob", row.playerName);
		assertEquals(7, row.hp);
		assertEquals(18, row.ht);
	}

	@Test
	public void watcherMoveReplayMovesRestoredWatchTargetHero() {
		platform.watcher = true;
		platform.watchTargetId = "2";
		GamesInProgress.curSlot = GamesInProgress.WATCHER_SLOT;
		Dungeon.depth = 1;
		Dungeon.branch = 0;
		Dungeon.hero.pos = 10;

		WebMultiplayer.receiveReplayEvent("2", 1, "move", 1, 0, 42, "Watch Target moved.");

		assertEquals(42, Dungeon.hero.pos);

		WebMultiplayer.receiveReplayEvent("2", 1, "move", 1, 0, 99, "Duplicate move.");

		assertEquals(42, Dungeon.hero.pos);
	}

	@Test
	public void currentWatchTargetRowStaysSelectedWhenNonLive() {
		platform.watcher = true;
		platform.watchTargetId = "2";
		platform.playerSeatOrder = "2,3";
		GamesInProgress.curSlot = GamesInProgress.WATCHER_SLOT;
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerIdentity\":{\"name\":\"Remote Bob\",\"color\":\"ff6b6b\"},"
						+ "\"peerVitals\":{\"hp\":7,\"ht\":18},\"peerBuffs\":[]}");
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 43,
				"{\"peerIdentity\":{\"name\":\"Remote Three\",\"color\":\"64b5f6\"},"
						+ "\"peerVitals\":{\"hp\":8,\"ht\":18},\"peerBuffs\":[]}");

		WebMultiplayer.markPeerDisconnected("2");

		WebMultiplayer.PlayerListRow disconnectedTarget = playerListRow("2");
		assertTrue(disconnectedTarget.watchTarget);
		assertFalse(disconnectedTarget.selectable);
		assertFalse(disconnectedTarget.live());
		assertFalse(disconnectedTarget.gameOver);
		assertFalse(disconnectedTarget.timedOut);
		assertTrue(playerListRow("3").selectable);

		WebMultiplayer.markPeerTimedOut("2");

		WebMultiplayer.PlayerListRow timedOutTarget = playerListRow("2");
		assertTrue(timedOutTarget.watchTarget);
		assertFalse(timedOutTarget.selectable);
		assertFalse(timedOutTarget.live());
		assertTrue(timedOutTarget.gameOver);
		assertTrue(timedOutTarget.timedOut);
	}

	@Test
	public void playerListRowSelectionDispatchesOnlySelectableRows() {
		platform.watcher = true;
		platform.watchTargetId = "2";
		GamesInProgress.curSlot = GamesInProgress.WATCHER_SLOT;
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 43,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");

		assertFalse(WebMultiplayer.selectPlayerListRow(playerListRow("2")));
		assertEquals(0, platform.switchTargetCount);

		assertTrue(WebMultiplayer.selectPlayerListRow(playerListRow("3")));
		assertEquals("3", platform.switchedWatchTarget);
		assertEquals(1, platform.switchTargetCount);

		WebMultiplayer.markPeerTimedOut("2");

		assertFalse(WebMultiplayer.selectPlayerListRow(playerListRow("2")));
		assertFalse(WebMultiplayer.selectPlayerListRow(null));
		assertEquals(1, platform.switchTargetCount);
	}

	@Test
	public void switchWatchTargetOnlyDispatchesLiveWatcherTargets() {
		platform.watcher = true;
		platform.watchTargetId = "2";
		GamesInProgress.curSlot = GamesInProgress.WATCHER_SLOT;
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 43,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.switchWatchTarget("3"));
		assertEquals("3", platform.switchedWatchTarget);
		assertEquals(1, platform.switchTargetCount);

		WebMultiplayer.markPeerTimedOut("2");

		assertFalse(WebMultiplayer.switchWatchTarget("2"));
		assertFalse(WebMultiplayer.switchWatchTarget("missing"));
		assertFalse(WebMultiplayer.switchWatchTarget("3"));
		assertEquals(1, platform.switchTargetCount);
	}

	@Test
	public void playerListRowsStopSelectableTargetsAfterRoomWinnerKnown() {
		platform.watcher = true;
		platform.watchTargetId = "2";
		GamesInProgress.curSlot = GamesInProgress.WATCHER_SLOT;
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 43,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		assertTrue(WebMultiplayer.playerListRows().get(1).selectable);

		WebMultiplayer.receiveReplayEvent("2", 2, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},"
						+ "\"roomWinner\":{\"participantId\":\"2\",\"playerName\":\"Remote Bob\","
						+ "\"playerColor\":\"ff6b6b\",\"heroClass\":\"ROGUE\",\"armorTier\":3,"
						+ "\"roomEpoch\":7,\"localTurns\":8,\"victoryMarker\":\"amulet\","
						+ "\"finishNonce\":\"winner\"},\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.roomWinnerKnown());
		for (WebMultiplayer.PlayerListRow row : WebMultiplayer.playerListRows()) {
			assertFalse(row.selectable);
		}
	}

	@Test
	public void playerListRowsStopSelectableTargetsAfterRoomNoWinnerKnown() {
		platform.watcher = true;
		platform.watchTargetId = "2";
		GamesInProgress.curSlot = GamesInProgress.WATCHER_SLOT;
		WebMultiplayer.receiveReplayEvent("2", 1, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		WebMultiplayer.receiveReplayEvent("3", 1, "status", 1, 0, 43,
				"{\"peerState\":{\"gameOver\":false},\"peerBuffs\":[]}");
		assertTrue(playerListRow("3").selectable);

		WebMultiplayer.receiveReplayEvent("4", 1, "status", 1, 0, 44,
				"{\"peerState\":{\"gameOver\":true},\"roomNoWinner\":true,\"peerBuffs\":[]}");

		assertTrue(WebMultiplayer.roomNoWinnerKnown());
		for (WebMultiplayer.PlayerListRow row : WebMultiplayer.playerListRows()) {
			assertFalse(row.selectable);
		}
	}

	private static void moveLocalTo(int depth) {
		Dungeon.depth = depth;
		Dungeon.branch = 0;
		WebMultiplayer.onLocalFloorChanged();
	}

	private static WebMultiplayer.PeerMirrorState onlyPeerMirror() {
		assertEquals(1, WebMultiplayer.peerMirrors().size());
		WebMultiplayer.PeerMirrorState state = WebMultiplayer.peerMirrors().get(0);
		assertNotNull(state);
		return state;
	}

	private static WebMultiplayer.PlayerListRow playerListRow(String playerId) {
		WebMultiplayer.PlayerListRow result = null;
		for (WebMultiplayer.PlayerListRow row : WebMultiplayer.playerListRows()) {
			if (row.playerId.equals(playerId)) {
				result = row;
				break;
			}
		}
		assertNotNull(result);
		return result;
	}

	private void sendWinnerClaim(String participantId, int sequence, int localTurns, String finishNonce) {
		WebMultiplayer.receiveReplayEvent(participantId, sequence, "status", 1, 0, 42,
				"{\"peerState\":{\"gameOver\":false},"
						+ "\"roomWinner\":{\"participantId\":\"" + participantId
						+ "\",\"playerName\":\"Remote " + participantId
						+ "\",\"playerColor\":\"ff6b6b\",\"heroClass\":\"ROGUE\",\"armorTier\":3,"
						+ "\"roomEpoch\":7,\"localTurns\":" + localTurns
						+ ",\"victoryMarker\":\"amulet\",\"finishNonce\":\"" + finishNonce
						+ "\"},\"peerBuffs\":[]}");
	}

	private static String roomWinnerTieBreakKey(String participantId, String finishNonce) {
		String material = Dungeon.seed + "|" + participantId + "|" + finishNonce;
		long hash = 0xcbf29ce484222325L;
		for (int i = 0; i < material.length(); i++) {
			hash ^= material.charAt(i);
			hash *= 0x100000001b3L;
		}
		String hex = Long.toHexString(hash);
		return "0000000000000000".substring(Math.min(hex.length(), 16)) + hex;
	}

	private static class TestPlatform extends PlatformSupport {
		@Override
		public void updateDisplaySize() {
		}

		@Override
		public void updateSystemUI() {
		}

		@Override
		public boolean connectedToUnmeteredNetwork() {
			return true;
		}

		@Override
		public boolean supportsVibration() {
			return false;
		}

		@Override
		public void setupFontGenerators(int pageSize, boolean systemFont) {
		}

		@Override
		protected FreeTypeFontGenerator getGeneratorForString(String input) {
			return null;
		}

		@Override
		public String[] splitforTextBlock(String text, boolean multiline) {
			return new String[]{text};
		}

		@Override
		public boolean multiplayerEnabled() {
			return multiplayerEnabled;
		}

		@Override
		public String multiplayerPlayerId() {
			return "1";
		}

		@Override
		public String multiplayerPlayerName() {
			return "Local Alice";
		}

		@Override
		public String multiplayerPlayerColor() {
			return "#69DB7C";
		}

		@Override
		public boolean multiplayerWatcher() {
			return watcher;
		}

		@Override
		public String multiplayerWatchTargetId() {
			return watchTargetId;
		}

		@Override
		public String multiplayerPlayerSeatOrder() {
			return playerSeatOrder;
		}

		@Override
		public int multiplayerRoomEpoch() {
			return roomEpoch;
		}

		@Override
		public String pollMultiplayerEvent() {
			String event = nextEvent;
			nextEvent = null;
			return event;
		}

		@Override
		public void announceMultiplayerReplayEvent(String kind, int depth, int branch, int cell, String message) {
			replayCount++;
			lastReplayKind = kind;
			lastReplayDepth = depth;
			lastReplayBranch = branch;
			lastReplayCell = cell;
			lastReplayMessage = message;
		}

		@Override
		public void requestMultiplayerWatcherKeyframe() {
			keyframeRequests++;
		}

		@Override
		public void sendMultiplayerWatcherKeyframe(String watcherId, String requestId) {
			sentWatcherKeyframeCount++;
			sentWatcherKeyframeWatcherId = watcherId;
			sentWatcherKeyframeRequestId = requestId;
		}

		private int replayCount;
		private String lastReplayKind;
		private int lastReplayDepth;
		private int lastReplayBranch;
		private int lastReplayCell;
		private String lastReplayMessage;
		private int keyframeRequests;
		private int sentWatcherKeyframeCount;
		private String sentWatcherKeyframeWatcherId;
		private String sentWatcherKeyframeRequestId;
		private String continueWatchTarget;
		private int disconnectCount;
		private int watchReturnEligibleCount;
		private String watchReturnEligibleReason;
		private String switchedWatchTarget;
		private int switchTargetCount;
		private boolean multiplayerEnabled = true;
		private boolean watcher;
		private String watchTargetId = "";
		private String playerSeatOrder = "";
		private int roomEpoch = 7;
		private String nextEvent;

		@Override
		public boolean continueMultiplayerAsWatcher(String targetId) {
			continueWatchTarget = targetId;
			return true;
		}

		@Override
		public void markMultiplayerWatchReturnEligible(String reason) {
			watchReturnEligibleCount++;
			watchReturnEligibleReason = reason;
		}

		@Override
		public void disconnectActiveMultiplayerGame() {
			disconnectCount++;
		}

		@Override
		public void switchMultiplayerWatchTarget(String targetId) {
			switchTargetCount++;
			switchedWatchTarget = targetId;
			watchTargetId = targetId;
		}
	}

	private static class PeerMirrorTestBuff extends Buff {
		PeerMirrorTestBuff() {
			type = buffType.NEGATIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.HASTE;
		}

		@Override
		public float iconFadePercent() {
			return 0.25f;
		}

		@Override
		public String iconTextDisplay() {
			return "7\"x";
		}
	}

	private static class HiddenTestBuff extends Buff {
	}

	private static class TestGame extends Game {
		TestGame() {
			super(PlainScene.class, Game.platform);
		}

		void readyForOutcomeSwitch() {
			scene = new PlainScene();
			requestedReset = false;
			sceneSwitchInProgress = false;
			requestedScene = null;
		}

		boolean sceneSwitchRequested() {
			return requestedReset;
		}

		static Class<? extends Scene> sceneClassForTesting() {
			return sceneClass;
		}

		static void restoreGameForTesting(Game game, Class<? extends Scene> restoredSceneClass) {
			instance = game;
			sceneClass = restoredSceneClass;
		}
	}

	public static class PlainScene extends Scene {
	}

	private static class TestFiles implements Files {
		private final File assetsRoot;

		TestFiles() {
			File root = new File("core/src/main/assets");
			if (!root.exists()) {
				root = new File("../core/src/main/assets");
			}
			assetsRoot = root;
		}

		@Override
		public FileHandle getFileHandle(String path, FileType type) {
			if (type == FileType.Internal || type == FileType.Classpath) {
				return new FileHandle(new File(assetsRoot, path));
			}
			return new FileHandle(path);
		}

		@Override public FileHandle classpath(String path) { return getFileHandle(path, FileType.Classpath); }
		@Override public FileHandle internal(String path) { return getFileHandle(path, FileType.Internal); }
		@Override public FileHandle external(String path) { return getFileHandle(path, FileType.External); }
		@Override public FileHandle absolute(String path) { return getFileHandle(path, FileType.Absolute); }
		@Override public FileHandle local(String path) { return getFileHandle(path, FileType.Local); }
		@Override public String getExternalStoragePath() { return ""; }
		@Override public boolean isExternalStorageAvailable() { return true; }
		@Override public String getLocalStoragePath() { return ""; }
		@Override public boolean isLocalStorageAvailable() { return true; }
	}

	private static class TestApplication implements Application {
		private final Map<String, Preferences> preferences = new HashMap<>();

		@Override public ApplicationListener getApplicationListener() { return null; }
		@Override public Graphics getGraphics() { return null; }
		@Override public Audio getAudio() { return null; }
		@Override public Input getInput() { return null; }
		@Override public Files getFiles() { return null; }
		@Override public Net getNet() { return null; }
		@Override public void log(String tag, String message) { }
		@Override public void log(String tag, String message, Throwable exception) { }
		@Override public void error(String tag, String message) { }
		@Override public void error(String tag, String message, Throwable exception) { }
		@Override public void debug(String tag, String message) { }
		@Override public void debug(String tag, String message, Throwable exception) { }
		@Override public void setLogLevel(int logLevel) { }
		@Override public int getLogLevel() { return LOG_NONE; }
		@Override public void setApplicationLogger(ApplicationLogger applicationLogger) { }
		@Override public ApplicationLogger getApplicationLogger() { return null; }
		@Override public ApplicationType getType() { return ApplicationType.WebGL; }
		@Override public int getVersion() { return 0; }
		@Override public long getJavaHeap() { return 0; }
		@Override public long getNativeHeap() { return 0; }
		@Override public Preferences getPreferences(String name) {
			return preferences.computeIfAbsent(name, ignored -> new MemoryPreferences());
		}
		@Override public Clipboard getClipboard() { return null; }
		@Override public void postRunnable(Runnable runnable) { runnable.run(); }
		@Override public void exit() { }
		@Override public void addLifecycleListener(LifecycleListener listener) { }
		@Override public void removeLifecycleListener(LifecycleListener listener) { }
	}

	private static class MemoryPreferences implements Preferences {
		private final Map<String, Object> values = new HashMap<>();

		@Override public Preferences putBoolean(String key, boolean val) { values.put(key, val); return this; }
		@Override public Preferences putInteger(String key, int val) { values.put(key, val); return this; }
		@Override public Preferences putLong(String key, long val) { values.put(key, val); return this; }
		@Override public Preferences putFloat(String key, float val) { values.put(key, val); return this; }
		@Override public Preferences putString(String key, String val) { values.put(key, val); return this; }
		@Override public Preferences put(Map<String, ?> vals) { values.putAll(vals); return this; }
		@Override public boolean getBoolean(String key) { return getBoolean(key, false); }
		@Override public int getInteger(String key) { return getInteger(key, 0); }
		@Override public long getLong(String key) { return getLong(key, 0); }
		@Override public float getFloat(String key) { return getFloat(key, 0); }
		@Override public String getString(String key) { return getString(key, ""); }
		@Override public boolean getBoolean(String key, boolean defValue) {
			Object value = values.get(key);
			return value instanceof Boolean ? (Boolean)value : defValue;
		}
		@Override public int getInteger(String key, int defValue) {
			Object value = values.get(key);
			return value instanceof Integer ? (Integer)value : defValue;
		}
		@Override public long getLong(String key, long defValue) {
			Object value = values.get(key);
			return value instanceof Long ? (Long)value : defValue;
		}
		@Override public float getFloat(String key, float defValue) {
			Object value = values.get(key);
			return value instanceof Float ? (Float)value : defValue;
		}
		@Override public String getString(String key, String defValue) {
			Object value = values.get(key);
			return value instanceof String ? (String)value : defValue;
		}
		@Override public Map<String, ?> get() { return new HashMap<>(values); }
		@Override public boolean contains(String key) { return values.containsKey(key); }
		@Override public void clear() { values.clear(); }
		@Override public void remove(String key) { values.remove(key); }
		@Override public void flush() { }
	}
}
