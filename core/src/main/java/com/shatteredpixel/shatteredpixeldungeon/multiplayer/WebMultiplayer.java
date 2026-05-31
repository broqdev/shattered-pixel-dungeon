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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMultiplayerOutcome;
import com.watabou.noosa.Game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

public class WebMultiplayer {

	private static final Logger LOG = Logger.getLogger(WebMultiplayer.class.getName());

	private static final int DEFAULT_FLOOR_CHASE_TURNS = 40;

	private static float turnProgress;
	private static int localTurns;
	private static int lastDepth = Integer.MIN_VALUE;
	private static int lastBranch = Integer.MIN_VALUE;

	private static int furthestLocalMainDepth;
	private static int furthestRemoteMainDepth;
	private static int chaseTargetDepth;
	private static int chaseTurnsLeft;
	private static int floorChaseTurns = DEFAULT_FLOOR_CHASE_TURNS;
	private static boolean floorChaseInfinite;
	private static boolean failed;
	private static String lastPeerStatusPayload;
	private static boolean localEliminated;
	private static RoomWinnerState roomWinner;
	private static boolean roomNoWinner;
	private static boolean roomOutcomeOverlayShown;
	private static boolean transientRoomRun;

	private static final HashSet<String> seenRemoteFloors = new HashSet<>();
	private static final HashMap<String, PeerMirrorState> peerMirrors = new HashMap<>();

	public static void update() {
		if (!enabled()) {
			return;
		}

		pollEvents();
		maybeClaimLastPlayerStanding();
		maybeMarkNoWinner();
		if (watcher()) {
			return;
		}
		announceCurrentFloorIfNeeded();
		publishPeerBuffStatusIfChanged();
	}

	public static void onLocalFloorChanged() {
		if (!active()) {
			return;
		}

		announceCurrentFloorIfNeeded();
		updateChaseForLocalFloor();
	}

	public static void recordHeroTime(float time) {
		if (!active() || failed || time <= 0f) {
			return;
		}

		turnProgress += time;
		while (turnProgress >= 1f && !failed) {
			turnProgress -= 1f;
			localTurns++;
			pollEvents();
			advanceChaseTurn();
			announceCurrentFloor();
		}
	}

	public static int localTurns() {
		return localTurns;
	}

	public static boolean chaseActive() {
		return active()
				&& !failed
				&& Dungeon.hero != null
				&& Dungeon.branch == 0
				&& chaseTargetDepth > 0
				&& furthestRemoteMainDepth > furthestLocalMainDepth;
	}

	public static int chaseTargetDepth() {
		return chaseActive() ? chaseTargetDepth : 0;
	}

	public static int chaseTurnsLeft() {
		return chaseActive() ? chaseTurnsLeft : 0;
	}

	public static int floorChaseTurns() {
		return floorChaseTurns;
	}

	public static boolean floorChaseInfinite() {
		return floorChaseInfinite;
	}

	public static String chaseStatusText() {
		if (!chaseActive()) {
			return "";
		}
		return String.format("Another player is ahead. Reach floor %d in %d turns.", chaseTargetDepth, chaseTurnsLeft);
	}

	public static boolean watcherView() {
		return enabled() && watcherSlot();
	}

	public static boolean watcherSlot() {
		return watcher() && GamesInProgress.isWatcherSlot(GamesInProgress.curSlot);
	}

	public static boolean transientRun() {
		return transientRoomRun;
	}

	static void clearTransientRunForTests() {
		endTransientRoomRun();
	}

	public static boolean inRoom() {
		return enabled();
	}

	public static boolean suppressSupportPrompts() {
		return inRoom() || roomWinnerKnown() || roomNoWinnerKnown();
	}

	public static String watchTargetId() {
		return Game.platform == null ? "" : Game.platform.multiplayerWatchTargetId();
	}

	public static boolean switchWatchTarget(String targetId) {
		if (!canSwitchWatchTarget(targetId)) {
			if (roomWinnerKnown() || roomNoWinnerKnown()) {
				showRoomOutcomeIfKnown();
			}
			return false;
		}
		Game.platform.switchMultiplayerWatchTarget(targetId);
		return true;
	}

	public static boolean selectPlayerListRow(PlayerListRow row) {
		if (row == null || !row.selectable) {
			return false;
		}
		return switchWatchTarget(row.playerId);
	}

	public static boolean multiplayerGameOverMenu() {
		return active() && Dungeon.hero != null && !Dungeon.hero.isAlive();
	}

	public static boolean canContinueToWatch() {
		return false;
	}

	public static boolean continueToWatch() {
		if (showRoomOutcomeIfKnown()) {
			return true;
		}
		if (!canContinueToWatch()) {
			return false;
		}
		String targetId = firstLivePeerId();
		if (targetId.isEmpty() || Game.platform == null) {
			return false;
		}
		announceLocalEliminated("game-over");
		return Game.platform.continueMultiplayerAsWatcher(targetId);
	}

	public static boolean onLocalGameOver() {
		if (!active()) {
			return false;
		}
		announceLocalEliminated("game-over");
		return true;
	}

	public static boolean leaveActiveGameIntentionally() {
		if (!active()) {
			return false;
		}
		announceLocalEliminated("left");
		if (Game.platform != null) {
			Game.platform.disconnectActiveMultiplayerGame();
		}
		endTransientRoomRun();
		return true;
	}

	public static boolean claimLocalVictory(String marker) {
		if (!localVictoryClaimAllowed()) {
			return false;
		}
		RoomWinnerState claim = new RoomWinnerState(
				playerId(),
				multiplayerPlayerName(),
				parseColorHex(multiplayerPlayerColor(), 0xFFFFFF),
				Dungeon.hero.heroClass == null ? HeroClass.WARRIOR : Dungeon.hero.heroClass,
				clampArmorTier(Dungeon.hero.tier()),
				multiplayerRoomEpoch(),
				Math.max(0, localTurns),
				safeVictoryMarker(marker),
				UUID.randomUUID().toString()
		);
		boolean accepted = acceptRoomWinnerClaim(claim);
		if (accepted) {
			webParityLog("multiplayer local victory claim accepted playerId=" + token(claim.participantId)
					+ " marker=" + token(claim.victoryMarker)
					+ " localTurns=" + claim.localTurns);
			publishPeerBuffStatusIfChanged();
		}
		return accepted;
	}

	public static boolean roomWinnerKnown() {
		return roomWinner != null;
	}

	public static boolean roomNoWinnerKnown() {
		return roomNoWinner;
	}

	public static RoomWinnerState roomWinner() {
		return roomWinner == null ? null : new RoomWinnerState(roomWinner);
	}

	public static boolean showRoomOutcomeIfKnown() {
		return showRoomWinnerIfKnown() || showRoomNoWinnerIfKnown();
	}

	public static boolean showRoomWinnerIfKnown() {
		if (!enabled() || !roomWinnerKnown()
				|| Game.switchingScene()
				|| roomOutcomeOverlayShown
				|| !(Game.scene() instanceof GameScene)) {
			return false;
		}
		roomOutcomeOverlayShown = true;
		GameScene.show(new WndMultiplayerOutcome());
		return true;
	}

	public static boolean showRoomNoWinnerIfKnown() {
		if (!enabled() || !roomNoWinnerKnown()
				|| Game.switchingScene()
				|| roomOutcomeOverlayShown
				|| !(Game.scene() instanceof GameScene)) {
			return false;
		}
		roomOutcomeOverlayShown = true;
		GameScene.show(new WndMultiplayerOutcome());
		return true;
	}

