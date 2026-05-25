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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blizzard;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.EmptyRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.logging.Logger;

public class MagicalFireRoom extends SpecialRoom {

	private static final Logger LOG = Logger.getLogger(MagicalFireRoom.class.getName());

	@Override
	public int minWidth() { return 7; }
	public int minHeight() { return 7; }

	@Override
	public void paint(Level level) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		Door door = entrance();
		door.set( Door.Type.REGULAR );

		Point firePos = center();
		Room behindFire = new EmptyRoom();
		int fireCells = 0;
		StringBuilder fireCellSummary = new StringBuilder();

		if (door.x == left || door.x == right){
			firePos.y = top+1;
			while (firePos.y != bottom){
				int cell = level.pointToCell(firePos);
				Blob.seed(cell, 1, EternalFire.class, level);
				appendFireCell(fireCellSummary, cell);
				fireCells++;
				Painter.set(level, firePos, Terrain.EMPTY_SP);
				firePos.y++;
			}
			if (door.x == left){
				behindFire.set(firePos.x+1, top+1, right-1, bottom-1);
			} else {
				behindFire.set(left+1, top+1, firePos.x-1, bottom-1);
			}
		} else {
			firePos.x = left+1;
			while (firePos.x != right){
				int cell = level.pointToCell(firePos);
				Blob.seed(cell, 1, EternalFire.class, level);
				appendFireCell(fireCellSummary, cell);
				fireCells++;
				Painter.set(level, firePos, Terrain.EMPTY_SP);
				firePos.x++;
			}
			if (door.y == top){
				behindFire.set(left+1, firePos.y+1, right-1, bottom-1);
			} else {
				behindFire.set(left+1, top+1, right-1, firePos.y-1);
			}
		}

		Painter.fill(level, behindFire, Terrain.EMPTY_SP);

		boolean honeyPot = Random.Int( 2 ) == 0;

		int n = Random.IntRange( 3, 4 );

		for (int i=0; i < n; i++) {
			int pos;
			do {
				pos = level.pointToCell(behindFire.random(0));
			} while (level.heaps.get(pos) != null);
			if (honeyPot){
				level.drop( new Honeypot(), pos);
				honeyPot = false;
			} else
				level.drop( prize( level ), pos );
		}

