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

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.multiplayer.WebMultiplayer;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

import java.util.logging.Logger;

public class WndMultiplayerLobby extends Window {

	private static final Logger LOG = Logger.getLogger(WndMultiplayerLobby.class.getName());

	private static final int WIDTH_P = 160;
	private static final int WIDTH_L = 260;
	private static final int MARGIN = 3;
	private static final int BUTTON_HEIGHT = 18;
	private static final int SEAT_HEIGHT = 19;
	private static final int HERO_ICON_WIDTH = 16;
	private static final int HERO_BUTTON_SIZE = 21;
	private static final float PENDING_TIMEOUT_SECONDS = 3f;

	private final LobbyState state;
	private RenderedTextBlock statusText;
	private RedButton startButton;
	private CheckBox infiniteButton;
	private RedButton actionButton;
	private String actionButtonAction = "";
	private RedButton leaveButton;
	private boolean lobbyBoundsLogged;
	private boolean pendingStatus;
	private float pendingStatusDeadline;
	private boolean closed;

	public WndMultiplayerLobby(LobbyState state) {
		super();
		this.state = state == null ? new LobbyState() : state;

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		float pos = MARGIN;

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title", this.state.roomName), 9);
		title.hardlight(Window.TITLE_COLOR);
		title.maxWidth(width - MARGIN*2);
		title.setPos(MARGIN, pos);
		add(title);
		pos = title.bottom() + MARGIN*2;

		if (PixelScene.landscape()) {
			int panelWidth = (width - MARGIN*3) / 2;
			float leftHeight = addLeftPanel(MARGIN, pos, panelWidth);
			float rightHeight = addRightPanel(MARGIN*2 + panelWidth, pos, panelWidth);
			pos += Math.max(leftHeight, rightHeight) + MARGIN;
		} else {
			pos += addLeftPanel(MARGIN, pos, width - MARGIN*2) + MARGIN;
			pos += addRightPanel(MARGIN, pos, width - MARGIN*2) + MARGIN;
		}

		statusText = PixelScene.renderTextBlock(6);
		statusText.maxWidth(width - MARGIN*2);
		statusText.setPos(MARGIN, pos);
		add(statusText);
		pos += 12;

		pos = addLobbyAction(MARGIN, pos, width - MARGIN*2) + MARGIN;

		resize(width, (int)pos);
		if (!DeviceCompat.hasHardKeyboard()) {
			boundOffsetWithMargin(0);
		}
		webParityLog("multiplayer lobby opened room=" + state.roomName
				+ " self=" + state.selfParticipantId
				+ " owner=" + state.selfOwner
				+ " watcher=" + state.selfIsWatcher()
				+ " players=" + state.playerCount()
				+ " watchers=" + state.watcherCount
				+ " connections=" + state.participantCount
				+ "/" + state.maxConnections);
		logLobbyBounds();
	}

	private float addLeftPanel(float x, float y, int width) {
		float pos = y;
		RenderedTextBlock heading = PixelScene.renderTextBlock(Messages.get(this, "players"), 8);
		heading.hardlight(Window.TITLE_COLOR);
		heading.setPos(x, pos);
		add(heading);
		pos = heading.bottom() + MARGIN;

		for (int i = 0; i < LobbyState.MAX_SEATS; i++) {
			pos = addSeatRow(x, pos, width, i, state.seats[i]) + MARGIN;
		}

		return pos - y;
	}

	private float addSeatRow(float x, float y, int width, int index, Participant participant) {
		RenderedTextBlock name = PixelScene.renderTextBlock(6);
		if (!participant.occupied()) {
			name.text(Messages.get(this, "seat_empty"), width);
			name.hardlight(0xAAAAAA);
			name.setPos(x + 1, y + 5);
			add(name);
			return y + SEAT_HEIGHT;
		}

		Image hero = heroImage(participant.heroChoice);
		hero.x = x + 1;
		hero.y = y + 2;
		add(hero);

		name.text(Messages.get(this, "seat_occupied", participant.name), width - HERO_ICON_WIDTH - 12);
		name.hardlight(participant.color);
		name.setPos(x + HERO_ICON_WIDTH + 2, y);
		add(name);

		if (participant.owner) {
			addOwnerLabelAfterName(name, x, y, width);
		}

		RenderedTextBlock detail = PixelScene.renderTextBlock(6);
		detail.text(seatDetail(participant), width - HERO_ICON_WIDTH - 2);
		detail.setPos(x + HERO_ICON_WIDTH + 2, y + 8);
		if (participant.ready) {
			detail.hardlight(Window.SHPX_COLOR);
		}
		add(detail);

		return y + SEAT_HEIGHT;
	}

