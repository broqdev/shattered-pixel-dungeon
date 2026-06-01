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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.multiplayer.WebMultiplayer;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.DeviceCompat;

import java.io.IOException;
import java.util.logging.Logger;

public class WndMultiplayerOutcome extends Window {

	private static final Logger LOG = Logger.getLogger(WndMultiplayerOutcome.class.getName());

	private static final int WIDTH = 132;
	private static final int MARGIN = 5;
	private static final int BUTTON_HEIGHT = 20;
	private static final float GAP = 4;

	public WndMultiplayerOutcome() {
		super();

		WebMultiplayer.RoomWinnerState winner = WebMultiplayer.roomWinner();
		boolean victory = winner != null;

		float pos = MARGIN;
		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this,
				victory ? "victory_title" : "no_winner_title"), 11);
		title.hardlight(Window.TITLE_COLOR);
		title.maxWidth(WIDTH - MARGIN * 2);
		title.setPos((WIDTH - title.width()) / 2f, pos);
		PixelScene.align(title);
		add(title);
		pos = title.bottom() + GAP;

		if (victory) {
			Image avatar = HeroSprite.avatar(winner.heroClass, winner.armorTier);
			avatar.scale.set(2.5f);
			avatar.x = (WIDTH - avatar.width()) / 2f;
			avatar.y = pos;
			PixelScene.align(avatar);
			add(avatar);
			pos = avatar.y + avatar.height() + GAP;

			RenderedTextBlock label = PixelScene.renderTextBlock(Messages.get(this, "winner"), 7);
			label.maxWidth(WIDTH - MARGIN * 2);
			label.setPos((WIDTH - label.width()) / 2f, pos);
			PixelScene.align(label);
			add(label);
			pos = label.bottom() + 1;

			RenderedTextBlock name = PixelScene.renderTextBlock(winner.playerName, 9);
			name.hardlight(winner.playerColor);
			name.maxWidth(WIDTH - MARGIN * 2);
			name.setPos((WIDTH - name.width()) / 2f, pos);
			PixelScene.align(name);
			add(name);
			pos = name.bottom() + GAP;
		} else {
			RenderedTextBlock body = PixelScene.renderTextBlock(Messages.get(this, "no_winner_body"), 7);
			body.maxWidth(WIDTH - MARGIN * 2);
			body.setPos((WIDTH - body.width()) / 2f, pos);
			PixelScene.align(body);
			add(body);
			pos = body.bottom() + GAP;
		}

		RedButton menu = new RedButton(Messages.get(this, "main_menu")) {
			@Override
			protected void onClick() {
				returnToMainMenu();
			}
		};
		menu.icon(Icons.get(Icons.DISPLAY));
		menu.setRect(MARGIN, pos, WIDTH - MARGIN * 2, BUTTON_HEIGHT);
		add(menu);

		resize(WIDTH, (int)(menu.bottom() + MARGIN));
		logOutcomeBounds(winner, menu);
	}

	@Override
	public void onBackPressed() {
		// Room outcomes are modal; use Main Menu to leave the room deliberately.
	}

	private void returnToMainMenu() {
		if (WebMultiplayer.roomWinnerKnown()) {
			WebMultiplayer.leaveRoomAfterVictory();
		} else {
			WebMultiplayer.leaveRoomAfterNoWinner();
		}
		try {
			Dungeon.saveAll("multiplayerOutcomeMainMenu");
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
		}
		Game.switchScene(TitleScene.class);
	}

	private static void logOutcomeBounds(WebMultiplayer.RoomWinnerState winner, RedButton menu) {
		if (!DeviceCompat.webParityLoggingEnabled()) {
			return;
		}
		if (winner != null) {
			LOG.info("[WEB-PARITY] multiplayer victory overlay rendered winnerId=" + token(winner.participantId)
					+ " playerName=" + token(winner.playerName)
					+ " playerColor=" + String.format("%06x", winner.playerColor & 0xFFFFFF)
					+ " heroClass=" + winner.heroClass
					+ " armorTier=" + winner.armorTier
					+ " mainMenuX=" + menu.left()
					+ " mainMenuY=" + menu.top()
					+ " mainMenuWidth=" + menu.width()
					+ " mainMenuHeight=" + menu.height()
					+ " mainMenuCenterX=" + menu.centerX()
					+ " mainMenuCenterY=" + menu.centerY());
		} else {
			LOG.info("[WEB-PARITY] multiplayer no-winner overlay rendered"
					+ " mainMenuX=" + menu.left()
					+ " mainMenuY=" + menu.top()
					+ " mainMenuWidth=" + menu.width()
					+ " mainMenuHeight=" + menu.height()
					+ " mainMenuCenterX=" + menu.centerX()
					+ " mainMenuCenterY=" + menu.centerY());
		}
	}

	private static String token(String value) {
		if (value == null || value.isEmpty()) {
			return "-";
		}
		return value.replace('|', '_').replace(' ', '_');
	}
}
