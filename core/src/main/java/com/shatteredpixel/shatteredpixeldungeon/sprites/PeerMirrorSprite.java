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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.multiplayer.WebMultiplayer;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.GameMath;
import com.watabou.utils.PointF;

import java.util.ArrayList;

public class PeerMirrorSprite extends CharSprite {

	private static final int FRAME_WIDTH = 12;
	private static final int FRAME_HEIGHT = 15;

	private int cell = -1;
	private HeroClass heroClass;
	private int armorTier = -1;
	private PeerBuffStrip peerBuffStrip;

	public PeerMirrorSprite() {
		this(true);
	}

	protected PeerMirrorSprite(boolean loadAssets) {
		super();

		if (!loadAssets) {
			return;
		}

		renderShadow = true;
		alpha(0.4f);
		appearance(
				Dungeon.hero != null && Dungeon.hero.heroClass != null ? Dungeon.hero.heroClass : HeroClass.WARRIOR,
				Dungeon.hero != null ? Dungeon.hero.tier() : 0
		);
	}

	public int cell() {
		return cell;
	}

	public void appearance(HeroClass heroClass, int armorTier) {
		if (heroClass == null) {
			return;
		}
		armorTier = Math.max(0, Math.min(6, armorTier));
		if (this.heroClass == heroClass && this.armorTier == armorTier) {
			return;
		}
		this.heroClass = heroClass;
		this.armorTier = armorTier;
		texture(heroClass.spritesheet());
		updateArmor(armorTier);
		idle();
	}

	public void sync(int targetCell, boolean shouldShow) {
		if (targetCell < 0) {
			visible = false;
			syncPeerBuffStrip();
			return;
		}

		boolean animate = shouldShow && visible && cell >= 0
				&& Dungeon.level != null && Dungeon.level.adjacent(cell, targetCell);
		if (cell != targetCell) {
			if (animate) {
				move(cell, targetCell);
			} else {
				interruptMotion();
				place(targetCell);
				idle();
			}
			cell = targetCell;
		}
		visible = shouldShow;
		syncPeerBuffStrip();
	}

	public void peerBuffs(ArrayList<WebMultiplayer.PeerBuffState> peerBuffs) {
		if (peerBuffs == null || peerBuffs.isEmpty()) {
			if (peerBuffStrip != null) {
				peerBuffStrip.setBuffs(peerBuffs);
				syncPeerBuffStrip();
			}
			return;
		}

		ensurePeerBuffStrip().setBuffs(peerBuffs);
		syncPeerBuffStrip();
	}

	public void updateArmor(int tier) {
		TextureFilm film = new TextureFilm(HeroSprite.tiers(), tier, FRAME_WIDTH, FRAME_HEIGHT);

		idle = new Animation(1, true);
		idle.frames(film, 0, 0, 0, 1, 0, 0, 1, 1);

		run = new Animation(20, true);
		run.frames(film, 2, 3, 4, 5, 6, 7);

		die = new Animation(20, false);
		die.frames(film, 0);

		attack = new Animation(15, false);
		attack.frames(film, 13, 14, 15, 0);
	}

	@Override
	public void move(int from, int to) {
		turnTo(from, to);
		play(run);

		motion = new PosTweener(this, worldToCamera(to), DEFAULT_MOVE_INTERVAL);
		motion.listener = this;
		parent.add(motion);
		isMoving = true;
	}

	@Override
	public void update() {
		super.update();
		syncPeerBuffStrip();
	}

	@Override
	public void killAndErase() {
		if (peerBuffStrip != null) {
			peerBuffStrip.killAndErase();
			peerBuffStrip = null;
		}
		super.killAndErase();
	}

	@Override
	public void onComplete(Tweener tweener) {
		if (tweener == motion) {
			synchronized (this) {
				isMoving = false;
				motion.killAndErase();
				motion = null;
				idle();
				GameScene.sortMobSprites();
				notifyAll();
			}
		} else {
			super.onComplete(tweener);
		}
	}

	@Override
	public void bloodBurstA(PointF from, int damage) {
		// Peer mirrors are visual presence only, not damageable characters.
	}