	private float addHeroChoice(float x, float y, int width) {
		HeroClass selfHero = heroClass(state.selfParticipant().heroChoice);
		float posX = x;
		float posY = y;
		for (HeroClass heroClass : HeroClass.values()) {
			IconButton button = new IconButton(new Image(heroClass.spritesheet(), 0, 90, 12, 15)){
				@Override
				protected void onClick() {
					requestAction("hero-choice", heroClass.name());
				}
			};
			button.enable(!state.selfIsWatcher());
			if (heroClass == selfHero) {
				button.icon().brightness(1.5f);
			} else {
				button.icon().brightness(0.45f);
			}
			if (posX + HERO_BUTTON_SIZE > x + width) {
				posX = x;
				posY += HERO_BUTTON_SIZE + MARGIN;
			}
			button.setRect(posX, posY, HERO_BUTTON_SIZE, HERO_BUTTON_SIZE);
			add(button);
			posX += HERO_BUTTON_SIZE + MARGIN;
		}
		return posY + HERO_BUTTON_SIZE;
	}

	private float addRightPanel(float x, float y, int width) {
		float pos = y;
		RenderedTextBlock heading = PixelScene.renderTextBlock(Messages.get(this, "rules"), 8);
		heading.hardlight(Window.TITLE_COLOR);
		heading.setPos(x, pos);
		add(heading);
		pos = heading.bottom() + MARGIN;

		RenderedTextBlock rules = PixelScene.renderTextBlock(6);
		rules.text(Messages.get(this, "floor_chase"), width);
		rules.setPos(x, pos);
		add(rules);
		pos = rules.bottom() + MARGIN;

		RenderedTextBlock ruleControl = PixelScene.renderTextBlock(Messages.get(this, "rule_owner_controls"), 8);
		ruleControl.hardlight(Window.TITLE_COLOR);
		ruleControl.setPos(x, pos);
		add(ruleControl);
		pos = ruleControl.bottom() + MARGIN;

		RenderedTextBlock ruleLimit = PixelScene.renderTextBlock(6);
		ruleLimit.text(state.floorChaseInfinite ? Messages.get(this, "floor_chase_setting_infinite")
				: Messages.get(this, "floor_chase_setting_turns", state.floorChaseTurns), width);
		ruleLimit.hardlight(Window.SHPX_COLOR);
		ruleLimit.setPos(x, pos);
		add(ruleLimit);
		pos = ruleLimit.bottom() + MARGIN;

		if (!state.selfOwner) {
			RenderedTextBlock ownerOnly = PixelScene.renderTextBlock(Messages.get(this, "rule_owner_only"), 6);
			ownerOnly.maxWidth(width);
			ownerOnly.hardlight(0xAAAAAA);
			ownerOnly.setPos(x, pos);
			add(ownerOnly);
			pos = ownerOnly.bottom() + MARGIN;
		}

		OptionSlider turns = new OptionSlider(Messages.get(this, "floor_turns"), "10", "100", 1, 10) {
			@Override
			protected void onChange() {
				requestAction("floor-turns", Integer.toString(getSelectedValue()*10));
			}
		};
		turns.setSelectedValue(Math.max(1, Math.min(10, state.floorChaseTurns/10)));
		turns.enable(state.selfOwner && !state.floorChaseInfinite);
		turns.setRect(x, pos, width, 24);
		add(turns);
		pos += 26;

		infiniteButton = new CheckBox(Messages.get(this, "infinite")){
			@Override
			protected void onClick() {
				super.onClick();
				requestAction("floor-infinite", Boolean.toString(checked()));
			}
		};
		infiniteButton.checked(state.floorChaseInfinite);
		infiniteButton.enable(state.selfOwner);
		infiniteButton.setRect(x, pos, width, 16);
		add(infiniteButton);
		pos += 18;

		RenderedTextBlock heroes = PixelScene.renderTextBlock(Messages.get(this, "hero_choice"), 8);
		heroes.hardlight(Window.TITLE_COLOR);
		heroes.setPos(x, pos);
		add(heroes);
		pos = heroes.bottom() + MARGIN;

		pos = addHeroChoice(x, pos, width) + MARGIN;

		RenderedTextBlock seed = PixelScene.renderTextBlock(6);
		seed.text(Messages.get(this, "seed", state.seedChecksum == null || state.seedChecksum.isEmpty()
				? "-" : state.seedChecksum), width);
		seed.setPos(x, pos);
		add(seed);
		pos = seed.bottom() + MARGIN;

		RenderedTextBlock count = PixelScene.renderTextBlock(6);
		count.text(Messages.get(this, "connections", state.participantCount, state.maxConnections), width);
		count.setPos(x, pos);
		add(count);
		pos = count.bottom();

		return pos - y;
	}