	public static void leaveRoomAfterVictory() {
		leaveRoomAfterOutcome();
	}

	public static void leaveRoomAfterNoWinner() {
		leaveRoomAfterOutcome();
	}

	private static void leaveRoomAfterOutcome() {
		if (Game.platform != null) {
			Game.platform.finishActiveMultiplayerRoom();
		}
		roomWinner = null;
		roomNoWinner = false;
		roomOutcomeOverlayShown = false;
		localEliminated = false;
		lastPeerStatusPayload = null;
		endTransientRoomRun();
	}

	public static void publishReplayEvent(String kind, int cell, String message) {
		if (!active() || Dungeon.hero == null || Dungeon.level == null || Game.platform == null) {
			return;
		}

		if (kind == null || kind.isEmpty()) {
			kind = "unknown";
		}
		if (message == null) {
			message = "";
		}

		Game.platform.announceMultiplayerReplayEvent(kind, Dungeon.depth, Dungeon.branch, cell, message);
		if (!"status".equals(kind)) {
			publishPeerBuffStatusIfChanged();
		}
	}

	public static void publishReplayEvent(String kind, String message) {
		publishReplayEvent(kind, Dungeon.hero == null ? 0 : Dungeon.hero.pos, message);
	}

	public static void saveActiveRunSnapshot(int save) {
		if (!active() || watcher() || Game.platform == null || Dungeon.hero == null) {
			return;
		}
		String snapshotFilesJson = "";
		if (GamesInProgress.isTransientMultiplayerSlot(save)) {
			snapshotFilesJson = Dungeon.transientRunSnapshotFilesJson(save);
			if (snapshotFilesJson == null || snapshotFilesJson.isEmpty()) {
				return;
			}
		}
		Game.platform.saveActiveMultiplayerRunSnapshot(save, snapshotFilesJson);
	}

	static void publishPeerBuffStatusIfChanged() {
		if (!active() || Dungeon.hero == null || Game.platform == null) {
			return;
		}

		String payload = peerStatusPayload();
		if (payload.equals(lastPeerStatusPayload)) {
			return;
		}

		lastPeerStatusPayload = payload;
		Game.platform.announceMultiplayerReplayEvent("status", Dungeon.depth, Dungeon.branch, Dungeon.hero.pos, payload);
	}

	static String peerStatusPayload() {
		StringBuilder payload = new StringBuilder();
		payload.append("{\"peerIdentity\":{");
		appendJsonField(payload, "name", multiplayerPlayerName());
		payload.append(',');
		appendJsonField(payload, "color", multiplayerPlayerColor());
		payload.append("},\"peerAppearance\":{");
		HeroClass heroClass = Dungeon.hero == null ? HeroClass.WARRIOR : Dungeon.hero.heroClass;
		appendJsonField(payload, "heroClass", heroClass == null ? HeroClass.WARRIOR.name() : heroClass.name());
		payload.append(',');
		payload.append("\"tier\":").append(clampArmorTier(Dungeon.hero == null ? 0 : Dungeon.hero.tier()));
		payload.append("},\"peerState\":{");
		payload.append("\"gameOver\":").append(localEliminated || (Dungeon.hero != null && !Dungeon.hero.isAlive()));
		payload.append(',');
		appendJsonField(payload, "state", localEliminated ? "game-over" : "live");
		payload.append("},\"peerVitals\":{");
		payload.append("\"hp\":").append(Math.max(0, Dungeon.hero == null ? 0 : Dungeon.hero.HP));
		payload.append(',');
		payload.append("\"ht\":").append(Math.max(0, Dungeon.hero == null ? 0 : Dungeon.hero.HT));
		payload.append("},\"peerBuffs\":[");
		if (Dungeon.hero != null) {
			boolean first = true;
			for (Buff buff : Dungeon.hero.buffs()) {
				int icon = buff.icon();
				if (icon == BuffIndicator.NONE) {
					continue;
				}
				if (!first) {
					payload.append(',');
				}
				first = false;
				payload.append('{');
				appendJsonField(payload, "className", buff.getClass().getName());
				payload.append(',');
				payload.append("\"icon\":").append(icon).append(',');
				appendJsonField(payload, "type", buff.type == null ? Buff.buffType.NEUTRAL.name() : buff.type.name());
				payload.append(',');
				appendJsonField(payload, "text", buff.iconTextDisplay());
				payload.append(',');
				payload.append("\"fade\":").append(safeIconFade(buff.iconFadePercent()));
				payload.append('}');
			}
		}
		payload.append(']');
		if (roomWinner != null) {
			payload.append(",\"roomWinner\":{");
			appendJsonField(payload, "participantId", roomWinner.participantId);
			payload.append(',');
			appendJsonField(payload, "playerName", roomWinner.playerName);
			payload.append(',');
			appendJsonField(payload, "playerColor", String.format("%06x", roomWinner.playerColor & 0xFFFFFF));
			payload.append(',');
			appendJsonField(payload, "heroClass", roomWinner.heroClass.name());
			payload.append(',');
			payload.append("\"armorTier\":").append(roomWinner.armorTier).append(',');
			payload.append("\"roomEpoch\":").append(roomWinner.roomEpoch).append(',');
			payload.append("\"localTurns\":").append(roomWinner.localTurns).append(',');
			appendJsonField(payload, "victoryMarker", roomWinner.victoryMarker);
			payload.append(',');
			appendJsonField(payload, "finishNonce", roomWinner.finishNonce);
			payload.append('}');
		}
		if (roomNoWinner) {
			payload.append(",\"roomNoWinner\":true");
		}
		payload.append('}');
		return payload.toString();
	}

	public static ArrayList<PeerMirrorState> peerMirrors() {
		ArrayList<PeerMirrorState> result = new ArrayList<>();
		for (PeerMirrorState state : peerMirrors.values()) {
			result.add(new PeerMirrorState(state));
		}
		return result;
	}

	public static ArrayList<PlayerListRow> playerListRows() {
		ArrayList<PlayerListRow> rows = new ArrayList<>();
		if (!enabled()) {
			return rows;
		}

		if (active() && Dungeon.hero != null) {
			rows.add(new PlayerListRow(
					playerId(),
					multiplayerPlayerName(),
					parseColorHex(multiplayerPlayerColor(), 0xFFFFFF),
					Dungeon.hero.heroClass == null ? HeroClass.WARRIOR : Dungeon.hero.heroClass,
					clampArmorTier(Dungeon.hero.tier()),
					Math.max(0, Dungeon.hero.HP),
					Math.max(0, Dungeon.hero.HT),
					currentPeerBuffs(),
					true,
					localEliminated || !Dungeon.hero.isAlive(),
					false,
					true,
					false,
					false
			));
		}

		ArrayList<PeerMirrorState> peers = peerMirrors();
		String watchTargetId = watchTargetId();
		boolean watcherView = watcherView();
		for (PeerMirrorState peer : peers) {
			boolean selected = watcherView && peer.playerId.equals(watchTargetId);
			boolean live = peer.connected && !peer.gameOver && !peer.timedOut;
			rows.add(new PlayerListRow(
					peer.playerId,
					peer.playerName == null || peer.playerName.isEmpty() ? peer.playerId : peer.playerName,
					peer.playerColor,
					peer.heroClass == null ? HeroClass.WARRIOR : peer.heroClass,
					clampArmorTier(peer.armorTier),
					peer.hp,
					peer.ht,
					peer.peerBuffs,
					peer.connected,
					peer.gameOver,
					peer.timedOut,
					false,
					selected,
					canSwitchWatchTarget(peer.playerId)
			));
		}
		sortPlayerListRows(rows);
		return rows;
	}

