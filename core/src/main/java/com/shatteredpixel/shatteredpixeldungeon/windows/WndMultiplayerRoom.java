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

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.multiplayer.WebMultiplayer;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Game;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.TextInput;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

import java.util.regex.Pattern;
import java.util.logging.Logger;

public class WndMultiplayerRoom extends Window {

	private static final Logger LOG = Logger.getLogger(WndMultiplayerRoom.class.getName());

	private static final int WIDTH_P = 150;
	private static final int WIDTH_L = 190;
	private static final int MARGIN = 2;
	private static final int INPUT_HEIGHT = 16;
	private static final int BUTTON_HEIGHT = 18;
	private static final int ROOM_NAME_MIN = 6;
	private static final int ROOM_NAME_MAX = 32;
	private static final int PASSWORD_MIN = 6;
	private static final int PASSWORD_MAX = 96;
	private static final int PLAYER_NAME_MIN = 1;
	private static final int PLAYER_NAME_MAX = 24;
	private static final Pattern ROOM_NAME = Pattern.compile("[A-Za-z0-9_-]+");

	public enum Mode {
		CREATE("create", "create_title", "create", "creating"),
		JOIN("join", "join_title", "join", "joining");

		private final String id;
		private final String titleKey;
		private final String actionKey;
		private final String pendingKey;

		Mode(String id, String titleKey, String actionKey, String pendingKey) {
			this.id = id;
			this.titleKey = titleKey;
			this.actionKey = actionKey;
			this.pendingKey = pendingKey;
		}
	}

	private final Mode mode;
	private final int contentWidth;
	private final int inputWidth;

	private TextInput roomNameInput;
	private TextInput passwordInput;
	private TextInput playerNameInput;
	private PointerArea roomNameFocusArea;
	private PointerArea passwordFocusArea;
	private PointerArea playerNameFocusArea;
	private float roomNameTop;
	private float passwordTop;
	private float playerNameTop;
	private RenderedTextBlock errorText;
	private RedButton backButton;
	private RedButton submitButton;
	private RedButton rejoinButton;
	private boolean pending;
	private boolean pendingRejoin;