	private float addLobbyAction(float x, float y, int width) {
		leaveButton = new RedButton(Messages.get(this, "leave")){
			@Override
			protected void onClick() {
				if (Game.platform != null) {
					Game.platform.leaveMultiplayerRoomEntry();
				}
				hide();
			}
		};

		float leaveWidth = Math.min(52, width);
		if (state.selfOwner) {
			startButton = new RedButton(state.startButtonText()){
				@Override
				protected void onClick() {
					requestAction("start-toggle", "");
				}
			};
			startButton.enable(state.startButtonEnabled());
			startButton.setRect(x, y, width - leaveWidth - MARGIN, BUTTON_HEIGHT);
			add(startButton);
			actionButton = startButton;
			actionButtonAction = "start-toggle";
			leaveButton.setRect(startButton.right() + MARGIN, y, leaveWidth, BUTTON_HEIGHT);
			add(leaveButton);
			return y + BUTTON_HEIGHT;
		}
		if (state.selfIsWatcher()) {
			leaveButton.setRect(x, y, width, BUTTON_HEIGHT);
			add(leaveButton);
			return y + BUTTON_HEIGHT;
		}
		RedButton ready = new RedButton(Messages.get(this, state.selfReady() ? "unready" : "ready_button")){
			@Override
			protected void onClick() {
				requestAction("ready-toggle", "");
			}
		};
		ready.setRect(x, y, width - leaveWidth - MARGIN, BUTTON_HEIGHT);
		add(ready);
		actionButton = ready;
		actionButtonAction = "ready-toggle";
		leaveButton.setRect(ready.right() + MARGIN, y, leaveWidth, BUTTON_HEIGHT);
		add(leaveButton);
		return y + BUTTON_HEIGHT;
	}

	private boolean requestAction(String action, String value) {
		if (Game.platform == null || !Game.platform.requestMultiplayerRoomAction(action, value)) {
			setStatus(Messages.get(this, "action_unavailable"));
			return false;
		}
		setPendingStatus();
		return true;
	}

	private String seatDetail(Participant participant) {
		String hero = Messages.get(this, "hero_" + participant.heroChoice.toLowerCase());
		String ready = participant.ready ? Messages.get(this, "ready") : Messages.get(this, "not_ready");
		return Messages.get(this, "seat_detail", hero, ready);
	}

	private Image heroImage(String heroChoice) {
		HeroClass heroClass = heroClass(heroChoice);
		return new Image(heroClass.spritesheet(), 0, 90, 12, 15);
	}

	private void addOwnerLabelAfterName(RenderedTextBlock name, float x, float y, int width) {
		RenderedTextBlock ownerLabel = PixelScene.renderTextBlock(Messages.get(this, "room_owner_label"), 5);
		ownerLabel.hardlight(Window.SHPX_COLOR);
		ownerLabel.setPos(Math.min(x + width - ownerLabel.width(), name.right() + 3), y + 1);
		add(ownerLabel);
	}

	private HeroClass heroClass(String heroChoice) {
		try {
			return HeroClass.valueOf(heroChoice == null || heroChoice.isEmpty() ? "WARRIOR" : heroChoice);
		} catch (IllegalArgumentException e) {
			return HeroClass.WARRIOR;
		}
	}

	private void setStatus(String message) {
		pendingStatus = false;
		pendingStatusDeadline = 0f;
		setStatusText(statusMessage(message));
	}

