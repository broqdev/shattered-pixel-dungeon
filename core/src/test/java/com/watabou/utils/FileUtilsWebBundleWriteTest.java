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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Clipboard;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FileUtilsWebBundleWriteTest {

	private Application originalApp;
	private Files originalFiles;

	@Before
	public void setUp() {
		originalApp = Gdx.app;
		originalFiles = Gdx.files;
	}

	@After
	public void tearDown() {
		Gdx.app = originalApp;
		Gdx.files = originalFiles;
		FileUtils.setDefaultFileProperties(null, "");
	}

	@Test
	public void webBundleWriteUsesSingleDirectWrite() throws Exception {
		RecordingFiles files = new RecordingFiles();
		Gdx.app = new TestWebApplication();
		Gdx.files = files;
		FileUtils.setDefaultFileProperties(Files.FileType.Local, "base/");

		Bundle bundle = new Bundle();
		bundle.put("value", 42);

		FileUtils.bundleToFile("game.dat", bundle);

		assertEquals(1, new HashSet<>(files.writes).size());
		assertEquals("base/game.dat", files.writes.get(0));
		assertTrue(files.deletes.isEmpty());
		assertTrue(files.moves.isEmpty());
	}

	@Test
	public void webSaveMirrorProvidesSynchronousReadbackForSaveFiles() throws Exception {
		RecordingFiles files = new RecordingFiles();
		Gdx.app = new TestWebApplication();
		Gdx.files = files;
		FileUtils.setDefaultFileProperties(Files.FileType.Local, "base/");

		Bundle bundle = new Bundle();
		bundle.put("value", 42);

		FileUtils.bundleToFile("game1/game.dat", bundle);
		files.data.put("base/game1/game.dat", new byte[0]);

		assertTrue(FileUtils.dirExists("game1"));
		assertTrue(FileUtils.fileLength("game1/game.dat") > 1);
		assertEquals(42, FileUtils.bundleFromFile("game1/game.dat").getInt("value"));

		FileUtils.deleteFile("game1/game.dat");

		assertEquals(0, FileUtils.fileLength("game1/game.dat"));
	}

	@Test
	public void webSaveMirrorTreatsOverwriteAsLatestSaveState() throws Exception {
		RecordingFiles files = new RecordingFiles();
		Gdx.app = new TestWebApplication();
		Gdx.files = files;
		FileUtils.setDefaultFileProperties(Files.FileType.Local, "base/");

		Bundle bundle = new Bundle();
		bundle.put("value", 42);

		FileUtils.bundleToFile("game1/game.dat", bundle);
		byte[] oldSaveBytes = files.data.get("base/game1/game.dat");

		FileUtils.overwriteFile("game1/game.dat", 1);
		files.data.put("base/game1/game.dat", oldSaveBytes);

		assertEquals(1, FileUtils.fileLength("game1/game.dat"));
		try {
			FileUtils.bundleFromFile("game1/game.dat");
			fail("Expected overwritten web save mirror to block stale bundle fallback");
		} catch (IOException expected) {
			// expected
		}
	}

	private static class RecordingFiles implements Files {
		final Map<String, byte[]> data = new HashMap<>();
		final ArrayList<String> writes = new ArrayList<>();
		final ArrayList<String> deletes = new ArrayList<>();
		final ArrayList<String> moves = new ArrayList<>();

		@Override
		public FileHandle getFileHandle(String path, FileType type) {
			return new RecordingFileHandle(this, path);
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

	private static class RecordingFileHandle extends FileHandle {
		private final RecordingFiles files;
		private final String path;

		RecordingFileHandle(RecordingFiles files, String path) {
			this.files = files;
			this.path = path;
		}

		@Override
		public String path() {
			return path;
		}

		@Override
		public String name() {
			int slash = path.lastIndexOf('/');
			return slash == -1 ? path : path.substring(slash + 1);
		}

		@Override
		public boolean exists() {
			return files.data.containsKey(path);
		}

		@Override
		public boolean isDirectory() {
			return false;
		}

		@Override
		public long length() {
			byte[] bytes = files.data.get(path);
			return bytes == null ? 0 : bytes.length;
		}

		@Override
		public ByteArrayInputStream read() {
			byte[] bytes = files.data.get(path);
			return new ByteArrayInputStream(bytes == null ? new byte[0] : bytes);
		}

		@Override
		public OutputStream write(boolean append) {
			ByteArrayOutputStream output = new ByteArrayOutputStream() {
				@Override
				public void close() {
					files.writes.add(path);
					files.data.put(path, toByteArray());
				}
			};
			return output;
		}

		@Override
		public boolean delete() {
			files.deletes.add(path);
			return files.data.remove(path) != null;
		}

		@Override
		public void moveTo(FileHandle dest) {
			files.moves.add(path + "->" + dest.path());
			files.data.put(dest.path(), files.data.get(path));
			delete();
		}
	}

	private static class TestWebApplication implements Application {
		private final Map<String, Preferences> preferences = new HashMap<>();

		@Override public ApplicationListener getApplicationListener() { return null; }
		@Override public Graphics getGraphics() { return null; }
		@Override public Audio getAudio() { return null; }
		@Override public Input getInput() { return null; }
		@Override public Files getFiles() { return null; }
		@Override public Net getNet() { return null; }
		@Override public void log(String tag, String message) { }
		@Override public void log(String tag, String message, Throwable exception) { }
		@Override public void error(String tag, String message) { }
		@Override public void error(String tag, String message, Throwable exception) { }
		@Override public void debug(String tag, String message) { }
		@Override public void debug(String tag, String message, Throwable exception) { }
		@Override public void setLogLevel(int logLevel) { }
		@Override public int getLogLevel() { return LOG_NONE; }
		@Override public void setApplicationLogger(ApplicationLogger applicationLogger) { }
		@Override public ApplicationLogger getApplicationLogger() { return null; }
		@Override public ApplicationType getType() { return ApplicationType.WebGL; }
		@Override public int getVersion() { return 0; }
		@Override public long getJavaHeap() { return 0; }
		@Override public long getNativeHeap() { return 0; }
		@Override public Preferences getPreferences(String name) {
			return preferences.computeIfAbsent(name, ignored -> new MemoryPreferences());
		}
		@Override public Clipboard getClipboard() { return null; }
		@Override public void postRunnable(Runnable runnable) { runnable.run(); }
		@Override public void exit() { }
		@Override public void addLifecycleListener(LifecycleListener listener) { }
		@Override public void removeLifecycleListener(LifecycleListener listener) { }
	}

	private static class MemoryPreferences implements Preferences {
		private final Map<String, Object> values = new HashMap<>();

		@Override public Preferences putBoolean(String key, boolean val) { values.put(key, val); return this; }
		@Override public Preferences putInteger(String key, int val) { values.put(key, val); return this; }
		@Override public Preferences putLong(String key, long val) { values.put(key, val); return this; }
		@Override public Preferences putFloat(String key, float val) { values.put(key, val); return this; }
		@Override public Preferences putString(String key, String val) { values.put(key, val); return this; }
		@Override public Preferences put(Map<String, ?> vals) { values.putAll(vals); return this; }
		@Override public boolean getBoolean(String key) { return getBoolean(key, false); }
		@Override public int getInteger(String key) { return getInteger(key, 0); }
		@Override public long getLong(String key) { return getLong(key, 0); }
		@Override public float getFloat(String key) { return getFloat(key, 0); }
		@Override public String getString(String key) { return getString(key, ""); }
		@Override public boolean getBoolean(String key, boolean defValue) { Object value = values.get(key); return value instanceof Boolean ? (Boolean)value : defValue; }
		@Override public int getInteger(String key, int defValue) { Object value = values.get(key); return value instanceof Integer ? (Integer)value : defValue; }
		@Override public long getLong(String key, long defValue) { Object value = values.get(key); return value instanceof Long ? (Long)value : defValue; }
		@Override public float getFloat(String key, float defValue) { Object value = values.get(key); return value instanceof Float ? (Float)value : defValue; }
		@Override public String getString(String key, String defValue) { Object value = values.get(key); return value instanceof String ? (String)value : defValue; }
		@Override public Map<String, ?> get() { return new HashMap<>(values); }
		@Override public boolean contains(String key) { return values.containsKey(key); }
		@Override public void clear() { values.clear(); }
		@Override public void remove(String key) { values.remove(key); }
		@Override public void flush() { }
	}
}
