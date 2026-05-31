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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.multiplayer.WebMultiplayer;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

import java.util.ArrayList;
import java.util.logging.Logger;

public class MultiplayerPlayerList extends Component {

	private static final Logger LOG = Logger.getLogger(MultiplayerPlayerList.class.getName());

	private static final int WIDTH = 126;
	private static final int ROW_HEIGHT = 22;
	private static final int AVATAR_BOX = 18;
	private static final int BAR_WIDTH = 58;
	private static final int MAX_BUFFS = 5;
	private static final int GAP = 2;

	private String fingerprint = "";
	private String boundsFingerprint = "";

	@Override
	public synchronized void update() {
		super.update();

		ArrayList<WebMultiplayer.PlayerListRow> rows = WebMultiplayer.playerListRows();
		visible = active = !rows.isEmpty();
		String nextFingerprint = fingerprint(rows);
		if (!nextFingerprint.equals(fingerprint)) {
			fingerprint = nextFingerprint;
			rebuild(rows);
			layout();
		}
	}

	@Override
	protected void layout() {
		float pos = y;
		ArrayList<PlayerRow> rows = new ArrayList<>();
		StringBuilder bounds = new StringBuilder();
		for (int i = 0; i < length; i++) {
			if (members.get(i) instanceof PlayerRow) {
				PlayerRow playerRow = (PlayerRow) members.get(i);
				playerRow.setRect(x, pos, WIDTH, ROW_HEIGHT);
				rows.add(playerRow);
				bounds.append(playerRow.row.playerId).append(':')
						.append(playerRow.left()).append(',')
						.append(playerRow.top()).append(',')
						.append(playerRow.width()).append(',')
						.append(playerRow.height()).append(',')
						.append(playerRow.row.watchTarget).append(',')
						.append(playerRow.row.selectable).append(',')
						.append(playerRow.row.live()).append(';');
				pos += ROW_HEIGHT + GAP;
			}
		}
		width = WIDTH;
		height = Math.max(0, pos - y - GAP);
		logRowBoundsIfChanged(bounds.toString(), rows);
	}

	private void rebuild(ArrayList<WebMultiplayer.PlayerListRow> rows) {
		clear();
		for (WebMultiplayer.PlayerListRow row : rows) {
			add(new PlayerRow(row));
		}
	}

	private static String fingerprint(ArrayList<WebMultiplayer.PlayerListRow> rows) {
		StringBuilder builder = new StringBuilder();
		for (WebMultiplayer.PlayerListRow row : rows) {
			builder.append(row.playerId).append(':')
					.append(row.playerName).append(':')
					.append(row.playerColor).append(':')
					.append(row.heroClass.name()).append(':')
					.append(row.armorTier).append(':')
					.append(row.hp).append('/').append(row.ht).append(':')
					.append(row.connected).append(':')
					.append(row.gameOver).append(':')
					.append(row.timedOut).append(':')
					.append(row.watchTarget).append(':')
					.append(row.selectable);
			for (WebMultiplayer.PeerBuffState buff : row.peerBuffs) {
				builder.append(':').append(buff.icon).append(',')
						.append(buff.type.name()).append(',')
						.append(buff.text).append(',')
						.append(buff.fade);
			}
			builder.append(';');
		}
		return builder.toString();
	}

	private static class PlayerRow extends Component {

		private final WebMultiplayer.PlayerListRow row;
		private ColorBlock bg;
		private Image avatar;
		private Image watchTargetIcon;
		private BitmapText name;
		private HealthBar hpBar;
		private BitmapText hpText;
		private final ArrayList<Image> buffIcons = new ArrayList<>();
		private Image stateIcon;
		private PointerArea hotArea;

		private PlayerRow(WebMultiplayer.PlayerListRow row) {
			super();
			this.row = row;
			buildChildren();
			active = row.selectable;
		}

		@Override
		protected void createChildren() {
		}

		private void buildChildren() {
			bg = new ColorBlock(1, 1, 0x88000000);
			add(bg);

			avatar = HeroSprite.avatar(row.heroClass, row.armorTier);
			add(avatar);

			name = new BitmapText(PixelScene.pixelFont);
			name.text(compactName(row.playerName));
			name.hardlight(row.playerColor);
			name.measure();
			add(name);

			watchTargetIcon = new Speck().image(Speck.STAR);
			watchTargetIcon.scale.set(1.1f);
			watchTargetIcon.hardlight(0xFFFF44);
			add(watchTargetIcon);

			hpBar = new HealthBar();
			add(hpBar);

			hpText = new BitmapText(PixelScene.pixelFont);
			hpText.alpha(0.7f);
			hpText.text(hpText(row));
			hpText.measure();
			add(hpText);

			int count = Math.min(MAX_BUFFS, row.peerBuffs.size());
			for (int i = 0; i < count; i++) {
				Image icon = new BuffIcon(row.peerBuffs.get(i).icon, false);
				buffIcons.add(icon);
				add(icon);
			}

			if (!row.live()) {
				stateIcon = (row.gameOver || row.timedOut ? Icons.SKULL : Icons.LOST).get();
				add(stateIcon);
			}

			hotArea = new PointerArea(0, 0, 0, 0) {
				@Override
				protected void onClick(com.watabou.input.PointerEvent event) {
					boolean result = WebMultiplayer.selectPlayerListRow(row);
					webParityLog("multiplayer player-list row clicked playerId=" + token(row.playerId)
							+ " selectable=" + row.selectable
							+ " live=" + row.live()
							+ " watchTarget=" + row.watchTarget
							+ " result=" + result);
				}
			};
			add(hotArea);
		}