	private void setPendingStatus() {
		pendingStatus = true;
		pendingStatusDeadline = Game.timeTotal + PENDING_TIMEOUT_SECONDS;
		setStatusText(Messages.get(this, "pending"));
	}

	private void setStatusText(String message) {
		if (!closed && statusText != null && statusText.parent != null) {
			statusText.text(message == null ? "" : message, width - MARGIN*2);
			statusText.hardlight(Window.SHPX_COLOR);
		}
	}

	private void clearTimedOutPendingStatus() {
		if (pendingStatusExpired(pendingStatusDeadline, Game.timeTotal)) {
			pendingStatus = false;
			pendingStatusDeadline = 0f;
			setStatusText(Messages.get(this, "pending_timeout"));
		}
	}

	static boolean pendingStatusExpired(float deadline, float now) {
		return deadline > 0f && now >= deadline;
	}

	public static String statusMessage(String message) {
		if (message == null) {
			return "";
		}
		String key = statusMessageKey(message);
		return key == null ? message : Messages.get(WndMultiplayerLobby.class, key);
	}

	static String statusMessageKey(String message) {
		switch (message) {
			case "Room created.":
				return "status_room_created";
			case "Room joined.":
				return "status_room_joined";
			case "Waiting for room owner...":
				return "status_waiting_owner";
			case "Leaving room...":
				return "status_leaving";
			case "Left room.":
				return "status_left";
			case "Only the room owner can change this setting.":
				return "status_owner_only";
			case "Need at least one player.":
				return "start_need_player_status";
			case "Waiting for players to ready.":
				return "start_need_ready_status";
			case "Waiting for players to reconnect.":
				return "status_waiting_reconnect";
			case "stale room state":
				return "reject_stale_room";
			case "watchers cannot ready":
				return "reject_watcher_ready";
			case "player seats full":
				return "reject_seats_full";
			case "watchers cannot choose a hero":
				return "reject_watcher_hero";
			case "invalid hero":
				return "reject_invalid_hero";
			case "unknown action":
				return "reject_unknown_action";
			case "Room action rejected.":
				return "reject_action";
			case "room full":
				return "reject_room_full";
			case "game already started":
				return "reject_game_started";
			case "room rejoin available":
				return "reject_rejoin_available";
			default:
				return null;
		}
	}

	@Override
	public void update() {
		super.update();
		if (closed) {
			return;
		}
		logLobbyBounds();
		updateStartButton();
		if (Game.platform == null) {
			return;
		}
		String event;
		while (!closed && (event = Game.platform.pollMultiplayerRoomEvent()) != null) {
			if (!handleRoomEvent(event)) {
				return;
			}
		}
		if (!closed) {
			clearTimedOutPendingStatus();
		}
	}

	private void updateStartButton() {
		if (startButton == null || !state.countdownActive) {
			return;
		}
		startButton.text(state.startButtonText());
		startButton.enable(state.startButtonEnabled());
	}

	private boolean handleRoomEvent(String event) {
		String[] parts = event.split("\\|", -1);
		if (parts.length == 0) {
			return true;
		}
		webParityLog("multiplayer lobby event " + parts[0]);
		if ("room-lobby".equals(parts[0])) {
			WndMultiplayerLobby lobby = new WndMultiplayerLobby(LobbyState.fromEvent(parts));
			hide();
			ShatteredPixelDungeon.scene().addToFront(lobby);
			return shouldContinuePollingAfterRoomEvent(parts[0], false);
		} else if ("room-launch".equals(parts[0])) {
			WebMultiplayer.RoomLaunch launch = WebMultiplayer.RoomLaunch.fromEvent(parts);
			if (!launch.valid) {
				setStatus(Messages.get(this, "launch_invalid"));
			} else if (WebMultiplayer.launchRoomRun(launch)) {
				hide();
				return shouldContinuePollingAfterRoomEvent(parts[0], true);
			} else if (launch.watcher) {
				setStatus(Messages.get(this, "launch_waiting"));
			} else {
				setStatus(Messages.get(this, "launch_unavailable"));
			}
		} else if ("room-status".equals(parts[0]) && parts.length >= 2) {
			setStatus(parts[1]);
		} else if ("room-error".equals(parts[0]) && parts.length >= 2) {
			setStatus(parts[1]);
		}
		return shouldContinuePollingAfterRoomEvent(parts[0], false);
	}

