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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.noosa.Game;
import com.watabou.utils.Point;
import com.watabou.utils.PlatformSupport;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class WebPlatformSupport extends PlatformSupport {

	private static final Logger LOG = Logger.getLogger(WebPlatformSupport.class.getName());

	private static FreeTypeFontGenerator basicFontGenerator;
	private static FreeTypeFontGenerator asianFontGenerator;

	private final Pattern regularSplitter = Pattern.compile(
			"(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)");

	private final Pattern regularSplitterMultiline = Pattern.compile(
			"(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)");

	private boolean defaultUiApplied;
	private final BrowserDataBackup browserDataBackup = new WebBrowserDataBackup();

	@Override
	public void updateDisplaySize() {
		Point observed = new Point(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Point stored = SPDSettings.windowResolution();
		if (observed.x > 0 && observed.y > 0 && (stored.x != observed.x || stored.y != observed.y)) {
			if (webParityLoggingEnabled()) {
				LOG.info("[WEB-PARITY] web display size observed width=" + observed.x
						+ " height=" + observed.y
						+ " storedWidth=" + stored.x
						+ " storedHeight=" + stored.y
						+ " action=storeObservedSize");
			}
			SPDSettings.windowResolution(observed);
		}
	}

	@Override
	public boolean webParityLoggingEnabled() {
		return webParityLoggingEnabledNative();
	}

	@Override
	protected void setTextInputActive(boolean value, boolean multiline) {
		setTextInputActiveNative(value, multiline);
	}

	@JSBody(script = "return typeof window !== 'undefined' && window.__shpdWebParityLogging === true;")
	private static native boolean webParityLoggingEnabledNative();

	@JSBody(params = { "value", "multiline" },
			script = "if (typeof window !== 'undefined') {"
					+ " if (typeof window.__shpdSetGameTextInputActive === 'function') {"
					+ "  window.__shpdSetGameTextInputActive(value === true, multiline === true);"
					+ " } else {"
					+ "  window.__shpdGameTextInputActive = value === true;"
					+ " }"
					+ "}")
	private static native void setTextInputActiveNative(boolean value, boolean multiline);

	@Override
	public BrowserDataBackup browserDataBackup() {
		return browserDataBackup;
	}

	@Override
	public boolean multiplayerEnabled() {
		return multiplayerEnabledNative();
	}

	@Override
	public boolean multiplayerRoomEntryAvailable() {
		return multiplayerRoomEntryAvailableNative();
	}

	@Override
	public boolean requestMultiplayerRoomEntry(String mode, String roomName, String password, String playerName) {
		return requestMultiplayerRoomEntryNative(mode, roomName, password, playerName);
	}

	@Override
	public String pollMultiplayerRoomEvent() {
		return pollMultiplayerRoomEventNative();
	}

	@Override
	public boolean requestMultiplayerRoomAction(String action, String value) {
		return requestMultiplayerRoomActionNative(action, value);
	}

	@Override
	public void leaveMultiplayerRoomEntry() {
		leaveMultiplayerRoomEntryNative();
	}

	@Override
	public String multiplayerPlayerId() {
		return multiplayerPlayerIdNative();
	}

	@Override
	public String multiplayerPlayerName() {
		return multiplayerPlayerNameNative();
	}

	@Override
	public String multiplayerPlayerColor() {
		return multiplayerPlayerColorNative();
	}

	@Override
	public String multiplayerPlayerSeatOrder() {
		return multiplayerPlayerSeatOrderNative();
	}

	@Override
	public int multiplayerRoomEpoch() {
		return multiplayerRoomEpochNative();
	}

	@Override
	public boolean multiplayerWatcher() {
		return multiplayerWatcherNative();
	}

	@Override
	public String multiplayerWatchTargetId() {
		return multiplayerWatchTargetIdNative();
	}

	@Override
	public void announceMultiplayerProgress(int slot, int depth, int branch, int turns) {
		announceMultiplayerProgressNative(slot, depth, branch, turns);
	}

	@Override
	public void announceMultiplayerReplayEvent(String kind, int depth, int branch, int cell, String message) {
		announceMultiplayerReplayEventNative(kind, depth, branch, cell, message);
	}

	@Override
	public String pollMultiplayerEvent() {
		return pollMultiplayerEventNative();
	}

	@Override
	public int consumeMultiplayerResumeSlot() {
		return consumeMultiplayerResumeSlotNative();
	}

	@Override
	public boolean consumeMultiplayerWatcherResumeRequested() {
		return consumeMultiplayerWatcherResumeRequestedNative();
	}

	@Override
	public void requestMultiplayerWatcherKeyframe() {
		requestMultiplayerWatcherKeyframeNative();
	}

	@Override
	public void sendMultiplayerWatcherKeyframe(String watcherId, String requestId) {
		sendMultiplayerWatcherKeyframe(watcherId, requestId, "");
	}

	@Override
	public void sendMultiplayerWatcherKeyframe(String watcherId, String requestId, String snapshotFilesJson) {
		sendMultiplayerWatcherKeyframeNative(watcherId, requestId, snapshotFilesJson);
	}

	@Override
	public void saveActiveMultiplayerRunSnapshot(int sourceSlot, String snapshotFilesJson) {
		saveActiveMultiplayerRunSnapshotNative(sourceSlot, snapshotFilesJson);
	}

	@Override
	public void switchMultiplayerWatchTarget(String targetId) {
		switchMultiplayerWatchTargetNative(targetId);
	}

	@Override
	public boolean continueMultiplayerAsWatcher(String targetId) {
		return continueMultiplayerAsWatcherNative(targetId);
	}

	@Override
	public void markMultiplayerWatchReturnEligible(String reason) {
		markMultiplayerWatchReturnEligibleNative(reason);
	}

	@Override
	public void disconnectActiveMultiplayerGame() {
		disconnectActiveMultiplayerGameNative();
	}

	@Override
	public void finishActiveMultiplayerRoom() {
		finishActiveMultiplayerRoomNative();
	}

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && window.__shpdMultiplayer.enabled === true;")
	private static native boolean multiplayerEnabledNative();

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayerRooms"
			+ " && typeof window.__shpdMultiplayerRooms.requestRoom === 'function';")
	private static native boolean multiplayerRoomEntryAvailableNative();

	@JSBody(params = { "mode", "roomName", "password", "playerName" },
			script = "if (typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayerRooms"
			+ " && typeof window.__shpdMultiplayerRooms.requestRoom === 'function') {"
			+ " window.__shpdMultiplayerRooms.requestRoom(mode, roomName, password, playerName);"
			+ " return true;"
			+ "}"
			+ "return false;")
	private static native boolean requestMultiplayerRoomEntryNative(String mode, String roomName, String password,
			String playerName);

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayerRooms"
			+ " && typeof window.__shpdMultiplayerRooms.pollEvent === 'function'"
			+ " ? window.__shpdMultiplayerRooms.pollEvent() : null;")
	private static native String pollMultiplayerRoomEventNative();

	@JSBody(params = { "action", "value" },
			script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayerRooms"
			+ " && typeof window.__shpdMultiplayerRooms.requestAction === 'function'"
			+ " ? window.__shpdMultiplayerRooms.requestAction(action, value) === true : false;")
	private static native boolean requestMultiplayerRoomActionNative(String action, String value);

	@JSBody(script = "if (typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayerRooms"
			+ " && typeof window.__shpdMultiplayerRooms.leaveRoom === 'function') {"
			+ " window.__shpdMultiplayerRooms.leaveRoom();"
			+ "}")
	private static native void leaveMultiplayerRoomEntryNative();

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.playerId === 'string'"
			+ " ? window.__shpdMultiplayer.playerId : '';")
	private static native String multiplayerPlayerIdNative();

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.playerName === 'string'"
			+ " ? window.__shpdMultiplayer.playerName : '';")
	private static native String multiplayerPlayerNameNative();

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.playerColor === 'string'"
			+ " ? window.__shpdMultiplayer.playerColor : '';")
	private static native String multiplayerPlayerColorNative();

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.playerSeatOrder === 'string'"
			+ " ? window.__shpdMultiplayer.playerSeatOrder : '';")
	private static native String multiplayerPlayerSeatOrderNative();

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.roomEpoch === 'number'"
			+ " ? window.__shpdMultiplayer.roomEpoch : 0;")
	private static native int multiplayerRoomEpochNative();

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && window.__shpdMultiplayer.isWatcher === true;")
	private static native boolean multiplayerWatcherNative();

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.watchTargetId === 'string'"
			+ " ? window.__shpdMultiplayer.watchTargetId : '';")
	private static native String multiplayerWatchTargetIdNative();

	@JSBody(params = { "slot", "depth", "branch", "turns" },
			script = "if (typeof window !== 'undefined'"
					+ " && !!window.__shpdMultiplayer"
					+ " && typeof window.__shpdMultiplayer.announceProgress === 'function') {"
					+ " window.__shpdMultiplayer.announceProgress(slot, depth, branch, turns);"
					+ "}")
	private static native void announceMultiplayerProgressNative(int slot, int depth, int branch, int turns);

	@JSBody(params = { "kind", "depth", "branch", "cell", "message" },
			script = "if (typeof window !== 'undefined'"
					+ " && !!window.__shpdMultiplayer"
					+ " && typeof window.__shpdMultiplayer.announceReplayEvent === 'function') {"
					+ " window.__shpdMultiplayer.announceReplayEvent(kind, depth, branch, cell, message);"
					+ "}")
	private static native void announceMultiplayerReplayEventNative(String kind, int depth, int branch,
			int cell, String message);

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.pollJavaEvent === 'function'"
			+ " ? window.__shpdMultiplayer.pollJavaEvent() : null;")
	private static native String pollMultiplayerEventNative();

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.consumeResumeSlot === 'function'"
			+ " ? window.__shpdMultiplayer.consumeResumeSlot() : 0;")
	private static native int consumeMultiplayerResumeSlotNative();

	@JSBody(script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.consumeWatcherResumeRequested === 'function'"
			+ " ? window.__shpdMultiplayer.consumeWatcherResumeRequested() === true : false;")
	private static native boolean consumeMultiplayerWatcherResumeRequestedNative();

	@JSBody(script = "if (typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.requestWatcherKeyframe === 'function') {"
			+ " window.__shpdMultiplayer.requestWatcherKeyframe();"
			+ "}")
	private static native void requestMultiplayerWatcherKeyframeNative();

	@JSBody(params = { "watcherId", "requestId", "snapshotFilesJson" }, script = "if (typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.sendWatcherKeyframe === 'function') {"
			+ " window.__shpdMultiplayer.sendWatcherKeyframe(watcherId, requestId, snapshotFilesJson);"
			+ "}")
	private static native void sendMultiplayerWatcherKeyframeNative(String watcherId, String requestId,
			String snapshotFilesJson);

	@JSBody(params = { "sourceSlot", "snapshotFilesJson" }, script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.saveActiveRunSnapshot === 'function'"
			+ " ? window.__shpdMultiplayer.saveActiveRunSnapshot(sourceSlot, snapshotFilesJson) === true : false;")
	private static native boolean saveActiveMultiplayerRunSnapshotNative(int sourceSlot, String snapshotFilesJson);

	@JSBody(params = { "targetId" }, script = "if (typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.switchWatchTarget === 'function') {"
			+ " window.__shpdMultiplayer.switchWatchTarget(targetId);"
			+ "}")
	private static native void switchMultiplayerWatchTargetNative(String targetId);

	@JSBody(params = { "targetId" }, script = "return typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.continueAsWatcher === 'function'"
			+ " ? window.__shpdMultiplayer.continueAsWatcher(targetId) === true : false;")
	private static native boolean continueMultiplayerAsWatcherNative(String targetId);

	@JSBody(params = { "reason" }, script = "if (typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.markWatchReturnEligible === 'function') {"
			+ " window.__shpdMultiplayer.markWatchReturnEligible(reason);"
			+ "}")
	private static native void markMultiplayerWatchReturnEligibleNative(String reason);

	@JSBody(script = "if (typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.disconnect === 'function') {"
			+ " window.__shpdMultiplayer.disconnect();"
			+ "}")
	private static native void disconnectActiveMultiplayerGameNative();

	@JSBody(script = "if (typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.finishRoom === 'function') {"
			+ " window.__shpdMultiplayer.finishRoom();"
			+ "} else if (typeof window !== 'undefined'"
			+ " && !!window.__shpdMultiplayer"
			+ " && typeof window.__shpdMultiplayer.disconnect === 'function') {"
			+ " window.__shpdMultiplayer.disconnect();"
			+ "}")
	private static native void finishActiveMultiplayerRoomNative();

	private static class WebBrowserDataBackup implements BrowserDataBackup {

		@Override
		public boolean isAvailable() {
			return browserDataBackupAvailableNative();
		}

		@Override
		public void exportData(BrowserDataBackupCallback callback) {
			if (!isAvailable()) {
				complete(callback, BrowserDataBackupResult.unavailable());
				return;
			}
			exportDataNative(Game.version, (success, message, localStorageKeys, indexedDbRecords, bytes) ->
					complete(callback, result(success, message, localStorageKeys, indexedDbRecords, bytes)));
		}

		@Override
		public void importData(BrowserDataBackupCallback callback) {
			if (!isAvailable()) {
				complete(callback, BrowserDataBackupResult.unavailable());
				return;
			}
			importDataNative((success, message, localStorageKeys, indexedDbRecords, bytes) ->
					complete(callback, result(success, message, localStorageKeys, indexedDbRecords, bytes)));
		}

		private void complete(BrowserDataBackupCallback callback, BrowserDataBackupResult result) {
			if (callback != null) {
				callback.onComplete(result);
			}
		}

		private BrowserDataBackupResult result(boolean success, String message, int localStorageKeys,
				int indexedDbRecords, double bytes) {
			long byteCount = Double.isNaN(bytes) || bytes < 0 ? 0 : (long)bytes;
			if (success) {
				return BrowserDataBackupResult.success(message, localStorageKeys, indexedDbRecords, byteCount);
			} else {
				return BrowserDataBackupResult.failure(message);
			}
		}

		@JSFunctor
		private interface BrowserDataBackupCompletion extends JSObject {
			void complete(boolean success, String message, int localStorageKeys, int indexedDbRecords, double bytes);
		}

		@JSBody(script = "return typeof window !== 'undefined'"
				+ " && !!window.__shpdBrowserDataBackup"
				+ " && typeof window.__shpdBrowserDataBackup.available === 'function'"
				+ " && window.__shpdBrowserDataBackup.available();")
		private static native boolean browserDataBackupAvailableNative();

		@JSBody(params = { "gameVersion", "completion" },
				script = "window.__shpdBrowserDataBackup.exportData(gameVersion, completion);")
		private static native void exportDataNative(String gameVersion, BrowserDataBackupCompletion completion);

		@JSBody(params = { "completion" },
				script = "window.__shpdBrowserDataBackup.importData(completion);")
		private static native void importDataNative(BrowserDataBackupCompletion completion);
	}

	@Override
	public boolean supportsFullScreen() {
		return false;
	}

	@Override
	public void updateSystemUI() {
		if (!defaultUiApplied) {
			defaultUiApplied = true;
			if (!WebApplication.isMobileDevice() && !SPDSettings.contains(SPDSettings.KEY_UI_SIZE)) {
				SPDSettings.interfaceSize(2);
			}
		}
		// Browser chrome and safe-area behavior are owned by the page.
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
		if (fonts != null && this.pageSize == pageSize && this.systemfont == systemFont) {
			return;
		}
		this.pageSize = pageSize;
		this.systemfont = systemFont;

		resetGenerators(false);
		fonts = new HashMap<>();

		if (systemFont) {
			basicFontGenerator = asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		} else {
			basicFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel_font.ttf"));
			asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		}

		fonts.put(basicFontGenerator, new HashMap<Integer, BitmapFont>());
		fonts.put(asianFontGenerator, new HashMap<Integer, BitmapFont>());

		packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
	}

	@Override
	protected FreeTypeFontGenerator getGeneratorForString(String input) {
		if (containsAsianScript(input)) {
			return asianFontGenerator;
		} else {
			return basicFontGenerator;
		}
	}

	private static boolean containsAsianScript(String input) {
		if (input == null) {
			return false;
		}
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if ((c >= '\uAC00' && c <= '\uD7AF')
					|| (c >= '\u4E00' && c <= '\u9FFF')
					|| (c >= '\u3000' && c <= '\u303F')
					|| (c >= '\uFF00' && c <= '\uFFEF')
					|| (c >= '\u3040' && c <= '\u309F')
					|| (c >= '\u30A0' && c <= '\u30FF')) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] splitforTextBlock(String text, boolean multiline) {
		if (multiline) {
			return regularSplitterMultiline.split(text);
		} else {
			return regularSplitter.split(text);
		}
	}
}
