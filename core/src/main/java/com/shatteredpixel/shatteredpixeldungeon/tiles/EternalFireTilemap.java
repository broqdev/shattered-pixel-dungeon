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

package com.shatteredpixel.shatteredpixeldungeon.tiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom;

public class EternalFireTilemap extends DungeonTilemap {

	private static final int EMBERS = 9 + 16*5;

	public EternalFireTilemap() {
		super(Assets.Environment.TERRAIN_FEATURES);
		hardlight(0.35f, 1.8f, 0.75f);

		if (Dungeon.level != null) {
			map(Dungeon.level.map, Dungeon.level.width());
		}
	}

	@Override
	protected int getTileVisual(int pos, int tile, boolean flat) {
		if (hasEternalFire(pos)) {
			return EMBERS + (tileVariance(pos) >= 50 ? 1 : 0);
		}
		return -1;
	}

	private static int tileVariance(int pos) {
		if (DungeonTileSheet.tileVariance == null || pos < 0 || pos >= DungeonTileSheet.tileVariance.length) {
			return 0;
		}
		return DungeonTileSheet.tileVariance[pos];
	}

	public static boolean hasEternalFire(int pos) {
		Blob fire = Dungeon.level == null ? null : Dungeon.level.blobs.get(MagicalFireRoom.EternalFire.class);
		return fire != null
				&& fire.volume > 0
				&& fire.cur != null
				&& pos >= 0
				&& pos < fire.cur.length
				&& fire.cur[pos] > 0;
	}

	public static int activeCellCount() {
		Blob fire = Dungeon.level == null ? null : Dungeon.level.blobs.get(MagicalFireRoom.EternalFire.class);
		if (fire == null || fire.cur == null) {
			return 0;
		}
		int active = 0;
		for (int value : fire.cur) {
			if (value > 0) {
				active++;
			}
		}
		return active;
	}

	public static String activeCellSample() {
		Blob fire = Dungeon.level == null ? null : Dungeon.level.blobs.get(MagicalFireRoom.EternalFire.class);
		if (fire == null || fire.cur == null) {
			return "cur=null";
		}
		StringBuilder sample = new StringBuilder();
		int active = 0;
		for (int i = 0; i < fire.cur.length; i++) {
			if (fire.cur[i] > 0) {
				if (active < 12) {
					if (sample.length() > 0) {
						sample.append(',');
					}
					sample.append(i).append(':').append(fire.cur[i]);
				}
				active++;
			}
		}
		if (active > 12) {
			sample.append(",...");
		}
		return sample.toString();
	}
}