	private static boolean canSwitchWatchTarget(String targetId) {
		if (targetId == null || targetId.isEmpty() || Game.platform == null
				|| !watcherView() || roomWinnerKnown() || roomNoWinnerKnown()
				|| targetId.equals(watchTargetId())) {
			return false;
		}
		PeerMirrorState peer = peerMirrors.get(targetId);
		return peer != null && peer.connected && !peer.gameOver && !peer.timedOut;
	}

	public static boolean resumeCloneIfRequested() {
		if (!active()) {
			return false;
		}

		int slot = Game.platform.consumeMultiplayerResumeSlot();
		if (slot <= 0) {
			return false;
		}

		GamesInProgress.setUnknown(slot);
		if (GamesInProgress.check(slot) == null) {
			return false;
		}

		GamesInProgress.curSlot = slot;
		Dungeon.hero = null;
		Dungeon.daily = Dungeon.dailyReplay = false;
		ActionIndicator.clearAction();
		InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
		ShatteredPixelDungeon.switchScene(InterlevelScene.class);
		return true;
	}

	public static boolean resumeWatcherIfRequested() {
		if (!enabled() || !watcher()) {
			return false;
		}
		if (!Game.platform.consumeMultiplayerWatcherResumeRequested()) {
			return false;
		}

		GamesInProgress.setUnknown(GamesInProgress.WATCHER_SLOT);
		return resumeWatcherAfterRequested(GamesInProgress.check(GamesInProgress.WATCHER_SLOT) != null);
	}

	static boolean resumeWatcherAfterRequested(boolean watcherKeyframeAvailable) {
		if (!watcherKeyframeAvailable) {
			GLog.w("Watcher keyframe is unavailable. Requesting a new keyframe.");
			if (Game.platform != null) {
				Game.platform.requestMultiplayerWatcherKeyframe();
			}
			return false;
		}
		endTransientRoomRun();
		GamesInProgress.curSlot = GamesInProgress.WATCHER_SLOT;
		Dungeon.hero = null;
		Dungeon.daily = Dungeon.dailyReplay = false;
		ActionIndicator.clearAction();
		InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
		ShatteredPixelDungeon.switchScene(InterlevelScene.class);
		return true;
	}

	public static boolean launchRoomRun(String[] eventParts) {
		RoomLaunch launch = RoomLaunch.fromEvent(eventParts);
		return launchRoomRun(launch);
	}

	public static boolean launchRoomRun(RoomLaunch launch) {
		if (launch == null || !launch.valid) {
			GLog.w("Room launch data is invalid.");
			return false;
		}
		if (launch.watcher) {
			resetRoomRunState();
			GamesInProgress.setUnknown(GamesInProgress.WATCHER_SLOT);
			if (GamesInProgress.check(GamesInProgress.WATCHER_SLOT) != null) {
				return resumeWatcherAfterRequested(true);
			}
			GLog.i("Waiting for player view...");
			if (Game.platform != null) {
				Game.platform.requestMultiplayerWatcherKeyframe();
			}
			return false;
		}

		int slot = GamesInProgress.TRANSIENT_MULTIPLAYER_SLOT;
		prepareRoomRunForLaunch(launch, slot);
		ShatteredPixelDungeon.switchScene(InterlevelScene.class);
		return true;
	}

	static boolean prepareRoomRunForLaunch(RoomLaunch launch, int slot) {
		if (launch == null || !launch.valid || launch.watcher
				|| (slot <= 0 && !GamesInProgress.isTransientMultiplayerSlot(slot))) {
			return false;
		}
		resetRoomRunState();
		if (GamesInProgress.isTransientMultiplayerSlot(slot)) {
			beginTransientRoomRun();
		} else {
			endTransientRoomRun();
		}
		GamesInProgress.curSlot = slot;
		GamesInProgress.selectedClass = launch.heroClass;
		GamesInProgress.randomizedClass = false;
		Dungeon.hero = null;
		Dungeon.daily = Dungeon.dailyReplay = false;
		Dungeon.initSeed(launch.sharedRunSeed);
		configureFloorChase(launch.floorChaseTurns, launch.floorChaseInfinite);
		ActionIndicator.clearAction();
		InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
		return true;
	}

	public static class RoomLaunch {
		public final boolean valid;
		public final String roomName;
		public final String participantId;
		public final boolean watcher;
		public final HeroClass heroClass;
		public final String sharedRunSeed;
		public final String seedChecksum;
		public final int floorChaseTurns;
		public final boolean floorChaseInfinite;
		public final int roomEpoch;
		public final String watchTargetId;
		public final String playerName;
		public final String playerColor;
		public final String playerSeatOrder;

		private RoomLaunch(boolean valid, String roomName, String participantId, boolean watcher,
				HeroClass heroClass, String sharedRunSeed, String seedChecksum, int floorChaseTurns,
				boolean floorChaseInfinite, int roomEpoch, String watchTargetId,
				String playerName, String playerColor, String playerSeatOrder) {
			this.valid = valid;
			this.roomName = roomName;
			this.participantId = participantId;
			this.watcher = watcher;
			this.heroClass = heroClass;
			this.sharedRunSeed = sharedRunSeed;
			this.seedChecksum = seedChecksum;
			this.floorChaseTurns = floorChaseTurns;
			this.floorChaseInfinite = floorChaseInfinite;
			this.roomEpoch = roomEpoch;
			this.watchTargetId = watchTargetId;
			this.playerName = playerName == null ? "" : playerName;
			this.playerColor = playerColor == null ? "" : playerColor;
			this.playerSeatOrder = playerSeatOrder == null ? "" : playerSeatOrder;
		}

		public static RoomLaunch fromEvent(String[] parts) {
			if (parts == null || parts.length < 11 || !"room-launch".equals(parts[0])) {
				return invalid();
			}
			String role = value(parts, 3);
			boolean watcher = "watcher".equals(role);
			String seed = value(parts, 5);
			String watchTargetId = value(parts, 10);
			if (value(parts, 2).isEmpty() || seed.isEmpty()
					|| (!watcher && value(parts, 4).isEmpty()) || (watcher && watchTargetId.isEmpty())) {
				return invalid();
			}
			return new RoomLaunch(true, value(parts, 1), value(parts, 2), watcher,
					heroClass(value(parts, 4)), seed, value(parts, 6), parseInt(value(parts, 7)),
					"true".equals(value(parts, 8)), parseInt(value(parts, 9)), watchTargetId,
					value(parts, 11), value(parts, 12), value(parts, 13));
		}

		private static RoomLaunch invalid() {
			return new RoomLaunch(false, "", "", false, HeroClass.WARRIOR, "", "", 0, false, 0, "", "", "", "");
		}

		private static String value(String[] parts, int index) {
			return index >= 0 && index < parts.length ? parts[index] : "";
		}

		private static HeroClass heroClass(String value) {
			try {
				return HeroClass.valueOf(value == null || value.isEmpty() ? "WARRIOR" : value);
			} catch (IllegalArgumentException e) {
				return HeroClass.WARRIOR;
			}
		}
	}

