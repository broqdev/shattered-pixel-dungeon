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

package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.watabou.utils.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class BadgesGlobalStateTest {

	private Files previousFiles;
	private Object previousGlobal;
	private Field globalField;

	@Before
	public void setUp() throws Exception {
		previousFiles = Gdx.files;
		globalField = Badges.class.getDeclaredField("global");
		globalField.setAccessible(true);
		previousGlobal = globalField.get(null);
		globalField.set(null, null);
		Gdx.files = new MissingFiles();
		FileUtils.setDefaultFileProperties(Files.FileType.Local, "");
	}

	@After
	public void tearDown() throws Exception {
		Gdx.files = previousFiles;
		FileUtils.setDefaultFileProperties(null, "");
		globalField.set(null, previousGlobal);
	}

	@Test
	public void isUnlockedLoadsGlobalBadgesBeforeChecking() throws Exception {
		assertFalse(Badges.isUnlocked(Badges.Badge.RESEARCHER_1));
		assertNotNull(globalField.get(null));
	}

	private static class MissingFiles implements Files {
		@Override
		public FileHandle getFileHandle(String path, FileType type) {
			return new MissingFileHandle(path);
		}

		@Override public FileHandle classpath(String path) { return getFileHandle(path, FileType.Classpath); }
		@Override public FileHandle internal(String path) { return getFileHandle(path, FileType.Internal); }
		@Override public FileHandle external(String path) { return getFileHandle(path, FileType.External); }
		@Override public FileHandle absolute(String path) { return getFileHandle(path, FileType.Absolute); }
		@Override public FileHandle local(String path) { return getFileHandle(path, FileType.Local); }
		@Override public String getExternalStoragePath() { return ""; }
		@Override public boolean isExternalStorageAvailable() { return true; }
		@Override public String getLocalStoragePath() { return ""; }
		@Override public boolean isLocalStorageAvailable() { return true; }
	}

	private static class MissingFileHandle extends FileHandle {
		private final String path;

		MissingFileHandle(String path) {
			this.path = path;
		}

		@Override
		public String path() {
			return path;
		}

		@Override
		public boolean exists() {
			return false;
		}

		@Override
		public boolean isDirectory() {
			return false;
		}

		@Override
		public long length() {
			return 0;
		}

		@Override
		public InputStream read() {
			throw new GdxRuntimeException("missing test file");
		}
	}
}
