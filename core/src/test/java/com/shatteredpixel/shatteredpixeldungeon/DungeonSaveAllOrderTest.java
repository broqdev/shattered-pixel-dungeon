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
import com.badlogic.gdx.utils.Clipboard;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DungeonSaveAllOrderTest {

	private Application originalApp;
	private int originalDepth;
	private int originalBranch;
	private CapturingHandler logHandler;
	private boolean originalUseParentHandlers;

	@Before
	public void setUp() {
		originalApp = Gdx.app;
		originalDepth = Dungeon.depth;
		originalBranch = Dungeon.branch;
	}

	@After
	public void tearDown() {
		Gdx.app = originalApp;
		Dungeon.depth = originalDepth;
		Dungeon.branch = originalBranch;
		if (logHandler != null) {
			Logger logger = Logger.getLogger(Dungeon.class.getName());
			logger.removeHandler(logHandler);
			logger.setUseParentHandlers(originalUseParentHandlers);
		}
	}

	@Test
	public void saveAllWritesLevelBeforeGameMetadata() throws Exception {
		ArrayList<String> calls = new ArrayList<>();

		Dungeon.saveAll(1, new Dungeon.SaveAllSteps() {
			@Override
			public void fixTime() {
				calls.add("fixTime");
			}

			@Override
			public void updateLevelExplored() {
				calls.add("updateLevelExplored");
			}

			@Override
			public void saveLevel(int save) throws IOException {
				calls.add("saveLevel:" + save);
			}

			@Override
			public void saveGame(int save) throws IOException {
				calls.add("saveGame:" + save);
			}

			@Override
			public void setGameInProgress(int save) {
				calls.add("setGameInProgress:" + save);
			}
		});

		assertEquals(Arrays.asList(
				"fixTime",
				"updateLevelExplored",
				"saveLevel:1",
				"saveGame:1",
				"setGameInProgress:1"
		), calls);
	}

	@Test
	public void saveAllWebParityLogIncludesExplicitReason() throws Exception {
		Gdx.app = new TestWebApplication();
		Dungeon.depth = 3;
		Dungeon.branch = 0;
		captureDungeonLogs();

		Dungeon.saveAll(1, "gameScenePause", new Dungeon.SaveAllSteps() {
			@Override
			public void fixTime() {
			}

			@Override
			public void updateLevelExplored() {
			}

			@Override
			public void saveLevel(int save) {
			}

			@Override
			public void saveGame(int save) {
			}

			@Override
			public void setGameInProgress(int save) {
			}
		});

		assertTrue(logHandler.messages.stream().anyMatch(message ->
				message.contains("[WEB-PARITY] saveAll begin")
						&& message.contains("reason=gameScenePause")
						&& message.contains("depthFile=game1/depth3.dat")));
		assertTrue(logHandler.messages.stream().anyMatch(message ->
				message.contains("[WEB-PARITY] saveAll complete")
						&& message.contains("reason=gameScenePause")));
	}

	private void captureDungeonLogs() {
		Logger logger = Logger.getLogger(Dungeon.class.getName());
		originalUseParentHandlers = logger.getUseParentHandlers();
		logger.setUseParentHandlers(false);
		logHandler = new CapturingHandler();
		logger.addHandler(logHandler);
	}

	private static class CapturingHandler extends Handler {
		final ArrayList<String> messages = new ArrayList<>();

		@Override
		public void publish(LogRecord record) {
			if (record.getLevel().intValue() >= Level.INFO.intValue()) {
				messages.add(record.getMessage());
			}
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() {
		}
	}

	private static class TestWebApplication implements Application {
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
		@Override public Preferences getPreferences(String name) { return null; }
		@Override public Clipboard getClipboard() { return null; }
		@Override public void postRunnable(Runnable runnable) { runnable.run(); }
		@Override public void exit() { }
		@Override public void addLifecycleListener(LifecycleListener listener) { }
		@Override public void removeLifecycleListener(LifecycleListener listener) { }
	}
}