	static void receiveRemoteFloor(String playerId, int depth, int branch, int turns) {
		if (playerId == null || playerId.equals(playerId())) {
			return;
		}

		String key = playerId + ":" + depth + ":" + branch;
		if (seenRemoteFloors.add(key)) {
			GLog.h("Player %s reached floor %d.", playerId, depth);
		}

		if (branch == 0 && depth > furthestRemoteMainDepth) {
			furthestRemoteMainDepth = depth;
			updateChaseForLocalFloor();
		}
		webParityLog("multiplayer remote floor received playerId=" + token(playerId)
				+ " depth=" + depth
				+ " branch=" + branch
				+ " turns=" + turns
				+ " chaseTarget=" + chaseTargetDepth
				+ " chaseTurnsLeft=" + chaseTurnsLeft);
	}

	static void configureFloorChase(int turns, boolean infinite) {
		if (turns < 10 || turns > 100 || turns % 10 != 0) {
			turns = DEFAULT_FLOOR_CHASE_TURNS;
		}
		floorChaseTurns = turns;
		floorChaseInfinite = infinite;
		if (floorChaseInfinite) {
			chaseTargetDepth = 0;
			chaseTurnsLeft = 0;
		}
	}

	static void receiveStatus(String message) {
		if (message != null && !message.isEmpty()) {
			GLog.i(message);
		}
	}

	static void receiveOutcomeStatus(String sourcePlayerId, String message) {
		if (!enabled() || sourcePlayerId == null || sourcePlayerId.equals(playerId())
				|| message == null || message.isEmpty()) {
			return;
		}
		boolean hadOutcome = roomWinnerKnown() || roomNoWinnerKnown();
		applyPeerStatus(peerMirror(sourcePlayerId), message);
		if (!hadOutcome && (roomWinnerKnown() || roomNoWinnerKnown())) {
			webParityLog("multiplayer outcome status received playerId=" + token(sourcePlayerId)
					+ " roomWinner=" + roomWinnerKnown()
					+ " roomNoWinner=" + roomNoWinnerKnown());
		}
	}

	private static void pollEvents() {
		String event;
		while ((event = Game.platform.pollMultiplayerEvent()) != null) {
			handleEvent(event);
		}
	}

	private static void handleEvent(String event) {
		String[] parts = event.split("\\|", -1);
		if (parts.length == 0) {
			return;
		}

		if ("floor".equals(parts[0]) && parts.length >= 5) {
			receiveRemoteFloor(parts[1], parseInt(parts[2]), parseInt(parts[3]), parseInt(parts[4]));
		} else if ("debug-turns".equals(parts[0]) && parts.length >= 2) {
			debugAdvanceHeroTime(parseInt(parts[1]));
		} else if ("status".equals(parts[0]) && parts.length >= 2) {
			receiveStatus(parts[1]);
		} else if ("outcome-status".equals(parts[0]) && parts.length >= 3) {
			receiveOutcomeStatus(parts[1], parts[2]);
		} else if ("watch-kf-req".equals(parts[0]) && parts.length >= 3) {
			receiveWatcherKeyframeRequest(parts[1], parts[2]);
		} else if ("replay".equals(parts[0]) && parts.length >= 8) {
			receiveReplayEvent(parts[1], parseInt(parts[2]), parts[3], parseInt(parts[4]),
					parseInt(parts[5]), parseInt(parts[6]), parts[7]);
		} else if ("peer-left".equals(parts[0]) && parts.length >= 2) {
			markPeerDisconnected(parts[1]);
		} else if ("peer-returned".equals(parts[0]) && parts.length >= 2) {
			markPeerReturned(parts[1]);
		} else if ("peer-timeout".equals(parts[0]) && parts.length >= 2) {
			markPeerTimedOut(parts[1]);
		}
	}

	private static void debugAdvanceHeroTime(int turns) {
		if (turns <= 0) {
			return;
		}
		int beforeTurns = localTurns;
		recordHeroTime(Math.min(200, turns));
		webParityLog("multiplayer debug turns advanced requested=" + turns
				+ " applied=" + (localTurns - beforeTurns)
				+ " localTurns=" + localTurns
				+ " chaseTarget=" + chaseTargetDepth
				+ " chaseTurnsLeft=" + chaseTurnsLeft
				+ " failed=" + failed);
	}

	private static void webParityLog(String message) {
		LOG.info("[WEB-PARITY] " + message);
	}