	public WndMultiplayerRoom(Mode mode) {
		super();
		this.mode = mode;
		contentWidth = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		inputWidth = contentWidth - MARGIN*2;

		float pos = MARGIN;

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, mode.titleKey), 9);
		title.hardlight(Window.TITLE_COLOR);
		title.maxWidth(inputWidth);
		title.setPos(MARGIN, pos);
		add(title);
		pos = title.bottom() + MARGIN*2;

		pos = addInput(Messages.get(this, "room_name"), ROOM_NAME_MAX, pos, InputTarget.ROOM_NAME);
		pos = addInput(Messages.get(this, "room_password"), PASSWORD_MAX, pos, InputTarget.PASSWORD);
		pos = addInput(Messages.get(this, "player_name"), PLAYER_NAME_MAX, pos, InputTarget.PLAYER_NAME);

		errorText = PixelScene.renderTextBlock(6);
		errorText.hardlight(0xFF8E75);
		errorText.text("", inputWidth);
		errorText.setPos(MARGIN, pos);
		add(errorText);
		pos += 20;

		backButton = new RedButton(Messages.get(this, "back")){
			@Override
			protected void onClick() {
				hide();
			}
		};
		submitButton = new RedButton(Messages.get(this, mode.actionKey)){
			@Override
			protected void onClick() {
				submit();
			}
		};
		if (mode == Mode.JOIN && Game.platform != null && Game.platform.multiplayerRoomRejoinAvailable()) {
			rejoinButton = new RedButton(Messages.get(this, "rejoin")){
				@Override
				protected void onClick() {
					rejoin();
				}
			};
			add(rejoinButton);
			rejoinButton.setRect(MARGIN, pos, inputWidth, BUTTON_HEIGHT);
			pos += BUTTON_HEIGHT + MARGIN;
		}
		add(backButton);
		add(submitButton);
		backButton.setRect(MARGIN, pos, (inputWidth - MARGIN) / 2f, BUTTON_HEIGHT);
		submitButton.setRect(backButton.right() + MARGIN, pos, (inputWidth - MARGIN) / 2f, BUTTON_HEIGHT);
		pos += BUTTON_HEIGHT;

		resize(contentWidth, (int)(pos + MARGIN));
		refreshInputBounds();

		if (!DeviceCompat.hasHardKeyboard()) {
			offset(0, -(int)(Game.height/(4*camera.zoom)));
			boundOffsetWithMargin(0);
		}
		roomNameInput.focus();
		PointerEvent.clearKeyboardThisPress = false;
		webParityLog("multiplayer room entry opened mode=" + mode.name()
				+ " x=" + (camera == null ? 0 : camera.x / camera.zoom)
				+ " y=" + (camera == null ? 0 : camera.y / camera.zoom)
				+ " width=" + contentWidth
				+ " height=" + height);
		logEntryBounds();
	}

	enum InputTarget {
		ROOM_NAME,
		PASSWORD,
		PLAYER_NAME
	}

	private float addInput(String label, int maxLength, float pos, InputTarget target) {
		RenderedTextBlock labelText = PixelScene.renderTextBlock(label, 6);
		labelText.maxWidth(inputWidth);
		labelText.setPos(MARGIN, pos);
		add(labelText);
		pos = labelText.bottom() + 1;

		TextInput input = new TextInput(Chrome.get(Chrome.Type.TOAST_WHITE), false,
				(int)PixelScene.uiCamera.zoom * 9){
			@Override
			public void onChanged() {
				clearError();
			}

			@Override
			public void tabPressed(boolean backwards) {
				focusInput(nextInputTarget(target, backwards));
			}
		};
		input.setMaxLength(maxLength);
		if (target == InputTarget.PASSWORD) {
			input.setPasswordMask('*');
		}
		add(input);
		PointerArea focusArea = new PointerArea(0, 0, 0, 0) {
			@Override
			protected void onPointerDown(com.watabou.input.PointerEvent event) {
				PointerEvent.clearKeyboardThisPress = false;
				input.focus();
			}

			@Override
			protected void onPointerUp(com.watabou.input.PointerEvent event) {
				PointerEvent.clearKeyboardThisPress = false;
				input.focus();
			}
		};
		focusArea.blockLevel = PointerArea.NEVER_BLOCK;
		add(focusArea);

		if (target == InputTarget.ROOM_NAME) {
			roomNameInput = input;
			roomNameFocusArea = focusArea;
			roomNameTop = pos;
		} else if (target == InputTarget.PASSWORD) {
			passwordInput = input;
			passwordFocusArea = focusArea;
			passwordTop = pos;
		} else {
			playerNameInput = input;
			playerNameFocusArea = focusArea;
			playerNameTop = pos;
		}
		input.setRect(MARGIN, pos, inputWidth, INPUT_HEIGHT);
		return pos + INPUT_HEIGHT + MARGIN*2;
	}

	static InputTarget nextInputTarget(InputTarget target, boolean backwards) {
		if (backwards) {
			switch (target) {
				case ROOM_NAME:
					return InputTarget.PLAYER_NAME;
				case PASSWORD:
					return InputTarget.ROOM_NAME;
				case PLAYER_NAME:
				default:
					return InputTarget.PASSWORD;
			}
		}
		switch (target) {
			case ROOM_NAME:
				return InputTarget.PASSWORD;
			case PASSWORD:
				return InputTarget.PLAYER_NAME;
			case PLAYER_NAME:
			default:
				return InputTarget.ROOM_NAME;
		}
	}

	private void focusInput(InputTarget target) {
		TextInput input;
		switch (target) {
			case ROOM_NAME:
				input = roomNameInput;
				break;
			case PASSWORD:
				input = passwordInput;
				break;
			case PLAYER_NAME:
			default:
				input = playerNameInput;
				break;
		}
		if (input != null) {
			PointerEvent.clearKeyboardThisPress = false;
			input.focus();
		}
	}

	private void submit() {
		if (pending) {
			return;
		}
		String roomName = roomNameInput.getText().trim();
		String password = passwordInput.getText();
		String playerName = playerNameInput.getText().trim();
		webParityLog("multiplayer room entry submit mode=" + mode.name()
				+ " roomNameLen=" + roomName.length()
				+ " passwordLen=" + password.length()
				+ " playerNameLen=" + playerName.length());
		String validationError = validate(roomName, password, playerName);
		if (validationError != null) {
			webParityLog("multiplayer room entry validation error mode=" + mode.name()
					+ " message=" + validationError.replace(' ', '_'));
			setError(validationError);
			return;
		}
		if (Game.platform == null
				|| !Game.platform.requestMultiplayerRoomEntry(mode.id, roomName, password, playerName)) {
			setError(Messages.get(this, "unavailable"));
			return;
		}
		pending = true;
		pendingRejoin = false;
		submitButton.enable(false);
		if (rejoinButton != null) {
			rejoinButton.enable(false);
		}
		setStatus(Messages.get(this, mode.pendingKey));
	}

	private void rejoin() {
		if (pending) {
			return;
		}
		webParityLog("multiplayer room entry rejoin submit");
		if (Game.platform == null || !Game.platform.requestMultiplayerRoomRejoin()) {
			hideRejoinButton();
			setError(Messages.get(this, "rejoin_unavailable"));
			return;
		}
		pending = true;
		pendingRejoin = true;
		submitButton.enable(false);
		if (rejoinButton != null) {
			rejoinButton.enable(false);
		}
		setStatus(Messages.get(this, "rejoining"));
	}

	private void hideRejoinButton() {
		if (rejoinButton != null) {
			rejoinButton.visible = false;
			rejoinButton.active = false;
		}
	}

	private String validate(String roomName, String password, String playerName) {
		String messageKey = validationMessageKey(roomName, password, playerName);
		return messageKey == null ? null : Messages.get(this, messageKey);
	}

	static String validationMessageKey(String roomName, String password, String playerName) {
		if (roomName.isEmpty()) {
			return "room_name_required";
		}
		if (!ROOM_NAME.matcher(roomName).matches()) {
			return "room_name_chars";
		}
		if (roomName.length() < ROOM_NAME_MIN) {
			return "room_name_short";
		}
		if (roomName.length() > ROOM_NAME_MAX) {
			return "room_name_long";
		}
		if (password.isEmpty()) {
			return "password_required";
		}
		if (password.length() < PASSWORD_MIN) {
			return "password_short";
		}
		if (password.length() > PASSWORD_MAX) {
			return "password_long";
		}
		if (playerName.length() < PLAYER_NAME_MIN) {
			return "player_name_required";
		}
		if (playerName.length() > PLAYER_NAME_MAX) {
			return "player_name_long";
		}
		return null;
	}

	private void setError(String message) {
		errorText.text(message, inputWidth);
		errorText.setPos(MARGIN, errorText.top());
		errorText.hardlight(0xFF8E75);
	}

	private void setStatus(String message) {
		errorText.text(message, inputWidth);
		errorText.setPos(MARGIN, errorText.top());
		errorText.hardlight(Window.SHPX_COLOR);
	}

	private void clearError() {
		if (errorText != null && errorText.text() != null && !errorText.text().isEmpty()) {
			errorText.text("", inputWidth);
		}
	}

	private void refreshInputBounds() {
		if (roomNameInput != null) {
			roomNameInput.setRect(MARGIN, roomNameTop, inputWidth, INPUT_HEIGHT);
			roomNameFocusArea.x = MARGIN;
			roomNameFocusArea.y = roomNameTop;
			roomNameFocusArea.width = inputWidth;
			roomNameFocusArea.height = INPUT_HEIGHT;
		}
		if (passwordInput != null) {
			passwordInput.setRect(MARGIN, passwordTop, inputWidth, INPUT_HEIGHT);
			passwordFocusArea.x = MARGIN;
			passwordFocusArea.y = passwordTop;
			passwordFocusArea.width = inputWidth;
			passwordFocusArea.height = INPUT_HEIGHT;
		}
		if (playerNameInput != null) {
			playerNameInput.setRect(MARGIN, playerNameTop, inputWidth, INPUT_HEIGHT);
			playerNameFocusArea.x = MARGIN;
			playerNameFocusArea.y = playerNameTop;
			playerNameFocusArea.width = inputWidth;
			playerNameFocusArea.height = INPUT_HEIGHT;
		}
	}

	@Override
	public void update() {
		super.update();
		if (!pending || Game.platform == null) {
			return;
		}
		String event;
		while ((event = Game.platform.pollMultiplayerRoomEvent()) != null) {
			handleRoomEvent(event);
		}
	}

	private void handleRoomEvent(String event) {
		String[] parts = event.split("\\|", -1);
		if (parts.length == 0) {
			return;
		}
		webParityLog("multiplayer room entry event " + parts[0]);
		if ("room-status".equals(parts[0]) && parts.length >= 2) {
			setStatus(WndMultiplayerLobby.statusMessage(parts[1]));
		} else if ("room-error".equals(parts[0]) && parts.length >= 2) {
			boolean wasRejoin = pendingRejoin;
			pending = false;
			pendingRejoin = false;
			if (submitButton != null) {
				submitButton.enable(true);
			}
			if (wasRejoin) {
				hideRejoinButton();
			} else if (rejoinButton != null && rejoinButton.visible) {
				rejoinButton.enable(true);
			}
			setError(WndMultiplayerLobby.statusMessage(parts[1]));
		} else if ("room-lobby".equals(parts[0])) {
			webParityLog("multiplayer room entry opening lobby parts=" + parts.length);
			WndMultiplayerLobby lobby = new WndMultiplayerLobby(WndMultiplayerLobby.LobbyState.fromEvent(parts));
			webParityLog("multiplayer room entry lobby constructed");
			hide();
			ShatteredPixelDungeon.scene().addToFront(lobby);
			webParityLog("multiplayer room entry lobby added");
		} else if ("room-launch".equals(parts[0])) {
			WebMultiplayer.RoomLaunch launch = WebMultiplayer.RoomLaunch.fromEvent(parts);
			if (!launch.valid) {
				pending = false;
				pendingRejoin = false;
				submitButton.enable(true);
				if (rejoinButton != null && rejoinButton.visible) {
					rejoinButton.enable(true);
				}
				setError(Messages.get(WndMultiplayerLobby.class, "launch_invalid"));
			} else if (WebMultiplayer.launchRoomRun(launch)) {
				hide();
			} else if (launch.watcher) {
				setStatus(Messages.get(WndMultiplayerLobby.class, "launch_waiting"));
			} else {
				boolean wasRejoin = pendingRejoin;
				pending = false;
				pendingRejoin = false;
				submitButton.enable(true);
				if (wasRejoin) {
					hideRejoinButton();
				} else if (rejoinButton != null && rejoinButton.visible) {
					rejoinButton.enable(true);
				}
				setError(Messages.get(WndMultiplayerLobby.class, "launch_unavailable"));
			}
		}
	}

	private int parseInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public void offset(int xOffset, int yOffset) {
		super.offset(xOffset, yOffset);
		refreshInputBounds();
	}

	private static void webParityLog(String message) {
		if (DeviceCompat.webParityLoggingEnabled()) {
			LOG.info("[WEB-PARITY] " + message);
		}
	}

	private void logEntryBounds() {
		logControlBounds("input", "room_name", roomNameInput);
		logControlBounds("input", "room_password", passwordInput);
		logControlBounds("input", "player_name", playerNameInput);
		logControlBounds("button", "back", backButton);
		logControlBounds("button", "rejoin", rejoinButton);
		logControlBounds("button", "submit", submitButton);
	}

	private void logControlBounds(String kind, String target, Component control) {
		if (!DeviceCompat.webParityLoggingEnabled() || control == null || camera == null) {
			return;
		}
		float globalX = camera.x / camera.zoom + control.left();
		float globalY = camera.y / camera.zoom + control.top();
		webParityLog("multiplayer room entry " + kind + " bounds mode=" + mode.name()
				+ " target=" + target
				+ " x=" + globalX
				+ " y=" + globalY
				+ " width=" + control.width()
				+ " height=" + control.height()
				+ " centerX=" + (globalX + control.width() / 2f)
				+ " centerY=" + (globalY + control.height() / 2f));
	}
}