		level.addItemToSpawn(new PotionOfFrost());
		webParityLog("magicalFire paint depth=" + Dungeon.depth
				+ " branch=" + Dungeon.branch
				+ " room=" + roomSummary(this)
				+ " door=" + door.x + ',' + door.y + '/' + door.type
				+ " fireCells=" + fireCells + '[' + fireCellSummary + ']'
				+ " behindFire=" + roomSummary(behindFire)
				+ " " + level.blobDebugSummary());

	}

	private static Item prize( Level level ) {

		if (Random.Int(3) != 0){
			Item prize = level.findPrizeItem();
			if (prize != null)
				return prize;
		}

		return Generator.random( Random.oneOf(
				Generator.Category.POTION,
				Generator.Category.SCROLL,
				Generator.Category.FOOD,
				Generator.Category.GOLD
		) );
	}

	@Override
	public boolean canPlaceGrass(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceCharacter(Point p, Level l) {
		Blob fire = l.blobs.get(EternalFire.class);

		//disallow placing on special tiles or next to fire if fire is present.
		//note that this is slightly brittle, assumes the fire is either all there or totally gone
		if (fire != null && fire.volume > 0){
			int cell = l.pointToCell(p);
			if (l.map[cell] == Terrain.EMPTY_SP) return false;

			if (fire.cur[cell] > 0)     return false;
			for (int i : PathFinder.NEIGHBOURS4){
				if (fire.cur[cell+i] > 0)   return false;
			}
		}

		return super.canPlaceCharacter(p, l);
	}

	public static class EternalFire extends Blob {

		private boolean webParityFirstEvolveLogged;

		@Override
		protected void evolve() {
			int beforeActive = activeCellCount(cur);
			int beforeTotal = totalBlobValue(cur);
			boolean logFirstEvolve = !webParityFirstEvolveLogged;
			if (logFirstEvolve) {
				webParityFirstEvolveLogged = true;
				webParityLog("eternalFire evolve begin depth=" + Dungeon.depth
						+ " branch=" + Dungeon.branch
						+ " beforeActive=" + beforeActive
						+ " beforeTotal=" + beforeTotal
						+ " " + eternalFireSnapshot(this)
						+ " related=" + relatedBlobSnapshot("freezing", Dungeon.level.blobs.get(Freezing.class))
						+ " " + relatedBlobSnapshot("blizzard", Dungeon.level.blobs.get(Blizzard.class))
						+ " " + relatedBlobSnapshot("fire", Dungeon.level.blobs.get(Fire.class)));
			}

			int cell;

			Freezing freeze = (Freezing)Dungeon.level.blobs.get( Freezing.class );
			Blizzard bliz = (Blizzard)Dungeon.level.blobs.get( Blizzard.class );

			Fire fire = (Fire)Dungeon.level.blobs.get( Fire.class );

			//if any part of the fire is cleared, cleanse the whole thing
			//Note that this is a bit brittle atm, it assumes only one group of eternal fire per floor
			boolean clearAll = false;

			Level l = Dungeon.level;
			for (int i = area.left - 1; i <= area.right; i++){
				for (int j = area.top - 1; j <= area.bottom; j++){
					cell = i + j*l.width();

					if (cur[cell] > 0){
						//evaporates in the presence of water, frost, or blizzard
						//this blob is not considered interchangeable with fire, so those blobs do not interact with it otherwise
						//potion of purity can cleanse it though
						if (l.water[cell]){
							cur[cell] = 0;
							clearAll = true;
						}
						//overrides fire
						if (fire != null && fire.volume > 0 && fire.cur[cell] > 0){
							fire.clear(cell);
						}

						//clears itself if there is frost/blizzard on or next to it
						for (int k : PathFinder.NEIGHBOURS9) {
							if (freeze != null && freeze.volume > 0 && freeze.cur[cell+k] > 0) {
								freeze.clear(cell);
								cur[cell] = 0;
								clearAll = true;
							}
							if (bliz != null && bliz.volume > 0 && bliz.cur[cell+k] > 0) {
								bliz.clear(cell);
								cur[cell] = 0;
								clearAll = true;
							}
						}
					}

					if (cur[cell] > 0
							|| cur[cell-1] > 0
							|| cur[cell+1] > 0
							|| cur[cell-Dungeon.level.width()] > 0
							|| cur[cell+Dungeon.level.width()] > 0) {

						//spread fire to nearby flammable cells
						if (Dungeon.level.flamable[cell] && (fire == null || fire.volume == 0 || fire.cur[cell] == 0)){
							GameScene.add(Blob.seed(cell, 4, Fire.class));
						}

						//ignite adjacent chars
						Char ch = Actor.findChar(cell);
						if (ch != null && !ch.isImmune(getClass())) {
							Buff.affect(ch, Burning.class).reignite(ch, 4f);
						}

						//burn adjacent heaps, but only on outside and non-water cells
						if (Dungeon.level.heaps.get(cell) != null
							&& Dungeon.level.map[cell] != Terrain.EMPTY_SP
							&& Dungeon.level.map[cell] != Terrain.WATER){
							Dungeon.level.heaps.get(cell).burn();
						}
					}

					off[cell] = cur[cell];
					volume += off[cell];
				}
			}

			if (clearAll){
				webParityLog("eternalFire evolve clearAll depth=" + Dungeon.depth
						+ " branch=" + Dungeon.branch
						+ " beforeActive=" + beforeActive
						+ " beforeTotal=" + beforeTotal
						+ " afterVolume=" + volume
						+ " " + eternalFireSnapshot(this));
				fullyClear();
			}
			if (logFirstEvolve || clearAll || beforeTotal != volume) {
				webParityLog("eternalFire evolve end depth=" + Dungeon.depth
						+ " branch=" + Dungeon.branch
						+ " clearAll=" + clearAll
						+ " beforeActive=" + beforeActive
						+ " beforeTotal=" + beforeTotal
						+ " afterVolume=" + volume
						+ " " + eternalFireSnapshot(this));
			}

		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			webParityLog("eternalFire store depth=" + Dungeon.depth
					+ " branch=" + Dungeon.branch
					+ " " + eternalFireSnapshot(this));
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			webParityLog("eternalFire restore depth=" + Dungeon.depth
					+ " branch=" + Dungeon.branch
					+ " " + eternalFireSnapshot(this));
		}

		@Override
		public void seed(Level level, int cell, int amount) {
			super.seed(level, cell, amount);
			level.updateCellFlags(cell);
			GameScene.updateMap(cell);
			webParityLog("eternalFire seed depth=" + Dungeon.depth
					+ " branch=" + Dungeon.branch
					+ " cell=" + cell
					+ " amount=" + amount
					+ " terrain=" + level.map[cell]
					+ " " + eternalFireSnapshot(this));
		}

		@Override
		public void clear(int cell) {
			int curValue = cur == null || cell < 0 || cell >= cur.length ? -1 : cur[cell];
			webParityLog("eternalFire clear requested depth=" + Dungeon.depth
					+ " branch=" + Dungeon.branch
					+ " cell=" + cell
					+ " curValue=" + curValue
					+ " " + eternalFireSnapshot(this));
			if (volume > 0 && cur[cell] > 0) {
				fullyClear();
			}
		}

		@Override
		public void fullyClear() {
			webParityLog("eternalFire fullyClear begin depth=" + Dungeon.depth
					+ " branch=" + Dungeon.branch
					+ " " + eternalFireSnapshot(this));
			super.fullyClear();
			Dungeon.level.buildFlagMaps();
			GameScene.updateMap();
			webParityLog("eternalFire fullyClear end depth=" + Dungeon.depth
					+ " branch=" + Dungeon.branch
					+ " " + eternalFireSnapshot(this)
					+ " levelBlobs=" + Dungeon.level.blobDebugSummary());
		}

		@Override
		public void use( BlobEmitter emitter ) {
			super.use( emitter );
			emitter.pour( ElmoParticle.FACTORY, 0.02f );
			webParityLog("eternalFire use emitter depth=" + Dungeon.depth
					+ " branch=" + Dungeon.branch
					+ " emitter=" + emitter.getClass().getName()
					+ " " + eternalFireSnapshot(this));
		}

		@Override
		public String tileDesc() {
			return Messages.get(this, "desc");
		}

		@Override
		public void onBuildFlagMaps( Level l ) {
			if (volume > 0){
				for (int i=0; i < l.length(); i++) {
					onUpdateCellFlags(l, i);
				}
			}
		}

		@Override
		public void onUpdateCellFlags(Level l, int cell) {
			if(volume > 0 && cur[cell] > 0) {
				l.passable[cell] = false;
				l.avoid[cell] = false;
			}
		}
	}

	private static void appendFireCell(StringBuilder summary, int cell) {
		if (summary.length() > 0) {
			summary.append(',');
		}
		summary.append(cell);
	}

	private static String roomSummary(Room room) {
		return room.left + "," + room.top + "-" + room.right + "," + room.bottom;
	}

	private static String eternalFireSnapshot(EternalFire fire) {
		return "volume=" + fire.volume
				+ " area=" + fire.area.left + ',' + fire.area.top
				+ '-' + fire.area.right + ',' + fire.area.bottom
				+ " activeCells=" + activeCellCount(fire.cur)
				+ " total=" + totalBlobValue(fire.cur)
				+ " sample=[" + cellSample(fire.cur) + ']';
	}

	private static String relatedBlobSnapshot(String name, Blob blob) {
		if (blob == null) {
			return name + "=null";
		}
		return name + "{volume=" + blob.volume
				+ " activeCells=" + activeCellCount(blob.cur)
				+ " total=" + totalBlobValue(blob.cur)
				+ " sample=[" + cellSample(blob.cur) + "]}";
	}

	private static int activeCellCount(int[] cells) {
		if (cells == null) {
			return 0;
		}
		int active = 0;
		for (int value : cells) {
			if (value > 0) {
				active++;
			}
		}
		return active;
	}

	private static int totalBlobValue(int[] cells) {
		if (cells == null) {
			return 0;
		}
		int total = 0;
		for (int value : cells) {
			total += value;
		}
		return total;
	}

	private static String cellSample(int[] cells) {
		if (cells == null) {
			return "cur=null";
		}
		StringBuilder sample = new StringBuilder();
		int active = 0;
		for (int i = 0; i < cells.length; i++) {
			if (cells[i] > 0) {
				if (active < 12) {
					if (sample.length() > 0) {
						sample.append(',');
					}
					sample.append(i).append(':').append(cells[i]);
				}
				active++;
			}
		}
		if (active > 12) {
			sample.append(",...");
		}
		return sample.toString();
	}

	private static void webParityLog(String message) {
		if (DeviceCompat.webParityLoggingEnabled()) {
			LOG.info("[WEB-PARITY] " + message);
		}
	}

}