	static boolean shouldContinuePollingAfterRoomEvent(String eventType, boolean launchStarted) {
		return !"room-lobby".equals(eventType)
				&& (!"room-launch".equals(eventType) || !launchStarted);
	}

	@Override
	public void hide() {
		closed = true;
		super.hide();
	}

	@Override
	public void destroy() {
		closed = true;
		super.destroy();
	}

	private void logLobbyBounds() {
		if (lobbyBoundsLogged) {
			return;
		}
		if (!DeviceCompat.webParityLoggingEnabled()) {
			lobbyBoundsLogged = true;
			return;
		}
		if (camera == null) {
			return;
		}
		logControlBounds("floor-infinite", infiniteButton, "enabled=" + state.selfOwner
				+ " infinite=" + state.floorChaseInfinite);
		if (actionButton != null) {
			logControlBounds(actionButtonAction, actionButton, actionButtonExtra());
		}
		logControlBounds("leave", leaveButton, "enabled=true");
		lobbyBoundsLogged = true;
	}

	private String actionButtonExtra() {
		if ("start-toggle".equals(actionButtonAction)) {
			return "enabled=" + state.startButtonEnabled()
					+ " countdown=" + state.countdownActive
					+ " readyToStart=" + state.readyToStart()
					+ " players=" + state.playerCount()
					+ " connections=" + state.participantCount
					+ "/" + state.maxConnections;
		}
		if ("ready-toggle".equals(actionButtonAction)) {
			return "enabled=true ready=" + state.selfReady();
		}
		return "enabled=true";
	}

	private void logControlBounds(String action, Component control, String extra) {
		if (control == null) {
			return;
		}
		float globalX = camera.x / camera.zoom + control.left();
		float globalY = camera.y / camera.zoom + control.top();
		webParityLog("multiplayer lobby control bounds action=" + action
				+ " " + extra
				+ " x=" + globalX
				+ " y=" + globalY
				+ " width=" + control.width()
				+ " height=" + control.height()
				+ " centerX=" + (globalX + control.width() / 2f)
				+ " centerY=" + (globalY + control.height() / 2f));
	}

	private static void webParityLog(String message) {
		if (DeviceCompat.webParityLoggingEnabled()) {
			LOG.info("[WEB-PARITY] " + message);
		}
	}

	public static class LobbyState {

		private static final int MAX_SEATS = 4;
		private static final int MAX_CONNECTIONS = 4;

		public String roomName = "";
		public String selfParticipantId = "";
		public boolean selfOwner;
		public String seedChecksum = "";
		public int participantCount = 1;
		public int maxConnections = MAX_CONNECTIONS;
		public int floorChaseTurns = 40;
		public boolean floorChaseInfinite;
		public boolean countdownActive;
		public long countdownDeadlineMs;
		public final Participant[] seats = new Participant[MAX_SEATS];
		public Participant[] watchers = new Participant[0];
		public int watcherCount;

		public LobbyState() {
			for (int i = 0; i < seats.length; i++) {
				seats[i] = new Participant();
			}
		}

		public boolean selfIsWatcher() {
			if (selfParticipantId.isEmpty()) {
				return false;
			}
			for (Participant seat : seats) {
				if (seat.id.equals(selfParticipantId)) {
					return false;
				}
			}
			for (Participant watcher : watchers) {
				if (watcher.id.equals(selfParticipantId)) {
					return true;
				}
			}
			return false;
		}

		public boolean selfReady() {
			return selfParticipant().ready;
		}

		public int playerCount() {
			int count = 0;
			for (Participant seat : seats) {
				if (seat.occupied()) {
					count++;
				}
			}
			return count;
		}

		public boolean readyToStart() {
			if (playerCount() == 0) {
				return false;
			}
			for (Participant seat : seats) {
				if (!seat.occupied()) {
					continue;
				}
				if (!seat.connected) {
					return false;
				}
				if (!seat.owner && !seat.ready) {
					return false;
				}
			}
			return true;
		}

		public boolean startButtonEnabled() {
			return selfOwner && (countdownActive || readyToStart());
		}

