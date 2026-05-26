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

package com.watabou.utils;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ToxicGasRoom;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class BundleStaticMemberClassTest {

	@Test
	public void collectionRestoresStaticMemberBundlableClasses() {
		Bundle bundle = new Bundle();
		List<Bundlable> nestedObjects = Arrays.asList(
				new ToxicGasRoom.ToxicGasSeed(),
				new MagicalFireRoom.EternalFire(),
				new Room.Door(2, 3));

		bundle.put("nested_objects", nestedObjects);

		Iterator<Bundlable> restored = bundle.getCollection("nested_objects").iterator();
		assertTrue(restored.next() instanceof ToxicGasRoom.ToxicGasSeed);
		assertTrue(restored.next() instanceof MagicalFireRoom.EternalFire);
		Bundlable restoredDoor = restored.next();
		assertTrue(restoredDoor instanceof Room.Door);
		assertEquals(2, ((Room.Door) restoredDoor).x);
		assertEquals(3, ((Room.Door) restoredDoor).y);
	}

	@Test
	public void directGetRestoresStaticMemberBundlableClasses() {
		Bundle bundle = new Bundle();
		bundle.put("blob", new ToxicGasRoom.ToxicGasSeed());

		Bundlable restored = bundle.get("blob");

		assertEquals(ToxicGasRoom.ToxicGasSeed.class, restored.getClass());
	}

	@Test
	public void collectionRestoresStaticMemberBlobCells() {
		TestLevel level = new TestLevel();
		ToxicGasRoom.ToxicGasSeed seed = new ToxicGasRoom.ToxicGasSeed();
		seed.seed(level, 12, 12);

		Bundle bundle = new Bundle();
		bundle.put("blobs", Arrays.asList(seed));

		Blob restored = (Blob) bundle.getCollection("blobs").iterator().next();
		assertEquals(ToxicGasRoom.ToxicGasSeed.class, restored.getClass());
		assertEquals(12, restored.volume);
		assertEquals(12, restored.cur[12]);
	}

	@Test
	public void collectionSkipsBundlablesThatNeedOwningInstanceToRestore() {
		Bundle bundle = new Bundle();
		bundle.put("inner_objects", Arrays.asList(new InnerBundlable()));

		assertTrue(bundle.getCollection("inner_objects").isEmpty());
	}

	@Test
	public void getEnumReturnsCanonicalConstants() {
		Bundle bundle = new Bundle();
		bundle.put("transition_type", LevelTransition.Type.REGULAR_EXIT);
		bundle.put("door_type", Room.Door.Type.LOCKED);

		assertSame(LevelTransition.Type.REGULAR_EXIT,
				bundle.getEnum("transition_type", LevelTransition.Type.class));
		assertSame(Room.Door.Type.LOCKED,
				bundle.getEnum("door_type", Room.Door.Type.class));
	}

	private class InnerBundlable implements Bundlable {
		@Override
		public void restoreFromBundle(Bundle bundle) {
		}

		@Override
		public void storeInBundle(Bundle bundle) {
		}
	}

	private static class TestLevel extends Level {
		private TestLevel() {
			setSize(5, 5);
			Arrays.fill(map, Terrain.EMPTY);
		}

		@Override
		protected boolean build() {
			return true;
		}

		@Override
		protected void createMobs() {
		}

		@Override
		protected void createItems() {
		}
	}
}
