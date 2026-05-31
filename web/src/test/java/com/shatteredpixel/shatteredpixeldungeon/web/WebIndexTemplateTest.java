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

package com.shatteredpixel.shatteredpixeldungeon.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class WebIndexTemplateTest {

	@Test
	public void gameBootstrapStartsOnLoadWithoutStartGate() throws IOException {
		String html = readIndexTemplate();
		assertFalse(html.contains("audio-start-gate"));
		assertFalse(html.contains("startGameFromGesture"));
		assertTrue(html.contains("function installCanvasInputOffsetShim()"));
		assertTrue(html.contains("function focusGameCanvas()"));
		assertTrue(html.contains("function focusActiveGameSurface()"));
		assertTrue(html.contains("offsetTop: {"));
		assertTrue(html.contains("function installDirectStartHowlerPolicy()"));
		assertTrue(html.contains("MULTIPLAYER_TRYSTERO_MODULE"));
		assertTrue(html.contains("trystero@0.21.8/torrent/+esm"));
		assertTrue(html.contains("MULTIPLAYER_TRYSTERO_CONFIG"));
		assertTrue(html.contains("relayRedundancy: 1"));
		assertTrue(html.contains("window.__shpdMultiplayer"));
		assertTrue(html.contains("window.__shpdMultiplayerRooms"));
		assertTrue(html.contains("playerName: \"\""));
		assertTrue(html.contains("playerColor: \"\""));
		assertTrue(html.contains("playerSeatOrder: \"\""));
		assertTrue(html.contains("roomEpoch: 0"));
		assertTrue(html.contains("playerName: participant && participant.playerName"));
		assertTrue(html.contains("function participantDisplayColor(participant, fallback)"));
		assertTrue(html.contains("participant.participantColor"));
		assertTrue(html.contains("playerColor: participantDisplayColor(participant, session.participantColor)"));
		assertTrue(html.contains("roomEpoch: snapshot.roomEpoch || session.roomEpoch || 1"));
		assertTrue(html.contains("participant.playerName || \"\""));
		assertTrue(html.contains("get playerName()"));
		assertTrue(html.contains("get playerColor()"));
		assertTrue(html.contains("get playerSeatOrder()"));
		assertTrue(html.contains("get roomEpoch()"));
		assertTrue(html.contains("requestRoom: requestMultiplayerRoomSession"));
		assertTrue(html.contains("requestAction: requestMultiplayerRoomAction"));
		assertTrue(html.contains("pollEvent: function()"));
		assertTrue(html.contains("function clearMultiplayerRoomEvents()"));
		assertTrue(html.contains("leaveRoom: function()"));
		assertTrue(html.contains("function createMultiplayerRoomSession(mode, input)"));
		assertTrue(html.contains("function startMultiplayerRoomTransport(session)"));
		assertTrue(html.contains("function participantForId(snapshot, participantId)"));
		assertTrue(html.contains("function requestMultiplayerRoomAction(action, value)"));
		assertTrue(html.contains("clearReadyAndCountdownForRuleChange"));
		assertTrue(html.contains("function startBlockedReason(snapshot)"));
		assertTrue(html.contains("function applyStartToggle(runtime)"));
		assertTrue(html.contains("function requestMultiplayerRoomLeave()"));
		assertTrue(html.contains("function deterministicOwnerTransferId(snapshot, previousOwnerId)"));
		assertTrue(html.contains("function applyParticipantLeave(runtime, participantId, pushLocalEvent)"));
		assertTrue(html.contains("MULTIPLAYER_ROOM_RECONNECT_GRACE_MS"));
		assertTrue(html.contains("function deriveMultiplayerReconnectTokenProof(roomSecret, participantId, reconnectToken)"));
		assertTrue(html.contains("function loadStoredRoomSessionForReconnect(normalizedRoomName, roomSecret, playerName)"));
		assertTrue(html.contains("function loadStoredRoomSessionForActivePlayerReturn(normalizedRoomName, roomSecret, playerName)"));
		assertTrue(html.contains("function loadStoredRoomSessionForWatchReturn(normalizedRoomName, roomSecret, playerName)"));
		assertTrue(html.contains("function roomReturnDeadlineExpired(deadlineMs)"));
		assertTrue(html.contains("function storedRoomSessionReconnectDeadlineMs(session)"));
		assertTrue(html.contains("function markStoredRoomSessionReconnectDeadline(session, deadlineMs, reason)"));
		assertTrue(html.contains("function activeRunSnapshotAvailableForReturn(session)"));
		assertTrue(html.contains("function markStoredActiveRoomSessionReturnDeadline(config, deadlineMs, reason)"));
		assertTrue(html.contains("function createFreshJoinSessionFromExpiredReconnect(expiredSession)"));
		assertTrue(html.contains("function markLobbyParticipantDisconnected(runtime, participantId)"));
		assertTrue(html.contains("function markLobbyOwnerDisconnected(runtime, participantId)"));
		assertTrue(html.contains("function applyParticipantReconnect(runtime, payload, peerId)"));
		assertTrue(html.contains("function reconnectBlockedReason(snapshot, participantId)"));
		assertTrue(html.contains("function reconnectRejectionMessage(reason)"));
		assertTrue(html.contains("function scheduleLobbyDisconnectCleanup(runtime, participantId, deadlineMs, disconnectSequence)"));
		assertTrue(html.contains("function cleanupLobbyDisconnectedParticipant(runtime, participantId, deadlineMs, disconnectSequence)"));
		assertTrue(html.contains("function generateMultiplayerParticipantAuthKeys()"));
		assertTrue(html.contains("function stripMultiplayerRoomSnapshotSecrets(snapshot)"));
		assertTrue(html.contains("function verifyMultiplayerRoomEnvelope(session, envelope, payload)"));
		assertTrue(html.contains("function roomPayloadAuthPublicKey(session, payload, senderParticipantId)"));
		assertTrue(html.contains("creatorAuthPublicKey: session.authPublicKey || null"));
		assertTrue(html.contains("authPublicKey: session.authPublicKey || null"));
		assertTrue(html.contains("await signMultiplayerRoomEnvelope(session, envelope);"));
		assertTrue(html.contains("runtime.peerParticipants[peerId] = payload.senderParticipantId"));
		assertTrue(html.contains("room.onPeerLeave((peerId) =>"));
		assertTrue(html.contains("participantId === activeSession.roomOwnerParticipantId"));
		assertTrue(html.contains("markLobbyOwnerDisconnected(runtime, participantId)"));
		assertTrue(html.contains("requesterAuthPublicKey: session.authPublicKey || null"));
		assertTrue(html.contains("payload.requesterAuthPublicKey || null"));
		assertTrue(html.contains("snapshotTransferSequence > currentTransferSequence"));
		assertTrue(html.contains("participant.reconnectDeadlineMs = deadlineMs"));
		assertTrue(html.contains("if (!participant.authPublicKey)"));
		assertTrue(html.contains("String(payload && payload.senderParticipantId || \"\") !== participantId"));
		assertTrue(html.contains("participant.connected = true"));
		assertTrue(html.contains("delete participant.reconnectDeadlineMs"));
		assertTrue(html.contains("phase = \"reconnect-handshake\""));
		assertTrue(html.contains("type: \"room-reconnect-request\""));
		assertTrue(html.contains("type: \"room-reconnect-rejected\""));
		assertTrue(html.contains("removeMultiplayerRoomSession(session)"));
		assertTrue(html.contains("snapshot.playerSeats[seatIndex].ready = false"));
		assertTrue(html.contains("function sendParticipantIntentPayload(runtime, action, value)"));
		assertTrue(html.contains("function scheduleRoomLaunchIfOwner(runtime)"));
		assertTrue(html.contains("function launchMultiplayerRoom(runtime)"));
		assertTrue(html.contains("function pushRoomLaunchEvent(session)"));
		assertTrue(html.contains("function installLaunchedMultiplayerRuntime(session)"));
		assertTrue(html.contains("function startPlayerWatchReturn(session)"));
		assertTrue(html.contains("function pushRoomWatchReturnLaunchEvent(session, config)"));
		assertTrue(html.contains("function snapshotCanUpdateRoomSession(session, snapshot, expectedPhase)"));
		assertTrue(html.contains("function snapshotCanUpdateLobbyFromSender(session, snapshot, senderParticipantId)"));
		assertTrue(html.contains("function participantIntentMatchesRoomEpoch(session, intent)"));
		assertTrue(html.contains("MULTIPLAYER_ROOM_ACTIVE_SESSION_KEY"));
		assertTrue(html.contains("function loadRoomActiveMultiplayerConfig()"));
		assertTrue(html.contains("function saveRoomActiveMultiplayerConfig(config)"));
		assertTrue(html.contains("shpd-owner-transfer-v1"));
		assertTrue(html.contains("ownerTransferSequence"));
		assertTrue(html.contains("applyParticipantLeave(runtime, session.participantId, false)"));
		assertTrue(html.contains("function roomCreateRaceKey(normalizedRoomName, creatorParticipantId, createNonce)"));
		assertTrue(html.contains("createRaceKey"));
		assertTrue(html.contains("joinRequestId"));
		assertTrue(html.contains("room.makeAction(\"room-msg\")"));
		assertTrue(html.contains("type: \"room-create-probe\""));
		assertTrue(html.contains("payload.snapshot.roomPhase === \"active\""));
		assertTrue(html.contains("payload.type === \"room-create-probe\" && session.phase === \"active\""));
		assertTrue(html.contains("type: \"room-join-request\""));
		assertTrue(html.contains("type: \"room-join-rejected\""));
		assertTrue(html.contains("requestId: session.joinRequestId"));
		assertTrue(html.contains("targetParticipantId"));
		assertTrue(html.contains("snapshotContainsParticipant"));
		assertTrue(html.contains("action === \"floor-turns\""));
		assertTrue(html.contains("action === \"floor-infinite\""));
		assertTrue(html.contains("action === \"ready-toggle\""));
		assertFalse(html.contains("action === \"role-toggle\""));
		assertTrue(html.contains("action === \"hero-choice\""));
		assertTrue(html.contains("intent.action === \"leave\""));
		assertTrue(html.contains("action === \"start-toggle\""));
		assertTrue(html.contains("type: \"room-launch\""));
		assertTrue(html.contains("snapshot.roomPhase = \"active\""));
		assertTrue(html.contains("payload.snapshot.roomOwnerParticipantId === payload.senderParticipantId"));
		assertTrue(html.contains("snapshotCanUpdateRoomSession(session, payload.snapshot, \"lobby\")"));
		assertTrue(html.contains("snapshotCanUpdateRoomSession(session, payload.snapshot, \"active\")"));
		assertTrue(html.contains("!participantIntentMatchesRoomEpoch(session, intent)"));
		assertTrue(html.contains("session.transportRoomId + \".active\""));
		assertTrue(html.contains("authPrivateKey: session.authPrivateKey || null"));
		assertTrue(html.contains("participantAuthPublicKeys: participantAuthPublicKeysForSnapshot(snapshot)"));
		assertTrue(html.contains("function signActivePayload(action, payload)"));
		assertTrue(html.contains("async function verifyActivePayload(action, payload, peerId, senderField)"));
		assertTrue(html.contains("floorChaseTurns: session.snapshot && session.snapshot.floorChaseTurns || MULTIPLAYER_ROOM_DEFAULT_FLOOR_CHASE_TURNS"));
		assertTrue(html.contains("floorChaseInfinite: !!(session.snapshot && session.snapshot.floorChaseInfinite)"));
		assertTrue(html.contains("launchConfig = launchConfig || loadRoomActiveMultiplayerConfig()"));
		assertTrue(html.contains("type: \"room-participant-intent\""));
		assertTrue(html.contains("type: \"room-intent-result\""));
		assertTrue(html.contains("handledIntentIds"));
		assertTrue(html.contains("room-name-conflict"));
		assertTrue(html.contains("MULTIPLAYER_ROOM_SOURCE_ROOM_UI"));
		assertTrue(html.contains("MULTIPLAYER_ROOM_SOURCE_DEV_URL"));
		assertTrue(html.contains(": MULTIPLAYER_ROOM_SOURCE_DEV_URL"));
		assertTrue(html.contains("AES-GCM"));
		assertTrue(html.contains("MULTIPLAYER_ROOM_SESSION_KEY_PREFIX"));
		assertFalse(html.contains("transportRoomId: session.transportRoomId"));
		assertFalse(html.contains("shpd-room-backdrop"));
		assertTrue(html.contains("function installWebMultiplayer(launchConfig)"));
		assertTrue(html.contains("disconnect: function()"));
		assertTrue(html.contains("finishRoom: function()"));
		assertTrue(html.contains("function cleanupMultiplayerRoom(reason)"));
		assertTrue(html.contains("function markActiveRoomReturnDeadline(reason)"));
		assertTrue(html.contains("room.leave()"));
		assertTrue(html.contains("window.clearInterval(state.retryIntervalId)"));
		assertTrue(html.contains("window.addEventListener(\"pagehide\", cleanupMultiplayerRoomOnPageHide"));
		assertTrue(html.contains("reloadAfterMultiplayerCleanup"));
		assertTrue(html.contains("consumeResumeSlot"));
		assertTrue(html.contains("resumeSlot"));
		assertTrue(html.contains("isWatcher"));
		assertTrue(html.contains("watchTargetId"));
		assertTrue(html.contains("playerSeatOrderForSnapshot"));
		assertTrue(html.contains("params.get(\"mpdebug\") === \"1\""));
		assertTrue(html.contains("multiplayerDebug"));
		assertTrue(html.contains("MULTIPLAYER_WATCHER_SLOT = 0"));
		assertTrue(html.contains("MULTIPLAYER_WATCHER_KEYFRAME_RETRY_MS = 1500"));
		assertTrue(html.contains("restoreBrowserDataSnapshotToWatcherSlot"));
		assertTrue(html.contains("consumeWatcherResumeRequested"));
		assertTrue(html.contains("restoreWatcherKeyframe"));
		assertTrue(html.contains("requestWatcherKeyframe"));
		assertTrue(html.contains("function scheduleWatcherKeyframeRetry()"));
		assertTrue(html.contains("function clearWatcherKeyframeRetry()"));
		assertTrue(html.contains("state.keyframeRetryTimeoutId = window.setTimeout(() =>"));
		assertTrue(html.contains("if (state.replayReady) {\n"
				+ "                        return;\n"
				+ "                    }"));
		assertTrue(html.contains("state.keyframeRequested = false;\n"
				+ "                        requestWatcherKeyframeIfNeeded(false);"));
		assertTrue(html.contains("keyframeFailureStatusPushed: false"));
		assertTrue(html.contains("if (!state.keyframeFailureStatusPushed)"));
		assertTrue(html.contains("multiplayer watcher keyframe restore retry failed"));
		assertTrue(html.contains("clearWatcherKeyframeRetry();\n"
				+ "                            if (state.participantSource === MULTIPLAYER_ROOM_SOURCE_ROOM_UI"));
		assertTrue(html.contains("pushStatus(\"Loaded watcher keyframe. Reloading...\");\n"
				+ "                                pushRoomUiWatcherHandoffStatus(\"Loaded watcher keyframe. Reloading...\");\n"
				+ "                                reloadAfterMultiplayerCleanup(multiplayerUrlWithoutCloneFlag());"));
		assertTrue(html.contains("multiplayer watcher keyframe reloading for room-ui"));
		assertTrue(html.contains("pushStatus(\"Loaded watcher keyframe. Reloading...\");"));
		assertTrue(html.contains("function pushRoomUiWatcherHandoffStatus(message)"));
		assertTrue(html.contains("pushMultiplayerRoomEvent(\"room-status\", [message])"));
		assertTrue(html.contains("Requested keyframe from player"));
		assertTrue(html.contains("Watcher keyframe restore failed"));
		assertTrue(html.contains("switchWatchTarget"));
		assertTrue(html.contains("continueAsWatcher"));
		assertFalse(html.contains("Continuing as watcher for player"));
		assertTrue(html.contains("watchReturn"));
		assertTrue(html.contains("rejectWatchReturnRoomFull"));
		assertTrue(html.contains("announceReplayEvent"));
		assertTrue(html.contains("replayReady"));
		assertTrue(html.contains("handlePeerMirrorReplay"));
		assertTrue(html.contains("requestPeerMirrorKeyframe"));
		assertTrue(html.contains("applyPeerMirrorKeyframe"));
		assertTrue(html.contains("peerMirrorKeyframeRequests"));
		assertTrue(html.contains("localMirrorStatusMessage"));
		assertTrue(html.contains("mirrorState"));
		assertTrue(html.contains("requesterRole: \"peer-mirror\""));
		assertTrue(html.contains("Peer Mirror for player"));
		assertTrue(html.contains("recovered from a Keyframe"));
		assertTrue(html.contains("debugOnly"));
		assertTrue(html.contains("!debugOnly || state.multiplayerDebug"));
		assertTrue(html.contains("logWebParity(\"multiplayer status\", {"));
		assertTrue(html.contains("state.participantSource === MULTIPLAYER_ROOM_SOURCE_ROOM_UI"));
		assertTrue(html.contains("payload.mirrorState = state.localMirrorState"));
		assertTrue(html.contains("handlePeerMirrorReplay(data.mirrorState, \"status\")"));
		assertTrue(html.contains("debugOnly: true"));
		assertTrue(html.contains("debugOnly: mirrorOnly"));
		assertTrue(html.contains("Peer Mirror"));
		assertTrue(html.contains("peer-left"));
		assertTrue(html.contains("peer-returned"));
		assertTrue(html.contains("peer-timeout"));
		assertTrue(html.contains("activeDisconnectTimers"));
		assertTrue(html.contains("activeDisconnectedPlayers"));
		assertTrue(html.contains("activeTimedOutPlayers"));
		assertTrue(html.contains("function scheduleActivePeerDisconnectTimeout(remotePlayerId)"));
		assertTrue(html.contains("function clearActivePeerDisconnect(remotePlayerId)"));
		assertTrue(html.contains("MULTIPLAYER_ROOM_RECONNECT_GRACE_MS"));
		assertTrue(html.contains("peerRoles.set(peerId, data.role === \"watcher\" ? \"watcher\" : \"player\")"));
		assertTrue(html.contains("multiplayerUrlWithWatchTarget"));
		assertTrue(html.contains("Switched watch target"));
		assertTrue(html.contains("room.makeAction(\"hello\")"));
		assertTrue(html.contains("trystero.joinRoom(MULTIPLAYER_TRYSTERO_CONFIG, roomId)"));
		assertTrue(html.contains("room.makeAction(\"kf-req\")"));
		assertTrue(html.contains("room.makeAction(\"kf-res\")"));
		assertTrue(html.contains("room.makeAction(\"replay\")"));
		assertTrue(html.contains("watch-kf-req"));
		assertTrue(html.contains("Replay stream gap"));
		assertTrue(html.contains("Sent watcher keyframe"));
		assertTrue(html.contains("Loaded watcher keyframe"));
		assertTrue(html.contains("state.isHost || alreadyRestored"));
		assertTrue(html.contains("state.isWatcher || state.isHost || alreadyRestored"));
		assertTrue(html.contains("room.makeAction(\"snap-req\")"));
		assertTrue(html.contains("room.makeAction(\"floor\")"));
		assertFalse(html.contains("howler.usingWebAudio = false"));
		assertTrue(html.contains("howler.autoUnlock = true"));
		assertTrue(html.contains("audio direct start webaudio policy"));

		int loadStart = html.indexOf("function start()");
		int loadListener = html.indexOf("window.addEventListener(\"load\", start);");
		assertTrue(loadStart >= 0);
		assertTrue(loadListener > loadStart);
		assertTrue(html.substring(loadStart, loadListener).contains("gameStarted = true"));
		assertTrue(html.substring(loadStart, loadListener).contains("%MODE%"));
	}

	@Test
	public void playerWatchReturnStoredSessionDoesNotCreateWatcherEntry() throws IOException {
		String html = readIndexTemplate();
		String roomCreation = sectionBetween(html,
				"async function createMultiplayerRoomSession(mode, input)",
				"const participantId =");
		String roomTransport = sectionBetween(html,
				"function startMultiplayerRoomTransport(session)",
				"cleanupMultiplayerRoomEntry(\"new-room-request\")");
			String activeRuntime = sectionBetween(html,
					"function installWebMultiplayer(launchConfig)",
					"import(MULTIPLAYER_TRYSTERO_MODULE)");
			String roomRequest = sectionBetween(html,
					"return createMultiplayerRoomSession(mode, input).then((session) => {",
					"}).catch((error) => {");

			assertTrue(html.contains("function storedRoomSessionActivePlayerReturnCandidate(session,"));
			assertTrue(html.contains("playerSeatIndexForParticipant(session.snapshot, session.participantId) >= 0"));
			assertTrue(roomCreation.contains("const activeSession = loadStoredRoomSessionForActivePlayerReturn("));
			assertTrue(roomCreation.contains("activeSession.mode = \"active-return\";"));
			assertTrue(roomCreation.contains("activeSession.phase = \"active\";"));
			assertTrue(roomCreation.contains("activeSession.watchReturnEligible = false;"));
			assertFalse(roomCreation.contains("loadStoredRoomSessionForWatchReturn"));
			assertFalse(roomCreation.contains("watchReturnSession.mode = \"watch-return\""));
			assertTrue(roomRequest.contains("session.mode === \"active-return\" && session.phase === \"active\""));
			assertTrue(roomRequest.contains("restoreActiveRoomRunSnapshot(session);"));
			assertTrue(roomRequest.contains("saveRoomActiveMultiplayerConfig(launchedMultiplayerConfigForSession(session));"));
			assertTrue(roomRequest.contains("window.location.replace(multiplayerUrlWithoutCloneFlag())"));
			assertFalse(roomRequest.contains("pushRoomLaunchEvent(session);"));
			assertTrue(roomRequest.indexOf("restoreActiveRoomRunSnapshot(session);")
					< roomRequest.indexOf("startMultiplayerRoomTransport(session);"));
			assertTrue(html.contains("const MULTIPLAYER_ACTIVE_RETURN_SLOT = 7;"));
			assertTrue(html.contains("function restoreActiveRoomRunSnapshot(session)"));
			assertTrue(html.contains("restoreBrowserDataSnapshotToSlot("));
			assertTrue(roomTransport.contains("Watcher return is disabled."));
		assertTrue(roomTransport.contains("removeMultiplayerRoomSession(session);"));
		assertTrue(html.contains("function startPlayerWatchReturn(session) {\n"
				+ "                pushMultiplayerRoomEvent(\"room-error\", [\"Watcher return is disabled.\"]);"));
		assertTrue(activeRuntime.contains("continueAsWatcher: function(targetId) {\n"
				+ "                        return false;\n"
				+ "                    }"));
		assertTrue(activeRuntime.contains("saveActiveRunSnapshot: function(sourceSlot, snapshotFilesJson)"));
		assertTrue(activeRuntime.contains("markWatchReturnEligible: function(reason) {\n"
				+ "                        return false;\n"
				+ "                    }"));
		assertFalse(activeRuntime.contains("markActiveRoomSessionWatchReturnEligible(0, \"continue-watch\")"));
		assertTrue(activeRuntime.contains("debugInjectPeerHello: function(remotePlayerId, role, mirrorState)"));
		assertTrue(activeRuntime.contains("if (!state.multiplayerDebug || typeof state.debugInjectPeerHello !== \"function\")"));
		assertTrue(activeRuntime.contains("return state.debugInjectPeerHello(remotePlayerId, role, mirrorState);"));
		assertTrue(activeRuntime.contains("debugPeerSnapshot: null"));
		assertTrue(activeRuntime.contains("debugPeerSnapshot: function()"));
		assertTrue(activeRuntime.contains("typeof state.debugPeerSnapshot !== \"function\""));
		assertTrue(activeRuntime.contains("return state.debugPeerSnapshot();"));
		assertTrue(activeRuntime.contains("pendingWatcherKeyframes: new Map()"));
		assertTrue(activeRuntime.contains("function deferWatcherKeyframeResponse(data, peerId)"));
		assertTrue(activeRuntime.contains("state.pendingWatcherKeyframes.set(pendingWatcherKeyframeKey(watcherId, requestId)"));
		assertTrue(activeRuntime.contains("javaEvents.push(multiplayerEncodeEvent(\"watch-kf-req\""));
		assertTrue(activeRuntime.contains("function sendPendingWatcherKeyframe(watcherId, requestId, snapshotFilesJson)"));
		assertTrue(activeRuntime.contains("sendWatcherKeyframe: function(watcherId, requestId, snapshotFilesJson)"));
		assertTrue(activeRuntime.contains("debugSaveDigest: function(slot)"));
		assertTrue(activeRuntime.contains("debugAnnounceProgress: function(depth, branch, turns)"));
		assertTrue(activeRuntime.contains("!state.multiplayerDebug || state.isWatcher || !state.ready"));
		assertTrue(activeRuntime.contains("sendProgressIfReady();"));
		assertTrue(activeRuntime.contains("debugAdvanceTurns: function(turns)"));
		assertTrue(activeRuntime.contains("multiplayerEncodeEvent(\"debug-turns\""));
		assertTrue(activeRuntime.contains("debugAnnounceRoomOutcome: function(message)"));
		assertTrue(activeRuntime.contains("state.localRoomOutcomeMessage = message;"));
		assertTrue(activeRuntime.contains("sendHelloIfReady();\n"
				+ "                        logWebParity(\"multiplayer debug room outcome announced\""));
		assertTrue(activeRuntime.contains("debugState: function()"));
		assertTrue(activeRuntime.contains("peerSnapshot: typeof state.debugPeerSnapshot === \"function\""));
		assertTrue(html.contains("state.debugPeerSnapshot = function()"));
		assertTrue(html.contains("for (const entry of peerPlayers.entries())"));
		assertTrue(html.contains("role: peerRoles.get(entry[0]) || \"player\""));
		assertTrue(html.contains("payload.mirrorState = Object.assign({}, mirrorState);"));
		assertTrue(html.contains("payload.mirrorState.playerId = String(payload.mirrorState.playerId || remotePlayerId);"));
		assertTrue(html.contains("function handleHello(data, peerId)"));
		assertTrue(activeRuntime.contains("function markActiveRoomSessionWatchReturnEligible(delayMs, reason)"));
		assertFalse(activeRuntime.contains("markActiveRoomSessionWatchReturnEligible(0, \"continue-watch\")"));
		assertTrue(activeRuntime.contains("markWatchReturnEligible: function(reason)"));
		assertFalse(activeRuntime.contains("markActiveRoomSessionWatchReturnEligible(0, \"manual\")"));
		assertFalse(activeRuntime.contains("MULTIPLAYER_ROOM_RECONNECT_GRACE_MS, \"disconnect-grace\""));
		assertTrue(activeRuntime.contains("for (const disconnectedPlayerId of state.activeDisconnectedPlayers.values())"));
		assertTrue(html.contains("state.retryIntervalId = window.setInterval(() => {\n"
				+ "                        sendHelloIfReady();"));
		assertTrue(activeRuntime.contains("pushMultiplayerRoomEvent(\"room-error\", [\"room full\"])"));
		assertTrue(activeRuntime.contains("cleanupMultiplayerRoom(\"watch-return-room-full\")"));
		assertTrue(html.contains("window.setTimeout(() => acceptWatchReturnIfNotFull(peerPlayers)"));
		assertTrue(activeRuntime.contains("finishRoom: function()"));
		assertTrue(activeRuntime.contains("removeStoredActiveRoomSessionForConfig({"));
	}

	@Test
	public void roomSnapshotsDoNotBroadcastReconnectProofs() throws IOException {
		String html = readIndexTemplate();
		String participant = sectionBetween(html,
				"function participantFromSession(session, owner)",
				"function participantForId(snapshot, participantId)");
		String sanitizer = sectionBetween(html,
				"function stripMultiplayerRoomSnapshotSecrets(snapshot)",
				"function saveMultiplayerRoomSession(session)");
		String reconnectRequest = sectionBetween(html,
				"function sendReconnectRequest(runtime, peerId)",
				"function clearRoomHandshakeRetry(runtime)");

		assertTrue(participant.contains("authPublicKey: session.authPublicKey || null"));
		assertFalse(participant.contains("reconnectTokenProof"));
		assertTrue(sanitizer.contains("delete participant.reconnectToken;"));
		assertTrue(sanitizer.contains("delete participant.reconnectTokenProof;"));
		assertTrue(reconnectRequest.contains("targetParticipantId: session.participantId"));
		assertTrue(reconnectRequest.contains("requesterAuthPublicKey: session.authPublicKey || null"));
		assertFalse(reconnectRequest.contains("reconnectTokenProof"));
	}

	@Test
	public void roomUiActiveMessagesAreSignedByParticipantIdentity() throws IOException {
		String html = readIndexTemplate();
		String activeRuntime = sectionBetween(html,
				"function installWebMultiplayer(launchConfig)",
				"installWebMultiplayer();");

		assertTrue(activeRuntime.contains("authPrivateKey: launchConfig ? launchConfig.authPrivateKey || null : null"));
		assertTrue(activeRuntime.contains("participantAuthPublicKeys: launchConfig && launchConfig.participantAuthPublicKeys"));
		assertTrue(activeRuntime.contains("function activePayloadSignatureRequired()"));
		assertTrue(activeRuntime.contains("function signActivePayload(action, payload)"));
		assertTrue(activeRuntime.contains("async function verifyActivePayload(action, payload, peerId, senderField)"));
		assertTrue(activeRuntime.contains("senderParticipantId: playerId"));
		assertTrue(activeRuntime.contains("state.activePeerParticipants.set(peerId, senderParticipantId);"));
		assertTrue(activeRuntime.contains("sendMultiplayerActivePayload(action, activePayloadForRoomRun(payload), sendAction, peerId, signActivePayload);"));
		assertTrue(activeRuntime.contains("getHello(async (data, peerId) =>"));
		assertTrue(activeRuntime.contains("await verifyActivePayload(\"hello\", data, peerId, \"playerId\")"));
		assertTrue(activeRuntime.contains("await verifyActivePayload(\"floor-progress\", data, peerId, \"playerId\")"));
		assertTrue(activeRuntime.contains("await verifyActivePayload(\"replay\", data, peerId, \"playerId\")"));
		assertTrue(activeRuntime.contains("function activePlayerHasLivePeer(remotePlayerId)"));
		assertTrue(activeRuntime.contains("const remotePlayerId = peerPlayers.get(peerId) || state.activePeerParticipants.get(peerId);"));
		assertTrue(activeRuntime.contains("&& !activePlayerHasLivePeer(remotePlayerId)"));
		assertTrue(activeRuntime.contains("state.activePeerParticipants.delete(peerId);"));
	}

	@Test
	public void storedRoomReturnsExpireBeforeReusingParticipantIdentity() throws IOException {
		String html = readIndexTemplate();
		String activeConfig = sectionBetween(html,
				"function loadRoomActiveMultiplayerConfig()",
				"function multiplayerUrlWithoutCloneFlag()");
		String storageHelpers = sectionBetween(html,
				"function removeMultiplayerRoomSession(session)",
				"function storedRoomSessionMatchesIdentity(session, normalizedRoomName, roomSecret, playerName)");
		String reconnectLoad = sectionBetween(html,
				"function loadStoredRoomSessionForReconnect(normalizedRoomName, roomSecret, playerName)",
				"function storedRoomSessionActivePlayerReturnCandidate(session,");
		String activeLoad = sectionBetween(html,
				"function loadStoredRoomSessionForActivePlayerReturn(normalizedRoomName, roomSecret, playerName)",
				"function storedRoomSessionWatchReturnCandidate(session, normalizedRoomName, roomSecret, playerName)");
		String restoreActive = sectionBetween(html,
				"function restoreActiveRoomRunSnapshot(session)",
				"function roomWatchReturnLaunchEventValues(session, config)");
		String roomEntryCleanup = sectionBetween(html,
				"function cleanupMultiplayerRoomRuntime(runtime, reason)",
				"async function sendEncryptedRoomPayload(runtime, payload, peerId)");
		String roomEntryTransport = sectionBetween(html,
				"function startMultiplayerRoomTransport(session)",
				"}).catch((error) => {");
		String activeRuntime = sectionBetween(html,
				"function installWebMultiplayer(launchConfig)",
				"installWebMultiplayer();");

		assertTrue(activeConfig.contains("roomReturnDeadlineExpired(config.activeReturnDeadlineMs)"));
		assertTrue(activeConfig.contains("removeStoredActiveRoomSessionForConfig(config);"));
		assertTrue(activeConfig.contains("storage.removeItem(MULTIPLAYER_ROOM_ACTIVE_SESSION_KEY);"));
		assertTrue(storageHelpers.contains("const directDeadline = Number(session && session.reconnectDeadlineMs) || 0;"));
		assertTrue(storageHelpers.contains("participant && participant.connected === false"));
		assertTrue(storageHelpers.contains("return Number(participant.reconnectDeadlineMs) || 0;"));
		assertTrue(storageHelpers.contains("session.reconnectDeadlineMs = deadlineMs;"));
		assertTrue(storageHelpers.contains("session.reconnectDeadlineReason = String(reason || \"\");"));
		assertTrue(reconnectLoad.contains("roomReturnDeadlineExpired(storedRoomSessionReconnectDeadlineMs(session))"));
		assertTrue(reconnectLoad.contains("storage.removeItem(key);"));
		assertTrue(reconnectLoad.indexOf("roomReturnDeadlineExpired(storedRoomSessionReconnectDeadlineMs(session))")
				< reconnectLoad.indexOf("storedRoomSessionReconnectCandidate(session"));
		assertTrue(activeLoad.contains("!activeRunSnapshotAvailableForReturn(session)"));
		assertTrue(activeLoad.contains("removeActiveRunSnapshotForSession(session);"));
		assertTrue(activeLoad.indexOf("!activeRunSnapshotAvailableForReturn(session)")
				< activeLoad.indexOf("const savedAt = Number(session.savedAt || session.createdAt) || 0;"));
		assertTrue(restoreActive.contains("roomReturnDeadlineExpired(activeReturnDeadlineMs(session, saved))"));
		assertTrue(restoreActive.contains("Room reconnect expired. Join again."));
		assertTrue(roomEntryCleanup.contains("window.removeEventListener(\"pagehide\", cleanupMultiplayerRoomEntryOnPageHide, true);"));
		assertTrue(roomEntryCleanup.contains("markStoredRoomSessionReconnectDeadline(runtime.session,"));
		assertTrue(roomEntryCleanup.contains("currentRoomReturnDeadlineMs(), reason);"));
		assertTrue(roomEntryCleanup.contains("function cleanupMultiplayerRoomEntryOnPageHide()"));
		assertTrue(roomEntryTransport.contains("window.addEventListener(\"pagehide\", cleanupMultiplayerRoomEntryOnPageHide"));
		assertTrue(activeRuntime.contains("function markActiveRoomReturnDeadline(reason)"));
		assertTrue(activeRuntime.contains("saveRoomActiveMultiplayerConfig(config);"));
		assertTrue(activeRuntime.contains("markStoredActiveRoomSessionReturnDeadline(config, deadlineMs, reason);"));
		assertTrue(activeRuntime.contains("markActiveRunSnapshotReturnDeadline(restoreKey, deadlineMs, reason);"));
		assertTrue(activeRuntime.contains("markActiveRoomReturnDeadline(reason);"));
	}

	@Test
	public void roomUiActiveMessagesCarryRoomRunIdentityAcrossRecreatedRooms() throws IOException {
		String html = readIndexTemplate();
		String createSession = sectionBetween(html,
				"async function createMultiplayerRoomSession(mode, input)",
				"const multiplayerRoomEvents = [];");
		String initialSnapshot = sectionBetween(html,
				"function initialMultiplayerRoomSnapshot(session)",
				"function snapshotContainsParticipant(snapshot, participantId)");
		String applySnapshot = sectionBetween(html,
				"function applyRoomSnapshotToSession(session, snapshot)",
				"function pushRoomLobbyEvent(session)");
		String launchHelpers = sectionBetween(html,
				"function launchSpecForParticipant(session, participantId)",
				"function sendRoomLaunch(runtime, peerId)");
		String activeRuntime = sectionBetween(html,
				"function installWebMultiplayer(launchConfig)",
				"installWebMultiplayer();");

		assertTrue(createSession.contains("roomRunId: createNonce"));
		assertTrue(initialSnapshot.contains("roomRunId: session.roomRunId || session.createNonce"));
		assertTrue(applySnapshot.contains("session.roomRunId = session.roomRunId || String(snapshot.roomRunId || snapshot.createNonce || \"\")"));
		assertTrue(launchHelpers.contains("function activeRoomRunIdForSession(session)"));
		assertTrue(launchHelpers.contains("roomRunId: activeRoomRunIdForSession(session)"));
		assertTrue(activeRuntime.contains("roomRunId: launchConfig ? String(launchConfig.roomRunId || \"\") : \"\""));
		assertTrue(activeRuntime.contains("const restoreKey = multiplayerRestoreKey(roomId, playerId, state.roomRunId);"));
		assertTrue(activeRuntime.contains("return multiplayerWatcherRestoreKey(roomId, playerId, state.watchTargetId, state.roomRunId);"));
		assertTrue(activeRuntime.contains("function activePayloadForRoomRun(payload)"));
		assertTrue(activeRuntime.contains("function activePayloadMatchesRoomRun(payload)"));
		assertTrue(activeRuntime.contains("if (!activePayloadMatchesRoomRun(payload))"));
		assertTrue(activeRuntime.contains("sendMultiplayerActivePayload(action, activePayloadForRoomRun(payload), sendAction, peerId, signActivePayload);"));
		assertTrue(activeRuntime.contains("roomRunId: state.roomRunId"));
	}

	@Test
	public void intentionalLobbyLeaveDoesNotReuseReconnectSession() throws IOException {
				String html = readIndexTemplate();
				String storageHelpers = sectionBetween(html,
					"function multiplayerRoomIdentityKey(prefix, identity)",
					"function removeStoredActiveRoomSessionForConfig(config)");
			String createSession = sectionBetween(html,
					"async function createMultiplayerRoomSession(mode, input)",
					"function initialMultiplayerRoomSnapshot(session)");
			String reconnectCandidate = sectionBetween(html,
					"function storedRoomSessionReconnectCandidate(session, normalizedRoomName, roomSecret, playerName)",
					"function loadStoredRoomSessionForReconnect(normalizedRoomName, roomSecret, playerName)");
		String reconnectRejected = sectionBetween(html,
				"if (payload.type === \"room-reconnect-rejected\"",
				"if (payload.type === \"room-join-rejected\"");
			String encryptedRoomMessage = sectionBetween(html,
					"async function handleEncryptedRoomMessage(runtime, envelope, peerId)",
					"if (payload.type === \"room-snapshot\" && payload.snapshot)");
			String peerLeave = sectionBetween(html,
					"room.onPeerLeave((peerId) =>",
					"if (session.phase === \"create-probe\")");
			String cleanup = sectionBetween(html,
					"function cleanupMultiplayerRoomRuntime(runtime, reason)",
					"async function sendEncryptedRoomPayload(runtime, payload, peerId)");
			String finishLeave = sectionBetween(html,
					"function finishMultiplayerRoomLeave(runtime, session)",
					"function requestMultiplayerRoomLeave()");
			String leave = sectionBetween(html,
					"function requestMultiplayerRoomLeave()",
					"function clearReadyAndCountdownForRuleChange(snapshot)");
			String promoteCreate = sectionBetween(html,
					"function promoteCreateProbeToOwner(runtime)",
					"function handleAcceptedRoomSnapshot(runtime, snapshot)");
			String acceptedSnapshot = sectionBetween(html,
					"function handleAcceptedRoomSnapshot(runtime, snapshot)",
					"function handleUpdatedRoomSnapshot(runtime, snapshot)");

			assertTrue(storageHelpers.contains("session && session.intentionalLeave === true"));
			assertTrue(storageHelpers.contains("function markStoredRoomIdentityIntentionallyLeft(identity)"));
			assertTrue(storageHelpers.contains("function storedRoomIdentityWasIntentionallyLeft(identity)"));
			assertTrue(storageHelpers.contains("function clearStoredRoomIdentityLeaveTombstone(identity)"));
			assertTrue(storageHelpers.contains("function removeStoredRoomSessionsForIdentity(identity, keepParticipantId)"));
			assertTrue(storageHelpers.contains("if (keepParticipantId && session.participantId === keepParticipantId)"));
			assertTrue(storageHelpers.contains("function acceptStoredRoomIdentityEntry(session)"));
			assertTrue(storageHelpers.contains("removeStoredRoomSessionsForIdentity(session, session.participantId);"));
			assertTrue(storageHelpers.contains("clearStoredRoomIdentityLeaveTombstone(session);"));
			assertTrue(storageHelpers.contains("storedRoomSessionMatchesIdentity(session, normalizedRoomName, roomSecret, playerName)"));
			assertFalse(html.contains("MULTIPLAYER_ROOM_LEAVE_TOMBSTONE_TTL_MS"));
			assertFalse(storageHelpers.contains("Date.now() - leftAt"));
			assertTrue(createSession.contains("const intentionallyLeft = storedRoomIdentityWasIntentionallyLeft(identity);"));
			assertTrue(createSession.contains("if (mode === \"join\" && intentionallyLeft)"));
			assertTrue(createSession.contains("removeStoredRoomSessionsForIdentity(identity);"));
			assertTrue(createSession.contains("if (mode === \"join\" && !intentionallyLeft)"));
			assertTrue(createSession.indexOf("if (mode === \"join\" && intentionallyLeft)")
					< createSession.indexOf("if (mode === \"join\" && !intentionallyLeft)"));
			assertFalse(createSession.contains("clearStoredRoomIdentityLeaveTombstone(session);"));
			assertTrue(reconnectCandidate.contains("session.intentionalLeave !== true"));
			assertTrue(reconnectCandidate.contains("session.phase === \"lobby\" || session.phase === \"reconnect-handshake\""));
			assertTrue(reconnectCandidate.contains("!!session.snapshot"));
			assertTrue(reconnectCandidate.contains("session.snapshot.roomPhase === \"lobby\""));
			assertTrue(reconnectCandidate.contains("snapshotContainsParticipant(session.snapshot, session.participantId)"));
			assertFalse(reconnectCandidate.contains("!session.snapshot ||"));
			assertFalse(reconnectCandidate.contains("!!session.reconnectToken"));
			assertTrue(promoteCreate.contains("acceptStoredRoomIdentityEntry(session);"));
			assertTrue(promoteCreate.indexOf("acceptStoredRoomIdentityEntry(session);")
					< promoteCreate.indexOf("saveMultiplayerRoomSession(session);"));
			assertTrue(acceptedSnapshot.contains("acceptStoredRoomIdentityEntry(session);"));
			assertTrue(acceptedSnapshot.indexOf("acceptStoredRoomIdentityEntry(session);")
					< acceptedSnapshot.indexOf("applyRoomSnapshotToSession(session, snapshot);"));
		assertTrue(reconnectRejected.contains("removeStoredRoomSessionsForIdentity(session);"));
		assertTrue(reconnectRejected.indexOf("removeStoredRoomSessionsForIdentity(session);")
				< reconnectRejected.indexOf("removeMultiplayerRoomSession(session);"));
		assertTrue(encryptedRoomMessage.contains("session.intentionalLeave === true"));
		assertTrue(encryptedRoomMessage.contains("payload.type === \"room-snapshot\" || payload.type === \"room-launch\""));
		assertTrue(peerLeave.contains("if (activeSession.intentionalLeave === true)"));
		assertTrue(cleanup.contains("function cleanupMultiplayerRoomEntryIfCurrent(runtime, reason)"));
		assertTrue(cleanup.contains("if (multiplayerRoomRuntime !== runtime)"));
		assertTrue(finishLeave.contains("const stillCurrentRuntime = multiplayerRoomRuntime === runtime;"));
		assertTrue(finishLeave.contains("const stillCurrentSession = window.__shpdMultiplayerRooms.currentSession === session;"));
			assertTrue(finishLeave.contains("if (stillCurrentRuntime || stillCurrentSession)"));
			assertTrue(finishLeave.contains("markStoredRoomIdentityIntentionallyLeft(session);"));
			assertTrue(finishLeave.contains("cleanupMultiplayerRoomEntryIfCurrent(runtime, \"manual\");"));
			assertTrue(finishLeave.contains("if (stillCurrentSession)"));
			assertTrue(leave.contains("session.intentionalLeave = true;"));
			assertTrue(leave.contains("markStoredRoomIdentityIntentionallyLeft(session);"));
			assertTrue(leave.contains("removeStoredRoomSessionsForIdentity(session);"));
			assertTrue(leave.contains("finishMultiplayerRoomLeave(runtime, session);"));
			assertTrue(leave.indexOf("session.intentionalLeave = true;")
					< leave.indexOf("markStoredRoomIdentityIntentionallyLeft(session);"));
			assertTrue(leave.indexOf("markStoredRoomIdentityIntentionallyLeft(session);")
					< leave.indexOf("removeStoredRoomSessionsForIdentity(session);"));
			assertTrue(leave.indexOf("session.intentionalLeave = true;")
					< leave.indexOf("applyParticipantLeave(runtime, session.participantId, false)"));
			assertTrue(leave.indexOf("session.intentionalLeave = true;")
							< leave.indexOf("sendParticipantIntentPayload(runtime, \"leave\", \"\")"));
		}

	@Test
	public void roomSessionStorageSeparatesRoomsAndPlayerIdentities() throws IOException {
		String html = readIndexTemplate();
		String sessionKey = sectionBetween(html,
				"function multiplayerRoomSessionKey(session)",
				"function multiplayerRoomIdentityKey(prefix, identity)");
		String identityKey = sectionBetween(html,
				"function multiplayerRoomIdentityKey(prefix, identity)",
				"function markStoredRoomIdentityIntentionallyLeft(identity)");
		String identityMatch = sectionBetween(html,
				"function storedRoomSessionMatchesIdentity(session, normalizedRoomName, roomSecret, playerName)",
				"function removeStoredRoomSessionsForIdentity(identity, keepParticipantId)");
		String removeIdentity = sectionBetween(html,
				"function removeStoredRoomSessionsForIdentity(identity, keepParticipantId)",
				"function acceptStoredRoomIdentityEntry(session)");

		assertTrue(sessionKey.contains("session.normalizedRoomName"));
		assertTrue(sessionKey.contains("+ \".\" + session.participantId"));
		assertFalse(sessionKey.contains("session.playerName"));
		assertTrue(identityKey.contains("identity.normalizedRoomName"));
		assertTrue(identityKey.contains("identity.roomSecret"));
		assertTrue(identityKey.contains("identity.playerName"));
		assertTrue(identityMatch.contains("session.normalizedRoomName === normalizedRoomName"));
		assertTrue(identityMatch.contains("session.roomSecret === roomSecret"));
		assertTrue(identityMatch.contains("session.playerName === playerName"));
		assertTrue(removeIdentity.contains("storedRoomSessionMatchesIdentity(session, normalizedRoomName, roomSecret, playerName)"));
		assertTrue(removeIdentity.contains("if (keepParticipantId && session.participantId === keepParticipantId)"));
	}

	@Test
	public void ownerLeaveTransfersLobbyWithoutKeepingCountdown() throws IOException {
		String html = readIndexTemplate();
		String leave = sectionBetween(html,
				"async function applyParticipantLeave(runtime, participantId, pushLocalEvent)",
				"function clearLobbyDisconnectCleanupTimer(runtime, participantId)");
		String senderGate = sectionBetween(html,
				"function snapshotCanUpdateLobbyFromSender(session, snapshot, senderParticipantId)",
				"function participantIntentMatchesRoomEpoch(session, intent)");
		String startToggle = sectionBetween(html,
				"function applyStartToggle(runtime)",
				"function remoteCreateRaceBeatsSession");

		assertTrue(leave.contains("const wasOwner = snapshot.roomOwnerParticipantId === participantId;"));
		assertTrue(leave.contains("snapshot.countdown = null;"));
		assertTrue(leave.contains("clearRoomLaunchTimer(runtime);"));
		assertTrue(leave.contains("snapshot.capacity.activeConnections = snapshot.participants.length;"));
		assertTrue(leave.contains("snapshot.ownerTransferSequence = (Number(snapshot.ownerTransferSequence) || 0) + 1;"));
		assertTrue(leave.contains("snapshot.roomOwnerParticipantId = await deterministicOwnerTransferId(snapshot, participantId);"));
		assertTrue(leave.contains("participant.owner = participant.participantId === snapshot.roomOwnerParticipantId;"));
		assertTrue(leave.contains("if (snapshot.participants.length > 0)"));
		assertTrue(leave.contains("sendCurrentRoomSnapshot(runtime);"));
		assertTrue(senderGate.contains("snapshotTransferSequence > currentTransferSequence"));
		assertTrue(senderGate.contains("senderParticipantId === snapshotOwnerParticipantId"));
		assertTrue(startToggle.contains("snapshot.countdown = null;"));
		assertTrue(startToggle.contains("clearRoomLaunchTimer(runtime);"));
	}

	@Test
	public void expiredReconnectRetriesAsFreshJoinButInvalidTokenDoesNot() throws IOException {
		String html = readIndexTemplate();
		String freshJoin = sectionBetween(html,
				"async function createFreshJoinSessionFromExpiredReconnect(expiredSession)",
				"async function createMultiplayerRoomSession(mode, input)");
		String storedReconnect = sectionBetween(html,
				"if (storedSession) {",
				"saveMultiplayerRoomSession(storedSession);");
		String reconnectHandler = sectionBetween(html,
				"if (payload.type === \"room-reconnect-request\"",
				"if (payload.type === \"room-join-request\" && session.phase === \"lobby\"");
		String reconnectApply = sectionBetween(html,
				"function applyParticipantReconnect(runtime, payload, peerId)",
				"function occupiedPlayerSeats(snapshot)");
		String reconnectRejected = sectionBetween(html,
				"if (payload.type === \"room-reconnect-rejected\"",
				"if (payload.type === \"room-join-rejected\"");
		String reconnectReasons = sectionBetween(html,
				"function reconnectRejectionMessage(reason)",
				"function rejectRoomCreateConflict(runtime)");

		assertTrue(freshJoin.contains("mode: \"join\""));
		assertTrue(freshJoin.contains("phase: \"join-handshake\""));
		assertTrue(freshJoin.contains("joinRequestId: \"jr-\" + multiplayerRandomBase64Url(12)"));
		assertTrue(freshJoin.contains("deriveMultiplayerReconnectTokenProof("));
		assertTrue(freshJoin.contains("expiredSession.roomSecret"));
		assertTrue(freshJoin.contains("expiredSession.transportRoomId"));
		assertTrue(freshJoin.contains("saveMultiplayerRoomSession(session);"));
		assertTrue(freshJoin.contains("return session;"));
		assertTrue(storedReconnect.contains("if (!storedSession.reconnectToken)"));
		assertTrue(storedReconnect.contains("storedSession.reconnectTokenProof = \"\";"));
		assertTrue(storedReconnect.contains("} else if (!storedSession.reconnectTokenProof)"));
		assertTrue(reconnectHandler.contains("session.roomOwnerParticipantId === session.participantId"));
		assertTrue(reconnectHandler.contains("String(payload.targetParticipantId || \"\") === session.snapshot.roomOwnerParticipantId"));
		assertTrue(reconnectApply.contains("participant.connected = true;"));
		assertFalse(reconnectApply.contains("participant.role = \"watcher\""));
		assertFalse(reconnectApply.contains("snapshot.watcherIds.push"));
		assertTrue(reconnectReasons.contains("function reconnectRejectedReasonIsExpired(reason)"));
		assertTrue(reconnectReasons.contains("normalized === \"reconnect expired\""));
		assertTrue(reconnectReasons.contains("normalized === \"reconnect expired. join again.\""));
		assertTrue(reconnectRejected.contains("const retryFreshJoin = reconnectRejectedReasonIsExpired(payload.reason);"));
		assertTrue(reconnectRejected.contains("const freshSession = await createFreshJoinSessionFromExpiredReconnect(session);"));
		assertTrue(reconnectRejected.contains("runtime.session = freshSession;"));
		assertTrue(reconnectRejected.contains("window.__shpdMultiplayerRooms.currentSession = freshSession;"));
		assertTrue(reconnectRejected.contains("sendJoinRequest(runtime, peerId);"));
		assertTrue(reconnectRejected.contains("expired-reconnect-fresh-join-timeout"));
		assertTrue(reconnectRejected.contains("window.__shpdMultiplayerRooms.currentSession = null;"));
		assertTrue(reconnectRejected.contains("cleanupMultiplayerRoomEntry(\"reconnect-rejected\");"));
		assertTrue(reconnectRejected.indexOf("if (retryFreshJoin)") < reconnectRejected.indexOf("cleanupMultiplayerRoomEntry(\"reconnect-rejected\")"));
		assertTrue(reconnectRejected.contains("pushMultiplayerRoomEvent(\"room-error\", [reconnectRejectionMessage(payload.reason)]);"));
		assertTrue(reconnectRejected.indexOf("window.__shpdMultiplayerRooms.currentSession = null;")
				< reconnectRejected.indexOf("pushMultiplayerRoomEvent(\"room-error\""));
		assertTrue(reconnectRejected.indexOf("if (retryFreshJoin)") < reconnectRejected.indexOf("pushMultiplayerRoomEvent(\"room-error\""));
	}

	@Test
	public void roomEntryStartsWithFreshEventQueue() throws IOException {
		String html = readIndexTemplate();
		String roomBridge = sectionBetween(html,
				"const multiplayerRoomEvents = [];",
				"function participantFromSession(session, owner)");
		String requestRoom = sectionBetween(html,
				"function requestMultiplayerRoomSession(mode, roomName, password, playerName, source)",
				"function requestMultiplayerRoomLeave()");

		assertTrue(roomBridge.contains("function clearMultiplayerRoomEvents()"));
		assertTrue(requestRoom.contains("clearMultiplayerRoomEvents();"));
		assertTrue(requestRoom.indexOf("clearMultiplayerRoomEvents();")
				< requestRoom.indexOf("return createMultiplayerRoomSession(mode, input)"));
	}

	@Test
	public void activeReplayDoesNotImplicitlyReconnectDisconnectedPeers() throws IOException {
		String html = readIndexTemplate();
		String activeRuntime = sectionBetween(html,
				"function installWebMultiplayer(launchConfig)",
				"installWebMultiplayer();");
		String replayHandler = sectionBetween(activeRuntime,
				"getReplayEvent(async (data, peerId) => {",
				"function requestSnapshotIfNeeded()");
		String helloHandler = sectionBetween(activeRuntime,
				"function handleHello(data, peerId)",
				"state.debugInjectPeerHello = function(remotePlayerId, role, mirrorState)");
		String progressHandler = sectionBetween(activeRuntime,
				"getProgress(async (data, peerId) => {",
				"getKeyframeRequest(async (data, peerId) => {");

		assertTrue(replayHandler.contains("const disconnected = state.activeDisconnectedPlayers.has(remotePlayerId);"));
		assertTrue(replayHandler.contains("handlePeerMirrorReplay(data, kind);\n"
				+ "                        if (disconnected) {\n"
				+ "                            return;\n"
				+ "                        }"));
		assertFalse(replayHandler.contains("clearActivePeerDisconnect"));
		assertTrue(progressHandler.contains("|| state.activeDisconnectedPlayers.has(remotePlayerId)"));
		assertFalse(progressHandler.contains("clearActivePeerDisconnect"));
		assertTrue(helloHandler.contains("clearActivePeerDisconnect(remotePlayerId);"));
	}

	@Test
	public void peerMirrorReplayConsumesNonVisualSequenceEventsWithoutForwardingRows() throws IOException {
		String html = readIndexTemplate();
		String peerMirrorHandler = sectionBetween(html,
				"function handlePeerMirrorReplay(data, kind)",
				"function clearActivePeerDisconnect(remotePlayerId)");

		assertTrue(peerMirrorHandler.contains(
				"const visibleMirrorKind = kind === \"move\" || kind === \"transition\" || kind === \"status\";"));
		assertTrue(peerMirrorHandler.contains("peerReplaySequences.set(remotePlayerId, seq);\n"
				+ "                        if (visibleMirrorKind) {\n"
				+ "                            enqueueReplayJavaEvent(data, kind);\n"
				+ "                        }"));
	}

	@Test
	public void ownerSessionEpochTracksJoinSnapshotBeforeParticipantIntents() throws IOException {
		String html = readIndexTemplate();
		String handler = sectionBetween(html,
				"if (payload.type === \"room-join-request\" && session.phase === \"lobby\"",
				"if (payload.type === \"room-participant-intent\" && session.phase === \"lobby\"");
		String epochSync = "session.roomEpoch = Number(session.snapshot && session.snapshot.roomEpoch) || session.roomEpoch;";

		int joinSuccess = handler.indexOf("if (!addParticipantToRoomSnapshot(session.snapshot, participant))");
		int sync = handler.indexOf(epochSync, joinSuccess);
		int save = handler.indexOf("saveMultiplayerRoomSession(session);", joinSuccess);

		assertTrue(joinSuccess >= 0);
		assertTrue(sync > joinSuccess);
		assertTrue(save > sync);
	}

	@Test
	public void participantIntentIdempotencyIsScopedPerParticipant() throws IOException {
		String html = readIndexTemplate();
		String intent = sectionBetween(html,
				"function applyParticipantIntent(runtime, intent)",
				"function clearRoomLaunchTimer(runtime)");
		String handler = sectionBetween(html,
				"if (payload.type === \"room-participant-intent\" && session.phase === \"lobby\"",
				"if (payload.type === \"room-intent-result\")");
		String participantId = "const participantId = String(intent.participantId || \"\");";
		String handledKey = "const handledIntentKey = participantId + \":\" + String(intent.intentId || \"\");";
		String duplicateCheck = "if (runtime.handledIntentIds[handledIntentKey])";
		String epochCheck = "if (!participantIntentMatchesRoomEpoch(session, intent))";

		assertTrue(intent.contains(participantId));
		assertTrue(intent.contains(handledKey));
		assertTrue(intent.contains(duplicateCheck));
		assertTrue(intent.contains("return runtime.handledIntentIds[handledIntentKey];"));
		assertTrue(intent.contains("runtime.handledIntentIds[handledIntentKey] = result;"));
		assertFalse(intent.contains("runtime.handledIntentIds[intent.intentId]"));
		assertTrue(intent.indexOf(participantId) < intent.indexOf(handledKey));
		assertTrue(intent.indexOf(handledKey) < intent.indexOf(duplicateCheck));
		assertTrue(intent.indexOf(duplicateCheck) < intent.indexOf(epochCheck));
		assertTrue(handler.contains("const result = applyParticipantIntent(runtime, intent);"));
		assertTrue(handler.contains("if (intent.action === \"leave\")"));
		assertTrue(handler.indexOf("if (intent.action === \"leave\")")
				< handler.indexOf("if (!participantIntentMatchesRoomEpoch(session, intent))"));
		assertTrue(handler.indexOf("const result = applyParticipantIntent(runtime, intent);")
				> handler.indexOf("if (intent.action === \"leave\")"));
	}

	@Test
	public void duplicateParticipantIntentResultsAreIdempotent() throws IOException {
		String html = readIndexTemplate();
		String resultHandler = sectionBetween(html,
				"if (payload.type === \"room-intent-result\")",
				"if (payload.type === \"room-reconnect-rejected\"");
		String transportRuntime = sectionBetween(html,
				"const runtime = {",
				"multiplayerRoomRuntime = runtime;");

		assertTrue(resultHandler.contains("const intentResultKey = JSON.stringify(["));
		assertTrue(resultHandler.contains("runtime.handledIntentResultIds = runtime.handledIntentResultIds || {};"));
		assertTrue(resultHandler.contains("if (runtime.handledIntentResultIds[intentResultKey])"));
		assertTrue(resultHandler.contains("runtime.handledIntentResultIds[intentResultKey] = true;"));
		assertTrue(resultHandler.indexOf("if (payload.targetParticipantId !== session.participantId)")
				< resultHandler.indexOf("const intentResultKey = JSON.stringify(["));
		assertTrue(resultHandler.indexOf("runtime.handledIntentResultIds[intentResultKey] = true;")
				< resultHandler.indexOf("pushMultiplayerRoomEvent(\"room-error\""));
		assertTrue(transportRuntime.contains("handledIntentResultIds: {},"));
	}

	@Test
	public void roomCapacityUsesPlayableSeatLimit() throws IOException {
		String html = readIndexTemplate();
		String join = sectionBetween(html,
				"function addParticipantToRoomSnapshot(snapshot, participant)",
				"function validHeroChoice(heroChoice)");
		String intent = sectionBetween(html,
				"if (intent.action === \"ready-toggle\")",
				"\n                if (result.ok)");
		String lobby = sectionBetween(html,
				"function pushRoomLobbyEvent(session)",
				"function cleanupMultiplayerRoomEntry(reason)");
		String joinRejected = sectionBetween(html,
				"if (payload.type === \"room-join-rejected\"",
				"function startMultiplayerRoomTransport(session)");

		assertTrue(html.contains("const MULTIPLAYER_ROOM_MAX_CONNECTIONS = 4;"));
		assertTrue(join.contains("snapshot.participants.length >= MULTIPLAYER_ROOM_MAX_CONNECTIONS"));
		assertTrue(join.contains("const seat = firstOpenSeat(snapshot);"));
		assertTrue(join.contains("seat < 0 || snapshot.participants.length >= MULTIPLAYER_ROOM_MAX_CONNECTIONS"));
		assertTrue(join.contains("participant.role = \"player\";"));
		assertFalse(join.contains("participant.role = \"watcher\";"));
		assertFalse(join.contains("snapshot.watcherIds.push(participant.participantId);"));
		assertTrue(join.contains("snapshot.capacity.activeConnections = snapshot.participants.length;"));
		assertFalse(intent.contains("role-toggle"));
		assertTrue(lobby.contains("participants.length || 1"));
		assertTrue(lobby.contains("MULTIPLAYER_ROOM_MAX_PLAYER_SEATS"));
		assertTrue(lobby.contains("values.push(watchers.length);"));
		assertTrue(joinRejected.contains("removeMultiplayerRoomSession(session);"));
		assertTrue(joinRejected.contains("window.__shpdMultiplayerRooms.currentSession = null;"));
		assertTrue(joinRejected.indexOf("removeMultiplayerRoomSession(session);")
				< joinRejected.indexOf("pushMultiplayerRoomEvent(\"room-error\""));
	}

	@Test
	public void activeRoomBoundaryRejectsLateLobbyMutations() throws IOException {
		String html = readIndexTemplate();
			String handler = sectionBetween(html,
					"async function handleEncryptedRoomMessage(runtime, envelope, peerId)",
					"function startMultiplayerRoomTransport(session)");
			String createConflict = sectionBetween(html,
					"function rejectRoomCreateConflict(runtime)",
					"function sendParticipantIntentResult(runtime, intent, result, peerId)");
			String localActions = sectionBetween(html,
					"function requestMultiplayerRoomAction(action, value)",
					"window.__shpdMultiplayerRooms = {");

		assertTrue(localActions.contains("session.phase !== \"lobby\""));
		assertTrue(handler.contains("payload.type === \"room-launch\" && payload.snapshot"));
		assertTrue(handler.contains("session.phase === \"lobby\""));
			assertTrue(handler.contains("session.phase = \"active\""));
			assertTrue(handler.contains("payload.type === \"room-create-probe\" && session.phase === \"active\""));
			assertTrue(handler.contains("sendCurrentRoomSnapshot(runtime, peerId);"));
			assertTrue(createConflict.contains("cleanupMultiplayerRoomEntry(\"room-name-conflict\");"));
			assertTrue(createConflict.contains("removeMultiplayerRoomSession(session);"));
			assertTrue(createConflict.contains("window.__shpdMultiplayerRooms.currentSession = null;"));
			assertTrue(handler.contains("rejectRoomCreateConflict(runtime);"));
			assertTrue(handler.contains("payload.type === \"room-join-request\" && session.phase === \"active\""));
			assertTrue(handler.contains("rejectRoomJoin(runtime, \"game already started\""));
		assertTrue(handler.contains("payload.type === \"room-participant-intent\" && session.phase === \"lobby\""));
		assertTrue(handler.contains("} else if (session.phase === \"lobby\"\n"
				+ "                            && snapshotCanUpdateLobbyFromSender"));
		assertFalse(handler.contains("payload.type === \"room-participant-intent\" && session.phase === \"active\""));
		assertFalse(handler.contains("session.phase === \"active\"\n"
				+ "                            && snapshotCanUpdateLobbyFromSender"));
	}

	@Test
	public void roomBridgeDoesNotRenderDomLobbyUi() throws IOException {
		String html = readIndexTemplate();
		String roomBridge = sectionBetween(html,
				"const MULTIPLAYER_ROOM_PROTOCOL_VERSION = 1;",
				"function installWebMultiplayer(launchConfig)");

		assertTrue(html.contains("<canvas id=\"canvas\""));
		assertTrue(html.contains("window.__shpdMultiplayerRooms = {"));
		assertTrue(roomBridge.contains("requestRoom: requestMultiplayerRoomSession"));
		assertTrue(roomBridge.contains("requestAction: requestMultiplayerRoomAction"));
		assertTrue(roomBridge.contains("pollEvent: function()"));
		assertFalse(roomBridge.contains("document.createElement"));
		assertFalse(roomBridge.contains("document.body.appendChild"));
		assertFalse(roomBridge.contains("innerHTML"));
		assertFalse(roomBridge.contains("querySelector"));
		assertFalse(html.contains("id=\"shpd-room"));
		assertFalse(html.contains("class=\"shpd-room"));
		assertFalse(html.contains("multiplayer-room-overlay"));
	}

	@Test
	public void devUrlRoomActionsUseHiddenRoomBridge() throws IOException {
		String html = readIndexTemplate();
		String roomBridge = sectionBetween(html,
				"const MULTIPLAYER_ROOM_PROTOCOL_VERSION = 1;",
				"function installWebMultiplayer(launchConfig)");
		String devUrlRunner = sectionBetween(html,
				"const multiplayerDevUrlRoomActionRunner = {",
				"window.__shpdMultiplayerRooms = {");
		String activeRuntime = sectionBetween(html,
				"function installWebMultiplayer(launchConfig)",
				"import(MULTIPLAYER_TRYSTERO_MODULE)");

		assertTrue(roomBridge.contains("MULTIPLAYER_ROOM_DEV_URL_ACTIONS"));
		assertTrue(roomBridge.contains("\"ready-toggle\", \"hero-choice\", \"floor-turns\""));
		assertFalse(roomBridge.contains("\"role-toggle\""));
		assertTrue(roomBridge.contains("\"floor-infinite\", \"wait-connections\", \"start-toggle\", \"leave\""));
		assertTrue(devUrlRunner.contains("roomMode"));
		assertTrue(devUrlRunner.contains("roomActions=action[:value],..."));
		assertTrue(devUrlRunner.contains("roomAction+roomActionValue"));
		assertTrue(devUrlRunner.contains("params.getAll(\"roomActions\")"));
		assertTrue(devUrlRunner.contains("params.getAll(\"roomAction\")"));
		assertTrue(devUrlRunner.contains("roomName: devUrlRoomParam(params, \"roomName\") || devUrlRoomParam(params, \"room\")"));
		assertTrue(devUrlRunner.contains("playerName: devUrlRoomParam(params, \"playerName\") || devUrlRoomParam(params, \"id\")"));
		assertTrue(devUrlRunner.contains("mode !== \"create\" && mode !== \"join\""));
		assertTrue(devUrlRunner.contains("Unknown room action: "));
		assertTrue(devUrlRunner.contains("MULTIPLAYER_ROOM_HERO_CHOICES.indexOf(value)"));
		assertTrue(devUrlRunner.contains("MULTIPLAYER_ROOM_FINITE_TURNS.indexOf(turns)"));
		assertTrue(devUrlRunner.contains("normalized === \"true\" || normalized === \"1\" || normalized === \"yes\""));
		assertTrue(devUrlRunner.contains("Invalid connection count for room action."));
		assertTrue(devUrlRunner.contains("action === \"wait-connections\""));
		assertTrue(devUrlRunner.contains("(snapshot.participants || []).length >= Number(value)"));
		assertTrue(devUrlRunner.contains("requestMultiplayerRoomSession(spec.mode, spec.roomName"));
		assertTrue(devUrlRunner.contains("MULTIPLAYER_ROOM_SOURCE_DEV_URL"));
		assertTrue(devUrlRunner.contains("requestMultiplayerRoomAction(action, value)"));
		assertTrue(devUrlRunner.contains("if (action === \"leave\")"));
		assertTrue(devUrlRunner.contains("return before.countdown ? !hasCountdown : false;"));
		assertTrue(devUrlRunner.contains("action === \"start-toggle\" ? 3 : 1"));
		assertTrue(devUrlRunner.contains("recordDevUrlRoomActionStep(action, \"retry\", browserDataBackupErrorMessage(error));"));
		assertTrue(html.contains("devUrlRoomActionRunner: multiplayerDevUrlRoomActionRunner"));
		assertTrue(html.contains("installWebMultiplayer();\n"
				+ "            startDevUrlRoomActionRunner();"));
		assertTrue(activeRuntime.contains("if (!launchConfig && isDevUrlRoomActionRequest(params))"));
		assertFalse(roomBridge.contains("document.createElement"));
		assertFalse(roomBridge.contains("document.body.appendChild"));
	}

	@Test
	public void watcherReplayStillFeedsPeerRowsBeforeTargetFilter() throws IOException {
		String html = readIndexTemplate();
		String replayHandler = sectionBetween(html,
				"getReplayEvent(async (data, peerId) => {",
				"function requestSnapshotIfNeeded()");

		int nonWatcherMirror = replayHandler.indexOf("if (!state.isWatcher) {");
		int watcherMirror = replayHandler.indexOf("handlePeerMirrorReplay(data, kind);\n"
				+ "                        if (disconnected) {", nonWatcherMirror);
		int targetFilter = replayHandler.indexOf("if (remotePlayerId !== state.watchTargetId)", watcherMirror);
		assertTrue(nonWatcherMirror >= 0);
		assertTrue(watcherMirror > nonWatcherMirror);
		assertTrue(targetFilter > watcherMirror);
	}

	@Test
	public void watcherWatchingStatusIsEdgeTriggeredPerWatcherPeer() throws IOException {
		String html = readIndexTemplate();
		String activeRuntime = sectionBetween(html,
				"function installWebMultiplayer(launchConfig)",
				"getHello(async (data, peerId) => {");
		String helloHandler = sectionBetween(activeRuntime,
				"function handleHello(data, peerId) {",
				"state.debugInjectPeerHello = function");

		assertTrue(activeRuntime.contains("activeWatcherStatusMessages: new Set()"));
		assertTrue(activeRuntime.contains("activeWatcherStatusPeers: new Map()"));
		assertTrue(helloHandler.contains("const watcherStatusKey = remotePlayerId + \":\" + String(data.watchTargetId || \"\");"));
		assertTrue(helloHandler.contains("!state.activeWatcherStatusMessages.has(watcherStatusKey)"));
		assertTrue(helloHandler.contains("state.activeWatcherStatusMessages.add(watcherStatusKey);"));
		assertTrue(helloHandler.contains("state.activeWatcherStatusPeers.set(peerId, watcherStatusKey);"));
		assertTrue(activeRuntime.contains("state.activeWatcherStatusMessages.delete(watcherStatusKey);"));
	}

	@Test
	public void watcherKeyframesCanUseTransientRoomSnapshotPayloads() throws IOException {
		String html = readIndexTemplate();

		assertTrue(html.contains("function createBrowserDataSnapshotPayloadFromFiles(filesJson, gameVersion)"));
		assertTrue(html.contains("sendWatcherKeyframe: function(watcherId, requestId, snapshotFilesJson)"));
		assertTrue(html.contains("sendPendingWatcherKeyframe(watcherId, requestId, snapshotFilesJson);"));
		assertTrue(html.contains("Number.isFinite(progressSlot) ? progressSlot : 1"));
		assertTrue(html.contains("Number.isFinite(parsedSourceSlot) ? parsedSourceSlot : 1"));
	}

	@Test
	public void roomLaunchCarriesPlayerSeatOrderIntoActiveRuntime() throws IOException {
		String html = readIndexTemplate();
		String launchHelpers = sectionBetween(html,
				"function playerSeatOrderForSnapshot(snapshot)",
				"function sendRoomLaunch(runtime, peerId)");
		String activeRuntime = sectionBetween(html,
				"function installWebMultiplayer(launchConfig)",
				"import(MULTIPLAYER_TRYSTERO_MODULE)");

		assertTrue(launchHelpers.contains("const firstPlayer = occupiedPlayerSeats(snapshot)[0];"));
		assertTrue(launchHelpers.contains("watchTargetId: firstPlayer && firstPlayer.participantId || \"\""));
		assertTrue(launchHelpers.contains("join(\",\")"));
		assertTrue(launchHelpers.contains("playerSeatOrderForSnapshot(snapshot)"));
		assertTrue(activeRuntime.contains("playerSeatOrder: launchConfig ? String(launchConfig.playerSeatOrder || \"\") : \"\""));
		assertTrue(activeRuntime.contains("roomEpoch: launchConfig ? Number(launchConfig.roomEpoch) || 0 : 0"));
		assertTrue(activeRuntime.contains("get playerSeatOrder()"));
		assertTrue(activeRuntime.contains("get roomEpoch()"));
		assertTrue(activeRuntime.contains("playerSeatOrder: state.playerSeatOrder"));
		assertTrue(activeRuntime.contains("roomEpoch: state.roomEpoch"));
	}

	@Test
	public void webTextInputActiveStateOwnsCanvasFocusAndKeyDefaults() throws IOException {
		String html = readIndexTemplate();
		String textInputBridge = sectionBetween(html,
				"window.__shpdGameTextInputActive = false;",
				"function applyDirectStartHowlerPolicy(howler)");
		String keyHandler = sectionBetween(html,
				"function preventPageKeyHandling(event)",
				"function pointerClientPosition(event)");
		String pointerFocus = sectionBetween(html,
				"document.addEventListener(\"pointerdown\", () => {",
				"}, { capture: true });");

		assertTrue(textInputBridge.contains("window.__shpdSetGameTextInputActive = function(active, multiline)"));
		assertTrue(textInputBridge.contains("function isGameTextInputActive()"));
		assertTrue(textInputBridge.contains("function releaseGameTextInputModifiers(reason)"));
		assertTrue(textInputBridge.contains("function trackGameTextInputModifierState(event)"));
		assertTrue(textInputBridge.contains("releaseGameTextInputModifiers(\"activate\")"));
		assertTrue(textInputBridge.contains("releaseGameTextInputModifiers(\"non-meta-key\")"));
		assertTrue(textInputBridge.contains("function focusGameCanvas()"));
		assertTrue(textInputBridge.contains("if (isGameTextInputActive())"));
		assertTrue(textInputBridge.contains("canvas focus skipped text input active"));
		assertTrue(keyHandler.contains("trackGameTextInputModifierState(event);"));
		assertTrue(keyHandler.contains("syntheticModifierRelease"));
		assertTrue(keyHandler.contains("event.key === \"Tab\""));
		assertTrue(keyHandler.contains("event.code === \"Tab\""));
		assertTrue(keyHandler.contains("event.keyCode === 9"));
		assertTrue(keyHandler.indexOf("if (isGameTextInputActive())")
				< keyHandler.indexOf("event.preventDefault();"));
		assertTrue(pointerFocus.contains("if (gameStarted && !isGameTextInputActive())"));
	}

	@Test
	public void webParityConsoleSuppressesBenignRtcCloseErrors() throws IOException {
		String html = readIndexTemplate();
		String rtcCloseFilter = sectionBetween(html,
				"function benignRtcCloseConsoleError(args)",
				"function replayPreviousWebParityLogs()");
		String consoleBuffer = sectionBetween(html,
				"function installWebParityConsoleBuffer()",
				"replayPreviousWebParityLogs();");

		assertTrue(rtcCloseFilter.contains("RTCErrorEvent"));
		assertTrue(rtcCloseFilter.contains("Close called"));
		assertTrue(rtcCloseFilter.contains("User-Initiated Abort"));
		assertTrue(rtcCloseFilter.contains("OperationError"));
		assertTrue(consoleBuffer.contains("level === \"error\" && benignRtcCloseConsoleError(arguments)"));
		assertTrue(consoleBuffer.contains("suppressed benign rtc close error"));
		assertTrue(consoleBuffer.indexOf("benignRtcCloseConsoleError(arguments)")
				< consoleBuffer.indexOf("original.apply(console, arguments)"));
	}

	@Test
	public void roomEntryPeerJoinCarriesPeerIdIntoReconnectHandshake() throws IOException {
		String html = readIndexTemplate();
		String roomEntryTransport = sectionBetween(html,
				"function startMultiplayerRoomTransport(session)",
				"room.onPeerLeave((peerId) =>");

		assertTrue(roomEntryTransport.contains("room.onPeerJoin((peerId) =>"));
		assertFalse(roomEntryTransport.contains("room.onPeerJoin(() =>"));
		assertTrue(roomEntryTransport.contains("const activeSession = runtime.session;"));
		assertTrue(roomEntryTransport.contains("activeSession.phase === \"join-handshake\""));
		assertTrue(roomEntryTransport.contains("activeSession.phase === \"reconnect-handshake\""));
		assertTrue(roomEntryTransport.contains("sendRoomHandshakeRequest(runtime, peerId);"));
	}

	@Test
	public void roomEntryRetriesJoinAndReconnectHandshakeWithinTimeout() throws IOException {
		String html = readIndexTemplate();
				String cleanup = sectionBetween(html,
						"function cleanupMultiplayerRoomRuntime(runtime, reason)",
					"async function sendEncryptedRoomPayload(runtime, payload, peerId)");
		String retryHelpers = sectionBetween(html,
				"function clearRoomHandshakeRetry(runtime)",
				"function rejectRoomJoin(runtime, reason, peerId, requestId, targetParticipantId)");
		String acceptedSnapshot = sectionBetween(html,
				"function handleAcceptedRoomSnapshot(runtime, snapshot)",
				"function handleUpdatedRoomSnapshot(runtime, snapshot)");
		String reconnectRejected = sectionBetween(html,
				"if (payload.type === \"room-reconnect-rejected\"",
				"if (payload.type === \"room-join-rejected\"");
		String roomEntryTransport = sectionBetween(html,
				"function startMultiplayerRoomTransport(session)",
				"}).catch((error) => {");

		assertTrue(html.contains("const MULTIPLAYER_ROOM_HANDSHAKE_RETRY_MS = 1000;"));
		assertTrue(cleanup.contains("clearRoomHandshakeRetry(runtime);"));
		assertTrue(retryHelpers.contains("window.clearInterval(runtime.handshakeRetryId);"));
		assertTrue(retryHelpers.contains("session.phase === \"join-handshake\""));
		assertTrue(retryHelpers.contains("sendJoinRequest(runtime, peerId);"));
		assertTrue(retryHelpers.contains("session.phase === \"reconnect-handshake\""));
		assertTrue(retryHelpers.contains("sendReconnectRequest(runtime, peerId);"));
		assertTrue(retryHelpers.contains("window.setInterval(() => {"));
		assertTrue(retryHelpers.contains("multiplayerRoomRuntime !== runtime"));
		assertTrue(retryHelpers.contains("MULTIPLAYER_ROOM_HANDSHAKE_RETRY_MS"));
		assertTrue(retryHelpers.contains("multiplayer room handshake retry"));
		assertTrue(acceptedSnapshot.contains("clearRoomHandshakeRetry(runtime);"));
		assertTrue(reconnectRejected.contains("startRoomHandshakeRetry(runtime);"));
		assertTrue(roomEntryTransport.contains("handshakeRetryId: 0"));
		assertEquals(2, countOccurrences(roomEntryTransport, "sendRoomHandshakeRequests(runtime);\n"
				+ "                        startRoomHandshakeRetry(runtime);"));
	}

	@Test
	public void roomJoinHandshakeRetriesTargetKnownPeersAndOwnerRepliesDirectly() throws IOException {
		String html = readIndexTemplate();
		String retryHelpers = sectionBetween(html,
				"function clearRoomHandshakeRetry(runtime)",
				"function rejectRoomJoin(runtime, reason, peerId, requestId, targetParticipantId)");
		String roomEntryTransport = sectionBetween(html,
				"function startMultiplayerRoomTransport(session)",
				"}).catch((error) => {");
		String joinRequestHandler = sectionBetween(html,
				"if (payload.type === \"room-join-request\" && session.phase === \"lobby\"",
				"if (payload.type === \"room-participant-intent\"");

		assertTrue(retryHelpers.contains("function sendRoomHandshakeRequests(runtime)"));
		assertTrue(retryHelpers.contains("for (const peerId of runtime.roomPeers)"));
		assertTrue(retryHelpers.contains("sendRoomHandshakeRequest(runtime, peerId);"));
		assertTrue(roomEntryTransport.contains("roomPeers: new Set()"));
		assertTrue(roomEntryTransport.contains("runtime.roomPeers.add(peerId);"));
		assertTrue(roomEntryTransport.contains("runtime.roomPeers.delete(peerId);"));
		assertTrue(roomEntryTransport.contains("sendRoomHandshakeRequests(runtime);"));
		assertTrue(joinRequestHandler.contains("sendCurrentRoomSnapshot(runtime, peerId);"));
		assertTrue(joinRequestHandler.contains("sendCurrentRoomSnapshot(runtime);"));
	}

	@Test
	public void roomEntryValidationRequiresMinimumRoomNameAndPasswordLengths() throws IOException {
		String html = readIndexTemplate();

		assertTrue(html.contains("const MULTIPLAYER_ROOM_NAME_MIN_LENGTH = 6;"));
		assertTrue(html.contains("const MULTIPLAYER_ROOM_PASSWORD_MIN_LENGTH = 6;"));
		assertTrue(html.contains("roomName.length < MULTIPLAYER_ROOM_NAME_MIN_LENGTH"));
		assertTrue(html.contains("password.length < MULTIPLAYER_ROOM_PASSWORD_MIN_LENGTH"));
		assertTrue(html.contains("Room name must be at least 6 characters."));
		assertTrue(html.contains("Room password must be at least 6 characters."));
	}

	@Test
	public void benignRtcCloseConsoleFilterSurvivesReleaseParityStrip() throws IOException {
		String html = BuildWeb.configureWebParityLogging(readIndexTemplate(), false);

		assertTrue(html.contains("function benignRtcCloseConsoleError(args)"));
		assertTrue(html.contains("function installBenignRtcCloseConsoleFilter()"));
		assertTrue(html.contains("installBenignRtcCloseConsoleFilter();"));
		assertTrue(html.contains("console.error = function()"));
		assertTrue(html.contains("Close called"));
		assertTrue(html.contains("User-Initiated Abort"));
		assertTrue(html.contains("OperationError"));
	}

	@Test
	public void releaseParityStripKeepsBootstrapHelpers() throws IOException {
		String html = BuildWeb.configureWebParityLogging(readIndexTemplate(), false);
		assertFalse(html.contains("%WEB_PARITY_LOGGING%"));
		assertFalse(html.contains("WEB_PARITY_LOGGING_ENABLED"));
		assertFalse(html.contains("WEB_PARITY_LOGGING_BEGIN"));
		assertFalse(html.contains("WEB_PARITY_LOGGING_END"));
		assertTrue(html.contains("window.__shpdWebParityLogging = false"));
		assertTrue(html.contains("function logWebParity()"));
		assertTrue(html.contains("function focusGameCanvas()"));
		assertTrue(html.contains("function focusActiveGameSurface()"));
		assertTrue(html.contains("function isGameTextInputActive()"));
		assertTrue(html.contains("window.__shpdSetGameTextInputActive"));
		assertTrue(html.contains("window.addEventListener(\"focus\", focusActiveGameSurface);"));

		int loadStart = html.indexOf("function start()");
		int loadListener = html.indexOf("window.addEventListener(\"load\", start);");
		assertTrue(loadStart >= 0);
		assertTrue(loadListener > loadStart);
		assertTrue(html.substring(loadStart, loadListener).contains("focusGameCanvas()"));
	}

	@Test
	public void webParityLoggingConfigurationIsRepeatable() throws IOException {
		String debugHtml = BuildWeb.configureWebParityLogging(readIndexTemplate(), true);
		assertEquals(debugHtml, BuildWeb.configureWebParityLogging(debugHtml, true));

		String releaseHtml = BuildWeb.configureWebParityLogging(readIndexTemplate(), false);
		assertEquals(releaseHtml, BuildWeb.configureWebParityLogging(releaseHtml, false));
	}

	private static String sectionBetween(String text, String startMarker, String endMarker) {
		int start = text.indexOf(startMarker);
		assertTrue("Missing start marker: " + startMarker, start >= 0);
		int end = text.indexOf(endMarker, start);
		assertTrue("Missing end marker: " + endMarker, end > start);
		return text.substring(start, end);
	}

	private static int countOccurrences(String text, String needle) {
		int count = 0;
		int next = 0;
		while ((next = text.indexOf(needle, next)) >= 0) {
			count++;
			next += needle.length();
		}
		return count;
	}

	private static String readIndexTemplate() throws IOException {
		try (InputStream input = WebIndexTemplateTest.class.getResourceAsStream("/webapp/index.html")) {
			assertTrue(input != null);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int read;
			while ((read = input.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
			return new String(output.toByteArray(), StandardCharsets.UTF_8);
		}
	}
}