		public String startButtonText() {
			if (countdownActive) {
				int seconds = Math.max(0, (int)Math.ceil((countdownDeadlineMs - System.currentTimeMillis()) / 1000f));
				if (seconds == 0) {
					return Messages.get(WndMultiplayerLobby.class, "start_countdown", 0);
				}
				return Messages.get(WndMultiplayerLobby.class, "start_countdown", seconds);
			}
			if (playerCount() == 0) {
				return Messages.get(WndMultiplayerLobby.class, "start_need_player");
			}
			if (!readyToStart()) {
				return Messages.get(WndMultiplayerLobby.class, "start_need_ready");
			}
			return Messages.get(WndMultiplayerLobby.class, "start");
		}

		public Participant selfParticipant() {
			for (Participant seat : seats) {
				if (seat.id.equals(selfParticipantId)) {
					return seat;
				}
			}
			for (Participant watcher : watchers) {
				if (watcher.id.equals(selfParticipantId)) {
					return watcher;
				}
			}
			return new Participant();
		}

		public static LobbyState fromEvent(String[] parts) {
			LobbyState state = new LobbyState();
			if (parts.length < 9) {
				state.roomName = value(parts, 1);
				state.selfParticipantId = "";
				state.selfOwner = parseBoolean(value(parts, 3));
				state.seedChecksum = value(parts, 4);
				state.participantCount = parseInt(value(parts, 5), 1);
				state.seats[0] = new Participant("", value(parts, 2), 0xFFFFFF, state.selfOwner,
						"WARRIOR", false, true);
				return state;
			}
			state.roomName = value(parts, 1);
			state.selfParticipantId = value(parts, 2);
			state.selfOwner = parseBoolean(value(parts, 3));
			state.seedChecksum = value(parts, 4);
			state.participantCount = parseInt(value(parts, 5), 1);
			state.maxConnections = parseInt(value(parts, 6), MAX_CONNECTIONS);
			state.floorChaseTurns = parseInt(value(parts, 7), 40);
			state.floorChaseInfinite = parseBoolean(value(parts, 8));
			state.countdownActive = parseBoolean(value(parts, 9));
			state.countdownDeadlineMs = parseLong(value(parts, 10), 0L);

			int index = 11;
			for (int seat = 0; seat < MAX_SEATS; seat++) {
				state.seats[seat] = new Participant(
						value(parts, index),
						value(parts, index + 1),
						parseColor(value(parts, index + 2)),
						parseBoolean(value(parts, index + 3)),
						value(parts, index + 4),
						parseBoolean(value(parts, index + 5)),
						parseBoolean(value(parts, index + 6)));
				index += 7;
			}

			state.watcherCount = Math.max(0, parseInt(value(parts, index), 0));
			index++;
			state.watchers = new Participant[state.watcherCount];
			for (int i = 0; i < state.watcherCount; i++) {
				state.watchers[i] = new Participant(
						value(parts, index),
						value(parts, index + 1),
						parseColor(value(parts, index + 2)),
						parseBoolean(value(parts, index + 3)),
						"",
						false,
						parseBoolean(value(parts, index + 4)));
				index += 5;
			}
			return state;
		}

		private static String value(String[] parts, int index) {
			return index >= 0 && index < parts.length ? parts[index] : "";
		}

		private static int parseInt(String value, int fallback) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return fallback;
			}
		}

		private static long parseLong(String value, long fallback) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				return fallback;
			}
		}

		private static boolean parseBoolean(String value) {
			return "true".equals(value);
		}

		private static int parseColor(String value) {
			try {
				String color = value == null ? "" : value.replace("#", "");
				return color.isEmpty() ? 0xFFFFFF : Integer.parseInt(color, 16);
			} catch (NumberFormatException e) {
				return 0xFFFFFF;
			}
		}
	}

	public static class Participant {

		public final String id;
		public final String name;
		public final int color;
		public final boolean owner;
		public final String heroChoice;
		public final boolean ready;
		public final boolean connected;

		public Participant() {
			this("", "", 0xFFFFFF, false, "WARRIOR", false, true);
		}

		public Participant(String id, String name, int color, boolean owner, String heroChoice, boolean ready,
				boolean connected) {
			this.id = id == null ? "" : id;
			this.name = name == null || name.isEmpty() ? "-" : name;
			this.color = color;
			this.owner = owner;
			this.heroChoice = heroChoice == null || heroChoice.isEmpty() ? "WARRIOR" : heroChoice;
			this.ready = ready;
			this.connected = connected;
		}

		public boolean occupied() {
			return !id.isEmpty();
		}
	}
}