	private static String token(String value) {
		if (value == null) {
			return "";
		}
		StringBuilder result = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			result.append(Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == '.' ? c : '_');
		}
		return result.toString();
	}

	private static void receiveWatcherKeyframeRequest(String watcherId, String requestId) {
		if (watcherId != null && !watcherId.isEmpty()) {
			GLog.i("Watcher %s requested a keyframe.", watcherId);
		}
		String snapshotFilesJson = "";
		if (!watcher() && Dungeon.hero != null) {
			try {
				Dungeon.saveAll("multiplayerWatcherKeyframe");
				snapshotFilesJson = Dungeon.transientRunSnapshotFilesJson(GamesInProgress.curSlot);
			} catch (Exception e) {
				LOG.warning("Unable to save watcher keyframe: " + e.getClass().getName());
			}
		}
		if (Game.platform != null) {
			Game.platform.sendMultiplayerWatcherKeyframe(watcherId, requestId, snapshotFilesJson);
		}
	}

	private static void beginTransientRoomRun() {
		transientRoomRun = true;
		Dungeon.beginTransientRunState();
	}

	private static void endTransientRoomRun() {
		transientRoomRun = false;
		Dungeon.clearTransientRunState();
	}

	static void receiveReplayEvent(String sourcePlayerId, int sequence, String kind, int depth, int branch,
			int cell, String message) {
		if (sourcePlayerId == null) {
			return;
		}
		boolean accepted = receivePeerMirrorReplayEvent(sourcePlayerId, sequence, kind, depth, branch, cell, message);
		if (!watcherView()) {
			return;
		}
		if (!sourcePlayerId.equals(watchTargetId())) {
			return;
		}
		if (!accepted && ("move".equals(kind) || "transition".equals(kind) || "status".equals(kind))) {
			return;
		}

		if (message == null || message.isEmpty()) {
			message = String.format("Watcher replay %s #%d.", kind, sequence);
		}

		if ("status".equals(kind) || "log".equals(kind)) {
			GLog.i(message);
			return;
		}

		if ("transition".equals(kind) || "unknown".equals(kind)) {
			GLog.w(message);
			Game.platform.requestMultiplayerWatcherKeyframe();
			return;
		}

		if ("move".equals(kind)) {
			if (applyWatcherMoveReplay(depth, branch, cell)) {
				GLog.i(message);
			} else {
				GLog.w("Watcher view is out of sync. Requesting a new keyframe.");
				Game.platform.requestMultiplayerWatcherKeyframe();
			}
			return;
		}

		if ("kill".equals(kind)) {
			if (applyWatcherKillReplay(depth, branch, cell)) {
				GLog.i(message);
			} else {
				GLog.w("Watcher view is out of sync. Requesting a new keyframe.");
				Game.platform.requestMultiplayerWatcherKeyframe();
			}
			return;
		}

		GLog.i(message);
	}

	private static boolean applyWatcherMoveReplay(int depth, int branch, int cell) {
		if (Dungeon.hero == null || depth != Dungeon.depth || branch != Dungeon.branch) {
			return false;
		}
		return GameScene.syncWatcherTargetCell(cell);
	}

	private static boolean applyWatcherKillReplay(int depth, int branch, int cell) {
		if (Dungeon.hero == null || depth != Dungeon.depth || branch != Dungeon.branch) {
			return false;
		}
		return GameScene.syncWatcherMobKilled(cell);
	}

	private static boolean receivePeerMirrorReplayEvent(String sourcePlayerId, int sequence, String kind, int depth,
			int branch, int cell, String message) {
		if (!enabled() || sourcePlayerId.equals(playerId())) {
			return false;
		}

		if ("status".equals(kind)) {
			return peerMirror(sourcePlayerId).update(sequence, depth, branch, cell, kind, message);
		}
		if (!"move".equals(kind) && !"transition".equals(kind)) {
			return false;
		}

		return peerMirror(sourcePlayerId).update(sequence, depth, branch, cell, kind, message);
	}

	private static PeerMirrorState peerMirror(String playerId) {
		PeerMirrorState state = peerMirrors.get(playerId);
		if (state == null) {
			state = new PeerMirrorState(playerId);
			peerMirrors.put(playerId, state);
		}
		return state;
	}

	static void markPeerDisconnected(String playerId) {
		if (playerId != null && !playerId.isEmpty()) {
			peerMirror(playerId).connected = false;
		}
	}

	static void markPeerReturned(String playerId) {
		if (playerId != null) {
			PeerMirrorState state = peerMirrors.get(playerId);
			if (state != null && !state.timedOut) {
				state.connected = true;
				state.sequence = 0;
			}
		}
	}

	static void markPeerTimedOut(String playerId) {
		if (playerId != null && !playerId.isEmpty()) {
			PeerMirrorState state = peerMirror(playerId);
			state.connected = false;
			state.gameOver = true;
			state.timedOut = true;
		}
	}

	private static void maybeClaimLastPlayerStanding() {
		if (!active() || watcher() || roomWinner != null || roomNoWinner || localEliminated
				|| Dungeon.hero == null || !Dungeon.hero.isAlive()) {
			return;
		}

		ArrayList<String> playerIds = knownCompetitivePlayerIds();
		if (playerIds.size() <= 1 || !playerIds.contains(playerId())) {
			return;
		}

		for (String participantId : playerIds) {
			if (participantId.equals(playerId())) {
				continue;
			}
			PeerMirrorState peer = peerMirrors.get(participantId);
			if (peer == null || (!peer.gameOver && !peer.timedOut)) {
				return;
			}
		}

		claimLocalVictory("last-player-standing");
	}

	private static void maybeMarkNoWinner() {
		if (!enabled() || roomWinner != null || roomNoWinner) {
			return;
		}

		ArrayList<String> playerIds = knownCompetitivePlayerIds();
		if (playerIds.isEmpty()) {
			return;
		}

		for (String participantId : playerIds) {
			if (participantId.equals(playerId()) && active()) {
				if (!localPlayerEliminated()) {
					return;
				}
				continue;
			}

			PeerMirrorState peer = peerMirrors.get(participantId);
			if (peer == null || (!peer.gameOver && !peer.timedOut)) {
				return;
			}
		}

		roomNoWinner = true;
	}

	private static ArrayList<String> knownCompetitivePlayerIds() {
		ArrayList<String> result = playerSeatOrder();
		if (!result.isEmpty()) {
			return result;
		}

		String localPlayerId = playerId();
		if (active() && !localPlayerId.isEmpty()) {
			result.add(localPlayerId);
		}
		for (PeerMirrorState peer : peerMirrors.values()) {
			if (peer.playerId != null && !peer.playerId.isEmpty() && !result.contains(peer.playerId)) {
				result.add(peer.playerId);
			}
		}
		return result;
	}

	private static void announceCurrentFloorIfNeeded() {
		if (Dungeon.hero == null || Dungeon.level == null) {
			return;
		}
		recordLocalMainDepthReached();
		if (Dungeon.depth != lastDepth || Dungeon.branch != lastBranch) {
			lastDepth = Dungeon.depth;
			lastBranch = Dungeon.branch;
			announceCurrentFloor();
		}
	}

	private static void announceCurrentFloor() {
		if (Dungeon.hero == null || Dungeon.level == null) {
			return;
		}
		Game.platform.announceMultiplayerProgress(GamesInProgress.curSlot, Dungeon.depth, Dungeon.branch, localTurns);
	}

	private static void announceLocalEliminated(String reason) {
		if (!active() || Game.platform == null || localEliminated) {
			return;
		}
		localEliminated = true;
		String payload = peerStatusPayload(reason);
		lastPeerStatusPayload = payload;
		Game.platform.announceMultiplayerReplayEvent("status", Dungeon.depth, Dungeon.branch,
				Dungeon.hero == null ? 0 : Dungeon.hero.pos, payload);
	}

	private static String peerStatusPayload(String state) {
		boolean wasEliminated = localEliminated;
		localEliminated = true;
		String payload = peerStatusPayload();
		localEliminated = wasEliminated;
		return payload.replace("\"state\":\"game-over\"", "\"state\":\"" + safeStateName(state) + "\"");
	}

	private static String safeStateName(String state) {
		return "left".equals(state) ? "left" : "game-over";
	}

	private static String firstLivePeerId() {
		ArrayList<String> seatOrder = playerSeatOrder();
		if (!seatOrder.isEmpty()) {
			for (String participantId : seatOrder) {
				if (livePeer(participantId)) {
					return participantId;
				}
			}
		}

		ArrayList<String> ids = new ArrayList<>();
		for (PeerMirrorState state : peerMirrors.values()) {
			if (livePeer(state.playerId)) {
				ids.add(state.playerId);
			}
		}
		ids.sort(String::compareTo);
		return ids.isEmpty() ? "" : ids.get(0);
	}

	private static int liveCompetitivePlayerCount() {
		int count = 0;
		for (String participantId : knownCompetitivePlayerIds()) {
			if (participantId.equals(playerId())) {
				if (active() && !localPlayerEliminated()) {
					count++;
				}
			} else if (livePeer(participantId)) {
				count++;
			}
		}
		return count;
	}

	private static boolean livePeer(String participantId) {
		if (participantId == null || participantId.isEmpty()) {
			return false;
		}
		PeerMirrorState state = peerMirrors.get(participantId);
		return state != null && state.connected && !state.gameOver && !state.timedOut;
	}

	private static boolean localPlayerEliminated() {
		return localEliminated || Dungeon.hero == null || !Dungeon.hero.isAlive();
	}

	private static void updateChaseForLocalFloor() {
		recordLocalMainDepthReached();
		if (floorChaseInfinite || failed || Dungeon.hero == null || Dungeon.branch != 0
				|| furthestRemoteMainDepth <= furthestLocalMainDepth) {
			chaseTargetDepth = 0;
			chaseTurnsLeft = 0;
			return;
		}

		int nextTarget = furthestLocalMainDepth + 1;
		if (nextTarget != chaseTargetDepth) {
			chaseTargetDepth = nextTarget;
			chaseTurnsLeft = floorChaseTurns;
			GLog.w("Another player is ahead. Reach floor %d in %d turns.", chaseTargetDepth, chaseTurnsLeft);
		}
	}

	private static void advanceChaseTurn() {
		recordLocalMainDepthReached();
		if (floorChaseInfinite || failed || Dungeon.hero == null || Dungeon.branch != 0
				|| furthestRemoteMainDepth <= furthestLocalMainDepth) {
			return;
		}

		if (chaseTargetDepth == 0) {
			updateChaseForLocalFloor();
		}

		chaseTurnsLeft--;
		if (chaseTurnsLeft > 0) {
			return;
		}

		failed = true;
		GLog.n("You failed the floor chase.");
		Dungeon.fail(FloorChaseFailure.class);
		Dungeon.hero.HP = 0;
		Hero.reallyDie(FloorChaseFailure.class);
	}

	private static boolean active() {
		return enabled() && !watcher();
	}

	private static boolean enabled() {
		return Game.platform != null && Game.platform.multiplayerEnabled();
	}

	private static boolean watcher() {
		return Game.platform != null && Game.platform.multiplayerWatcher();
	}

	private static String playerId() {
		return Game.platform == null ? "" : Game.platform.multiplayerPlayerId();
	}

	private static String multiplayerPlayerName() {
		String name = Game.platform == null ? "" : Game.platform.multiplayerPlayerName();
		return name == null || name.isEmpty() ? playerId() : name;
	}

	private static String multiplayerPlayerColor() {
		String color = Game.platform == null ? "" : Game.platform.multiplayerPlayerColor();
		return normalizeColorHex(color);
	}

	private static void recordLocalMainDepthReached() {
		if (Dungeon.hero != null && Dungeon.branch == 0 && Dungeon.depth > furthestLocalMainDepth) {
			furthestLocalMainDepth = Dungeon.depth;
		}
	}

	static void resetForTesting() {
		resetRoomRunState();
	}

	private static void resetRoomRunState() {
		turnProgress = 0f;
		localTurns = 0;
		lastDepth = Integer.MIN_VALUE;
		lastBranch = Integer.MIN_VALUE;
		furthestLocalMainDepth = 0;
		furthestRemoteMainDepth = 0;
		chaseTargetDepth = 0;
		chaseTurnsLeft = 0;
		floorChaseTurns = DEFAULT_FLOOR_CHASE_TURNS;
		floorChaseInfinite = false;
		failed = false;
		lastPeerStatusPayload = null;
		localEliminated = false;
		roomWinner = null;
		roomNoWinner = false;
		roomOutcomeOverlayShown = false;
		seenRemoteFloors.clear();
		peerMirrors.clear();
	}

	private static void appendJsonField(StringBuilder payload, String key, String value) {
		payload.append('"').append(key).append("\":");
		appendJsonString(payload, value == null ? "" : value);
	}

	private static void appendJsonString(StringBuilder payload, String value) {
		payload.append('"');
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			switch (ch) {
				case '"':
					payload.append("\\\"");
					break;
				case '\\':
					payload.append("\\\\");
					break;
				case '\b':
					payload.append("\\b");
					break;
				case '\f':
					payload.append("\\f");
					break;
				case '\n':
					payload.append("\\n");
					break;
				case '\r':
					payload.append("\\r");
					break;
				case '\t':
					payload.append("\\t");
					break;
				default:
					if (ch < 0x20) {
						payload.append(String.format("\\u%04x", (int) ch));
					} else {
						payload.append(ch);
					}
					break;
			}
		}
		payload.append('"');
	}

	private static float safeIconFade(float fade) {
		if (Float.isNaN(fade) || Float.isInfinite(fade)) {
			return 0f;
		}
		return Math.max(0f, Math.min(1f, fade));
	}

	private static int parseInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static String normalizeColorHex(String color) {
		if (color == null) {
			return "";
		}
		String normalized = color.trim();
		if (normalized.startsWith("#")) {
			normalized = normalized.substring(1);
		}
		if (normalized.length() != 6) {
			return "";
		}
		for (int i = 0; i < normalized.length(); i++) {
			if (Character.digit(normalized.charAt(i), 16) < 0) {
				return "";
			}
		}
		return normalized.toLowerCase();
	}

	private static boolean localVictoryClaimAllowed() {
		return active() && Dungeon.hero != null && !localEliminated && Dungeon.hero.isAlive();
	}

	private static int parseColorHex(String color, int fallback) {
		String normalized = normalizeColorHex(color);
		if (normalized.isEmpty()) {
			return fallback;
		}
		try {
			return Integer.parseInt(normalized, 16);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static int multiplayerRoomEpoch() {
		return Game.platform == null ? 0 : Game.platform.multiplayerRoomEpoch();
	}

	private static boolean acceptRoomWinnerClaim(RoomWinnerState claim) {
		if (roomNoWinner) {
			return false;
		}
		if (!validRoomWinnerClaim(claim)) {
			return false;
		}
		if (roomWinner == null || compareRoomWinnerClaims(claim, roomWinner) < 0) {
			roomWinner = new RoomWinnerState(claim);
			roomNoWinner = false;
			lastPeerStatusPayload = null;
			return true;
		}
		return false;
	}

	private static boolean validRoomWinnerClaim(RoomWinnerState claim) {
		if (claim == null || claim.participantId.isEmpty() || claim.victoryMarker.isEmpty()
				|| claim.finishNonce.isEmpty() || claim.localTurns < 0) {
			return false;
		}
		int currentEpoch = multiplayerRoomEpoch();
		if (currentEpoch > 0 && claim.roomEpoch != currentEpoch) {
			return false;
		}
		if (claim.participantId.equals(playerId())) {
			return localVictoryClaimAllowed();
		}

		PeerMirrorState state = peerMirrors.get(claim.participantId);
		return state != null && state.connected && !state.gameOver && !state.timedOut;
	}

	private static int compareRoomWinnerClaims(RoomWinnerState left, RoomWinnerState right) {
		if (left.localTurns != right.localTurns) {
			return left.localTurns < right.localTurns ? -1 : 1;
		}
		return roomWinnerTieBreakKey(left).compareTo(roomWinnerTieBreakKey(right));
	}

	private static String roomWinnerTieBreakKey(RoomWinnerState claim) {
		String material = Dungeon.seed + "|" + claim.participantId + "|" + claim.finishNonce;
		return stableTieBreakHash(material);
	}

	private static String stableTieBreakHash(String material) {
		long hash = 0xcbf29ce484222325L;
		for (int i = 0; i < material.length(); i++) {
			hash ^= material.charAt(i);
			hash *= 0x100000001b3L;
		}
		String hex = Long.toHexString(hash);
		return "0000000000000000".substring(Math.min(hex.length(), 16)) + hex;
	}

	private static String safeVictoryMarker(String marker) {
		if (marker == null) {
			return "victory";
		}
		String safe = marker.trim().toLowerCase();
		if (safe.isEmpty()) {
			return "victory";
		}
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < safe.length(); i++) {
			char ch = safe.charAt(i);
			if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '-' || ch == '_') {
				result.append(ch);
			}
		}
		return result.length() == 0 ? "victory" : result.toString();
	}

	public static class FloorChaseFailure {
	}

	public static class PeerMirrorState {
		public final String playerId;
		public int sequence;
		public int depth;
		public int branch;
		public int cell;
		public String lastKind;
		public String status;
		public String playerName = "";
		public int playerColor = 0xFFFFFF;
		public HeroClass heroClass;
		public int armorTier = -1;
		public int hp = -1;
		public int ht = -1;
		public ArrayList<PeerBuffState> peerBuffs = new ArrayList<>();
		public boolean connected = true;
		public boolean gameOver;
		public boolean timedOut;

		private PeerMirrorState(String playerId) {
			this.playerId = playerId;
		}

		private PeerMirrorState(PeerMirrorState other) {
			playerId = other.playerId;
			sequence = other.sequence;
			depth = other.depth;
			branch = other.branch;
			cell = other.cell;
			lastKind = other.lastKind;
			status = other.status;
			playerName = other.playerName;
			playerColor = other.playerColor;
			heroClass = other.heroClass;
			armorTier = other.armorTier;
			hp = other.hp;
			ht = other.ht;
			peerBuffs = copyPeerBuffs(other.peerBuffs);
			connected = other.connected;
			gameOver = other.gameOver;
			timedOut = other.timedOut;
		}

		private boolean update(int sequence, int depth, int branch, int cell, String kind, String message) {
			if (sequence <= this.sequence) {
				return false;
			}
			this.sequence = sequence;
			this.depth = depth;
			this.branch = branch;
			this.cell = cell;
			this.lastKind = kind;
			applyPeerStatus(this, message);
			if ("status".equals(kind)) {
				this.status = message;
			}
			return true;
		}
	}

	public static class PeerBuffState {
		public final String className;
		public final int icon;
		public final Buff.buffType type;
		public final String text;
		public final float fade;

		public PeerBuffState(String className, int icon, Buff.buffType type, String text, float fade) {
			this.className = className == null ? "" : className;
			this.icon = icon;
			this.type = type == null ? Buff.buffType.NEUTRAL : type;
			this.text = text == null ? "" : text;
			this.fade = safeIconFade(fade);
		}
	}

	private static void applyPeerStatus(PeerMirrorState state, String message) {
		if (message == null || message.isEmpty()) {
			return;
		}

		String identity = jsonObjectValue(message, "peerIdentity");
		if (identity != null) {
			String name = jsonStringValue(identity, "name");
			String color = jsonStringValue(identity, "color");
			if (name != null) {
				state.playerName = name;
			}
			state.playerColor = parseColorHex(color, state.playerColor);
		}

		String appearance = jsonObjectValue(message, "peerAppearance");
		if (appearance != null) {
			String heroClassName = jsonStringValue(appearance, "heroClass");
			int tier = jsonIntValue(appearance, "tier", state.armorTier);
			if (heroClassName != null && !heroClassName.isEmpty()) {
				try {
					state.heroClass = HeroClass.valueOf(heroClassName);
				} catch (IllegalArgumentException ignored) {
					// Ignore unknown future classes instead of dropping the rest of the replay state.
				}
			}
			state.armorTier = clampArmorTier(tier);
		}

		String peerState = jsonObjectValue(message, "peerState");
		if (peerState != null) {
			state.gameOver = state.timedOut || jsonBooleanValue(peerState, "gameOver", state.gameOver);
		}

		String winner = jsonObjectValue(message, "roomWinner");
		if (winner != null) {
			acceptRoomWinnerClaim(roomWinnerClaimFromJson(winner));
		}
		if (!roomWinnerKnown() && jsonBooleanValue(message, "roomNoWinner", false)) {
			roomNoWinner = true;
		}

		String vitals = jsonObjectValue(message, "peerVitals");
		if (vitals != null) {
			state.hp = Math.max(0, jsonIntValue(vitals, "hp", state.hp));
			state.ht = Math.max(0, jsonIntValue(vitals, "ht", state.ht));
		}

		ArrayList<String> buffObjects = jsonObjectArrayValue(message, "peerBuffs");
		if (buffObjects != null) {
			state.peerBuffs = parsePeerBuffs(buffObjects);
		}
	}

	private static RoomWinnerState roomWinnerClaimFromJson(String payload) {
		String participantId = jsonStringValue(payload, "participantId");
		String victoryMarker = jsonStringValue(payload, "victoryMarker");
		HeroClass heroClass = HeroClass.WARRIOR;
		String heroClassName = jsonStringValue(payload, "heroClass");
		if (heroClassName != null && !heroClassName.isEmpty()) {
			try {
				heroClass = HeroClass.valueOf(heroClassName);
			} catch (IllegalArgumentException ignored) {
				// Unknown future classes are normalized to the current default.
			}
		}
		return new RoomWinnerState(
				participantId,
				jsonStringValue(payload, "playerName"),
				parseColorHex(jsonStringValue(payload, "playerColor"), 0xFFFFFF),
				heroClass,
				jsonIntValue(payload, "armorTier", 0),
				jsonIntValue(payload, "roomEpoch", 0),
				jsonIntValue(payload, "localTurns", -1),
				victoryMarker == null ? "" : safeVictoryMarker(victoryMarker),
				jsonStringValue(payload, "finishNonce")
		);
	}

	private static ArrayList<PeerBuffState> parsePeerBuffs(ArrayList<String> buffs) {
		ArrayList<PeerBuffState> result = new ArrayList<>();
		for (String buff : buffs) {
			if (buff == null) {
				continue;
			}

			int icon = jsonIntValue(buff, "icon", BuffIndicator.NONE);
			if (icon < 0 || icon == BuffIndicator.NONE) {
				continue;
			}

			Buff.buffType type = Buff.buffType.NEUTRAL;
			String typeName = jsonStringValue(buff, "type");
			try {
				type = Buff.buffType.valueOf(typeName == null ? type.name() : typeName);
			} catch (IllegalArgumentException ignored) {
				// Keep unknown future types neutral while preserving the icon record.
			}

			result.add(new PeerBuffState(
					jsonStringValue(buff, "className"),
					icon,
					type,
					jsonStringValue(buff, "text"),
					jsonFloatValue(buff, "fade", 0f)));
		}
		return result;
	}

	private static ArrayList<PeerBuffState> currentPeerBuffs() {
		ArrayList<PeerBuffState> result = new ArrayList<>();
		if (Dungeon.hero == null) {
			return result;
		}
		for (Buff buff : Dungeon.hero.buffs()) {
			int icon = buff.icon();
			if (icon == BuffIndicator.NONE) {
				continue;
			}
			result.add(new PeerBuffState(
					buff.getClass().getName(),
					icon,
					buff.type == null ? Buff.buffType.NEUTRAL : buff.type,
					buff.iconTextDisplay(),
					buff.iconFadePercent()
			));
		}
		return result;
	}

	private static void sortPlayerListRows(ArrayList<PlayerListRow> rows) {
		ArrayList<String> seatOrder = playerSeatOrder();
		rows.sort(new Comparator<PlayerListRow>() {
			@Override
			public int compare(PlayerListRow left, PlayerListRow right) {
				int leftIndex = seatOrderIndex(left.playerId, seatOrder);
				int rightIndex = seatOrderIndex(right.playerId, seatOrder);
				if (leftIndex != rightIndex) {
					return leftIndex - rightIndex;
				}
				return left.playerId.compareTo(right.playerId);
			}
		});
	}

	private static ArrayList<String> playerSeatOrder() {
		ArrayList<String> result = new ArrayList<>();
		String value = Game.platform == null ? "" : Game.platform.multiplayerPlayerSeatOrder();
		if (value == null || value.trim().isEmpty()) {
			return result;
		}
		for (String playerId : value.split(",")) {
			playerId = playerId.trim();
			if (!playerId.isEmpty()) {
				result.add(playerId);
			}
		}
		return result;
	}

	private static int seatOrderIndex(String playerId, ArrayList<String> seatOrder) {
		if (playerId != null && seatOrder != null) {
			int index = seatOrder.indexOf(playerId);
			if (index >= 0) {
				return index;
			}
		}
		return 1000;
	}

	public static class PlayerListRow {
		public final String playerId;
		public final String playerName;
		public final int playerColor;
		public final HeroClass heroClass;
		public final int armorTier;
		public final int hp;
		public final int ht;
		public final ArrayList<PeerBuffState> peerBuffs;
		public final boolean connected;
		public final boolean gameOver;
		public final boolean timedOut;
		public final boolean localPlayer;
		public final boolean watchTarget;
		public final boolean selectable;

		public PlayerListRow(String playerId, String playerName, int playerColor, HeroClass heroClass, int armorTier,
				int hp, int ht, ArrayList<PeerBuffState> peerBuffs, boolean connected, boolean gameOver,
				boolean timedOut, boolean localPlayer, boolean watchTarget, boolean selectable) {
			this.playerId = playerId == null ? "" : playerId;
			this.playerName = playerName == null || playerName.isEmpty() ? this.playerId : playerName;
			this.playerColor = playerColor;
			this.heroClass = heroClass == null ? HeroClass.WARRIOR : heroClass;
			this.armorTier = clampArmorTier(armorTier);
			this.hp = hp;
			this.ht = ht;
			this.peerBuffs = copyPeerBuffs(peerBuffs);
			this.connected = connected;
			this.gameOver = gameOver;
			this.timedOut = timedOut;
			this.localPlayer = localPlayer;
			this.watchTarget = watchTarget;
			this.selectable = selectable;
		}

		public boolean live() {
			return connected && !gameOver && !timedOut;
		}
	}

	public static class RoomWinnerState {
		public final String participantId;
		public final String playerName;
		public final int playerColor;
		public final HeroClass heroClass;
		public final int armorTier;
		public final int roomEpoch;
		public final int localTurns;
		public final String victoryMarker;
		public final String finishNonce;

		private RoomWinnerState(String participantId, String playerName, int playerColor, HeroClass heroClass,
				int armorTier, int roomEpoch, int localTurns, String victoryMarker, String finishNonce) {
			this.participantId = participantId == null ? "" : participantId;
			this.playerName = playerName == null || playerName.isEmpty() ? this.participantId : playerName;
			this.playerColor = playerColor;
			this.heroClass = heroClass == null ? HeroClass.WARRIOR : heroClass;
			this.armorTier = clampArmorTier(armorTier);
			this.roomEpoch = roomEpoch;
			this.localTurns = localTurns;
			this.victoryMarker = victoryMarker == null ? "" : victoryMarker;
			this.finishNonce = finishNonce == null ? "" : finishNonce;
		}

		private RoomWinnerState(RoomWinnerState other) {
			this(other.participantId, other.playerName, other.playerColor, other.heroClass, other.armorTier,
					other.roomEpoch, other.localTurns, other.victoryMarker, other.finishNonce);
		}
	}

	private static ArrayList<PeerBuffState> copyPeerBuffs(ArrayList<PeerBuffState> buffs) {
		ArrayList<PeerBuffState> result = new ArrayList<>();
		if (buffs != null) {
			result.addAll(buffs);
		}
		return result;
	}

	private static String jsonObjectValue(String payload, String name) {
		return jsonDelimitedValue(payload, name, '{', '}');
	}

	private static ArrayList<String> jsonObjectArrayValue(String payload, String name) {
		String array = jsonDelimitedValue(payload, name, '[', ']');
		if (array == null) {
			return null;
		}

		ArrayList<String> result = new ArrayList<>();
		int depth = 0;
		int start = -1;
		boolean inString = false;
		boolean escaped = false;
		for (int i = 1; i < array.length() - 1; i++) {
			char c = array.charAt(i);
			if (inString) {
				if (escaped) {
					escaped = false;
				} else if (c == '\\') {
					escaped = true;
				} else if (c == '"') {
					inString = false;
				}
				continue;
			}
			if (c == '"') {
				inString = true;
			} else if (c == '{') {
				if (depth == 0) {
					start = i;
				}
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0 && start >= 0) {
					result.add(array.substring(start, i + 1));
					start = -1;
				}
			}
		}
		return result;
	}

	private static String jsonDelimitedValue(String payload, String name, char open, char close) {
		int nameIndex = jsonFieldIndex(payload, name);
		if (nameIndex < 0) {
			return null;
		}
		int start = payload.indexOf(open, nameIndex);
		if (start < 0) {
			return null;
		}
		int depth = 0;
		boolean inString = false;
		boolean escaped = false;
		for (int i = start; i < payload.length(); i++) {
			char c = payload.charAt(i);
			if (inString) {
				if (escaped) {
					escaped = false;
				} else if (c == '\\') {
					escaped = true;
				} else if (c == '"') {
					inString = false;
				}
				continue;
			}
			if (c == '"') {
				inString = true;
			} else if (c == open) {
				depth++;
			} else if (c == close) {
				depth--;
				if (depth == 0) {
					return payload.substring(start, i + 1);
				}
			}
		}
		return null;
	}

	private static String jsonStringValue(String payload, String name) {
		int nameIndex = jsonFieldIndex(payload, name);
		if (nameIndex < 0) {
			return null;
		}
		int colon = payload.indexOf(':', nameIndex);
		if (colon < 0) {
			return null;
		}
		int start = colon + 1;
		while (start < payload.length() && Character.isWhitespace(payload.charAt(start))) {
			start++;
		}
		if (start >= payload.length() || payload.charAt(start) != '"') {
			return null;
		}

		StringBuilder value = new StringBuilder();
		boolean escaped = false;
		for (int i = start + 1; i < payload.length(); i++) {
			char c = payload.charAt(i);
			if (escaped) {
				value.append(unescapeJsonChar(c));
				escaped = false;
			} else if (c == '\\') {
				escaped = true;
			} else if (c == '"') {
				return value.toString();
			} else {
				value.append(c);
			}
		}
		return null;
	}

	private static boolean jsonBooleanValue(String payload, String name, boolean fallback) {
		int nameIndex = jsonFieldIndex(payload, name);
		if (nameIndex < 0) {
			return fallback;
		}
		int colon = payload.indexOf(':', nameIndex);
		if (colon < 0) {
			return fallback;
		}
		int start = colon + 1;
		while (start < payload.length() && Character.isWhitespace(payload.charAt(start))) {
			start++;
		}
		if (payload.startsWith("true", start)) {
			return true;
		} else if (payload.startsWith("false", start)) {
			return false;
		}
		return fallback;
	}

	private static int jsonIntValue(String payload, String name, int fallback) {
		String number = jsonNumberValue(payload, name);
		if (number == null) {
			return fallback;
		}
		try {
			return Integer.parseInt(number);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static float jsonFloatValue(String payload, String name, float fallback) {
		String number = jsonNumberValue(payload, name);
		if (number == null) {
			return fallback;
		}
		try {
			return Float.parseFloat(number);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static String jsonNumberValue(String payload, String name) {
		int nameIndex = jsonFieldIndex(payload, name);
		if (nameIndex < 0) {
			return null;
		}
		int colon = payload.indexOf(':', nameIndex);
		if (colon < 0) {
			return null;
		}
		int start = colon + 1;
		while (start < payload.length() && Character.isWhitespace(payload.charAt(start))) {
			start++;
		}
		int end = start;
		if (end < payload.length() && payload.charAt(end) == '-') {
			end++;
		}
		while (end < payload.length()) {
			char c = payload.charAt(end);
			if (!Character.isDigit(c) && c != '.') {
				break;
			}
			end++;
		}
		return end == start ? null : payload.substring(start, end);
	}

	private static char unescapeJsonChar(char c) {
		switch (c) {
			case 'b':
				return '\b';
			case 'f':
				return '\f';
			case 'n':
				return '\n';
			case 'r':
				return '\r';
			case 't':
				return '\t';
			default:
				return c;
		}
	}

	private static int jsonFieldIndex(String payload, String name) {
		if (payload == null || name == null) {
			return -1;
		}
		return payload.indexOf("\"" + name + "\"");
	}

	private static int clampArmorTier(int tier) {
		return Math.max(0, Math.min(6, tier));
	}
}