	private PeerBuffStrip ensurePeerBuffStrip() {
		if (peerBuffStrip == null) {
			peerBuffStrip = new PeerBuffStrip();
			peerBuffStrip.visible = false;
		}
		if (parent != null && peerBuffStrip.parent != parent) {
			parent.add(peerBuffStrip);
		}
		return peerBuffStrip;
	}

	private void syncPeerBuffStrip() {
		if (peerBuffStrip != null) {
			peerBuffStrip.sync(this);
		}
	}

	private static class PeerBuffStrip extends Group {

		private static final int MAX_ICONS = 8;
		private static final int GAP = 1;

		private final ArrayList<PeerBuffIcon> icons = new ArrayList<>();
		private ArrayList<WebMultiplayer.PeerBuffState> displayedBuffs = new ArrayList<>();

		void setBuffs(ArrayList<WebMultiplayer.PeerBuffState> buffs) {
			if (sameBuffs(displayedBuffs, buffs)) {
				return;
			}

			for (PeerBuffIcon icon : icons) {
				icon.killAndErase();
			}
			icons.clear();
			displayedBuffs = copyBuffs(buffs);

			int count = Math.min(MAX_ICONS, displayedBuffs.size());
			for (int i = 0; i < count; i++) {
				PeerBuffIcon icon = new PeerBuffIcon(displayedBuffs.get(i));
				icons.add(icon);
				add(icon);
			}
		}

		void sync(PeerMirrorSprite sprite) {
			visible = sprite.visible && !icons.isEmpty();
			if (!visible) {
				return;
			}

			float width = icons.size() * BuffIndicator.SIZE_SMALL + (icons.size() - 1) * GAP;
			float left = PixelScene.align(sprite.x + (sprite.width() - width) / 2f);
			float top = PixelScene.align(sprite.y - BuffIndicator.SIZE_SMALL - 1);
			for (int i = 0; i < icons.size(); i++) {
				icons.get(i).setPos(left + i * (BuffIndicator.SIZE_SMALL + GAP), top);
			}
		}

		private static boolean sameBuffs(ArrayList<WebMultiplayer.PeerBuffState> left,
				ArrayList<WebMultiplayer.PeerBuffState> right) {
			int leftSize = left == null ? 0 : left.size();
			int rightSize = right == null ? 0 : right.size();
			if (leftSize != rightSize) {
				return false;
			}
			for (int i = 0; i < leftSize; i++) {
				WebMultiplayer.PeerBuffState a = left.get(i);
				WebMultiplayer.PeerBuffState b = right.get(i);
				if (a.icon != b.icon
						|| a.type != b.type
						|| Float.compare(a.fade, b.fade) != 0
						|| !a.className.equals(b.className)
						|| !a.text.equals(b.text)) {
					return false;
				}
			}
			return true;
		}

		private static ArrayList<WebMultiplayer.PeerBuffState> copyBuffs(ArrayList<WebMultiplayer.PeerBuffState> buffs) {
			ArrayList<WebMultiplayer.PeerBuffState> copy = new ArrayList<>();
			if (buffs != null) {
				copy.addAll(buffs);
			}
			return copy;
		}
	}

	private static class PeerBuffIcon extends Group {

		private final BuffIcon icon;
		private final Image grey;
		private final float fade;

		PeerBuffIcon(WebMultiplayer.PeerBuffState state) {
			icon = new BuffIcon(state.icon, false);
			add(icon);
			grey = new Image(TextureCache.createSolid(0xCC666666));
			add(grey);
			fade = GameMath.gate(0, state.fade, 1);
		}

		void setPos(float x, float y) {
			icon.x = x;
			icon.y = y;
			grey.x = x;
			grey.y = y;
			float fadeHeight = fade * icon.height();
			float zoom = (camera() != null) ? camera().zoom : 1;
			if (fadeHeight < icon.height() / 2f) {
				grey.scale.set(icon.width(), (float) Math.ceil(zoom * fadeHeight) / zoom);
			} else {
				grey.scale.set(icon.width(), (float) Math.floor(zoom * fadeHeight) / zoom);
			}
		}
	}
}