		@Override
		protected void layout() {
			super.layout();

			bg.x = x;
			bg.y = y;
			bg.size(width, height);

			float avatarX = x + 3;
			float avatarY = y + 2;

			avatar.x = avatarX + (AVATAR_BOX - avatar.width()) / 2f;
			avatar.y = avatarY + (AVATAR_BOX - avatar.height()) / 2f;
			PixelScene.align(avatar);

			watchTargetIcon.visible = row.watchTarget;

			float textX = x + AVATAR_BOX + 7;
			float textWidth = width - (textX - x) - 4;
			if (stateIcon != null) {
				textWidth -= 10;
			}
			if (row.watchTarget) {
				textWidth -= watchTargetIcon.width() + 3;
			}

			name.x = textX;
			name.y = y + 2;
			fitText(name, textWidth);
			PixelScene.align(name);

			watchTargetIcon.x = name.x + name.width() + 2;
			watchTargetIcon.y = name.y;
			PixelScene.align(watchTargetIcon);

			hpBar.setRect(textX, y + 11, BAR_WIDTH, 2);
			if (row.ht > 0) {
				hpBar.level(Math.max(0, row.hp) / (float)row.ht);
			} else {
				hpBar.level(0f);
			}

			hpText.x = textX + (BAR_WIDTH - hpText.width()) / 2f;
			hpText.y = y + 8;
			PixelScene.align(hpText);

			float iconX = textX;
			float iconY = y + 15;
			for (Image icon : buffIcons) {
				icon.x = iconX;
				icon.y = iconY;
				PixelScene.align(icon);
				iconX += BuffIndicator.SIZE_SMALL + 1;
			}

			if (stateIcon != null) {
				stateIcon.x = x + width - stateIcon.width() - 4;
				stateIcon.y = y + (height - stateIcon.height()) / 2f;
				PixelScene.align(stateIcon);
			}

			hotArea.x = x;
			hotArea.y = y;
			hotArea.width = width;
			hotArea.height = height;
			hotArea.active = row.selectable;

			float alpha = row.live() ? 1f : 0.42f;
			avatar.alpha(alpha);
			name.alpha(alpha);
			hpBar.alpha(alpha);
			hpText.alpha(alpha * 0.7f);
			for (Image icon : buffIcons) {
				icon.alpha(alpha);
			}
			if (stateIcon != null) {
				stateIcon.alpha(0.85f);
			}
			bg.alpha(row.live() ? 0.72f : 0.44f);
			watchTargetIcon.alpha(row.live() ? 1f : 0.55f);
		}

		private static String compactName(String name) {
			if (name == null || name.isEmpty()) {
				return "-";
			}
			return name.length() <= 16 ? name : name.substring(0, 15) + ".";
		}

		private static String hpText(WebMultiplayer.PlayerListRow row) {
			if (row.ht <= 0) {
				return "-/-";
			}
			return Math.max(0, row.hp) + "/" + row.ht;
		}

		private static void fitText(BitmapText text, float maxWidth) {
			text.measure();
			if (text.width() > maxWidth && maxWidth > 0) {
				text.scale.set(Math.max(0.5f, maxWidth / text.width()));
			} else {
				text.scale.set(1f);
			}
			text.measure();
		}
	}

	private void logRowBoundsIfChanged(String nextBoundsFingerprint, ArrayList<PlayerRow> rows) {
		if (!DeviceCompat.webParityLoggingEnabled() || nextBoundsFingerprint.equals(boundsFingerprint)) {
			return;
		}
		boundsFingerprint = nextBoundsFingerprint;
		for (PlayerRow row : rows) {
			logRowBounds(row);
		}
	}

	private static void logRowBounds(PlayerRow row) {
		webParityLog("multiplayer player-list row bounds playerId=" + token(row.row.playerId)
				+ " name=" + token(row.row.playerName)
				+ " local=" + row.row.localPlayer
				+ " watchTarget=" + row.row.watchTarget
				+ " selectable=" + row.row.selectable
				+ " live=" + row.row.live()
				+ " connected=" + row.row.connected
				+ " gameOver=" + row.row.gameOver
				+ " timedOut=" + row.row.timedOut
				+ " x=" + row.left()
				+ " y=" + row.top()
				+ " width=" + row.width()
				+ " height=" + row.height()
				+ " centerX=" + row.centerX()
				+ " centerY=" + row.centerY());
	}

	private static void webParityLog(String message) {
		if (DeviceCompat.webParityLoggingEnabled()) {
			LOG.info("[WEB-PARITY] " + message);
		}
	}

	private static String token(String value) {
		if (value == null || value.isEmpty()) {
			return "-";
		}
		return value.replace('|', '_').replace(' ', '_');
	}
}
