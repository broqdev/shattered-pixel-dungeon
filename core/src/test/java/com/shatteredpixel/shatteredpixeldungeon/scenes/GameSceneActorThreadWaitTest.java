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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class GameSceneActorThreadWaitTest {

	private Field actorThreadField;
	private Field currentActorField;
	private Thread originalActorThread;
	private Object originalCurrentActor;
	private boolean originalKeepActorThreadAlive;
	private Application originalApp;
	private Hero originalHero;
	private Level originalLevel;
	private Thread parkedActorThread;
	private final AtomicBoolean keepParkedThreadAlive = new AtomicBoolean();

	@Before
	public void setUp() throws Exception {
		actorThreadField = GameScene.class.getDeclaredField("actorThread");
		actorThreadField.setAccessible(true);
		originalActorThread = (Thread) actorThreadField.get(null);

		currentActorField = Actor.class.getDeclaredField("current");
		currentActorField.setAccessible(true);
		originalCurrentActor = currentActorField.get(null);
		originalKeepActorThreadAlive = Actor.keepActorThreadAlive;
		originalApp = Gdx.app;
		originalHero = Dungeon.hero;
		originalLevel = Dungeon.level;
	}

	@After
	public void tearDown() throws Exception {
		keepParkedThreadAlive.set(false);
		if (parkedActorThread != null) {
			parkedActorThread.interrupt();
			parkedActorThread.join(1000);
		}
		actorThreadField.set(null, originalActorThread);
		currentActorField.set(null, originalCurrentActor);
		Actor.keepActorThreadAlive = originalKeepActorThreadAlive;
		Gdx.app = originalApp;
		Dungeon.hero = originalHero;
		Dungeon.level = originalLevel;
	}

	@Test
	public void liveButParkedActorThreadDoesNotBlockWait() throws Exception {
		startParkedActorThread();
		currentActorField.set(null, null);
		actorThreadField.set(null, parkedActorThread);

		long started = System.nanoTime();
		boolean finished = new GameScene().waitForActorThread(250, false);
		long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

		assertTrue(finished);
		assertTrue("parked actor thread wait should return before timeout, elapsed=" + elapsedMillis,
				elapsedMillis < 100);
	}

	@Test
	public void webSceneSwitchDoesNotBlockRenderThreadWhileActorThreadIsProcessing() throws Exception {
		Gdx.app = new TestWebApplication();
		TestActor currentActor = new TestActor();
		startInterruptTolerantActorThread();
		currentActorField.set(null, currentActor);
		actorThreadField.set(null, parkedActorThread);
		Actor.keepActorThreadAlive = true;

		GameScene scene = new GameScene();
		long started = System.nanoTime();
		boolean ready = scene.readyForSceneSwitch();
		long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

		assertFalse(ready);
		assertFalse(Actor.keepActorThreadAlive);
		assertSame("processing state should stay true until the actor loop exits",
				currentActor, currentActorField.get(null));
		assertTrue("web scene switch check should not block, elapsed=" + elapsedMillis,
				elapsedMillis < 500);
	}

	@Test
	public void webSceneSwitchWaitsForParkedActorThreadToExitBeforeReady() throws Exception {
		Gdx.app = new TestWebApplication();
		Actor.keepActorThreadAlive = true;
		startSelfParkedActorThread();
		currentActorField.set(null, null);
		actorThreadField.set(null, parkedActorThread);

		GameScene scene = new GameScene();

		assertFalse(scene.readyForSceneSwitch());
		assertFalse(Actor.keepActorThreadAlive);

		parkedActorThread.join(1000);
		assertFalse("parked web actor thread should exit before scene switch is ready",
				parkedActorThread.isAlive());
		assertTrue(scene.readyForSceneSwitch());
	}

	@Test
	public void interruptingParkedWebActorThreadRequestsCooperativeStop() throws Exception {
		Gdx.app = new TestWebApplication();
		Actor.keepActorThreadAlive = true;
		startSelfParkedActorThread();
		currentActorField.set(null, null);
		actorThreadField.set(null, parkedActorThread);

		boolean finished = new GameScene().waitForActorThread(250, true);
		parkedActorThread.join(1000);

		assertTrue(finished);
		assertFalse(Actor.keepActorThreadAlive);
		assertFalse("parked web actor thread should be notified to exit", parkedActorThread.isAlive());
	}

	@Test
	public void webPauseStillRunsSavePathWhenActorThreadIsProcessing() throws Exception {
		Gdx.app = new TestWebApplication();
		startInterruptTolerantActorThread();
		currentActorField.set(null, new TestActor());
		actorThreadField.set(null, parkedActorThread);
		Actor.keepActorThreadAlive = true;
		Dungeon.hero = new Hero();
		Dungeon.hero.ready = false;

		TestPauseGameScene scene = new TestPauseGameScene();
		scene.onPause();

		assertTrue(scene.savedOnPause);
		assertTrue("pause wait must not request actor shutdown", Actor.keepActorThreadAlive);
		assertTrue("active actor test double should remain alive until tearDown", parkedActorThread.isAlive());
	}

	@Test
	public void actorProcessClearsCurrentWhenShutdownIsRequestedDuringAct() throws Exception {
		Actor.clear();
		Actor.keepActorThreadAlive = true;
		ShutdownDuringActActor actor = new ShutdownDuringActActor();
		Actor.add(actor);

		try {
			Actor.process();

			assertFalse(Actor.processing());
			assertNull(currentActorField.get(null));
		} finally {
			Actor.remove(actor);
		}
	}

	@Test
	public void webSceneSwitchWakesSpriteWaitingActorThreadBeforeReady() throws Exception {
		Gdx.app = new TestWebApplication();
		Actor.clear();
		Actor.keepActorThreadAlive = true;

		MovingWaitChar actor = new MovingWaitChar();
		CharSprite sprite = movingSpriteFor(actor);
		Actor.add(actor);

		parkedActorThread = new Thread(Actor::process, "SHPD Actor Thread sprite wait test");
		actorThreadField.set(null, parkedActorThread);
		parkedActorThread.start();

		waitUntilSpriteWait(actor);

		GameScene scene = new GameScene();
		assertFalse(scene.readyForSceneSwitch());
		parkedActorThread.join(1000);

		assertFalse("sprite-waiting web actor thread should exit after scene switch stop",
				parkedActorThread.isAlive());
		assertFalse("actor must not act after shutdown is requested while waiting on sprite",
				actor.acted.get());
		assertNull(currentActorField.get(null));
		assertTrue(scene.readyForSceneSwitch());

		synchronized (sprite) {
			sprite.isMoving = false;
			sprite.notifyAll();
		}
		Actor.clear();
	}

	@Test
	public void actorProcessDoesNotParkOnMovingSpriteWhenShutdownAlreadyRequested() throws Exception {
		Actor.clear();
		MovingWaitChar actor = new MovingWaitChar();
		CharSprite sprite = movingSpriteFor(actor);
		Actor.add(actor);
		Actor.requestThreadShutdown();

		parkedActorThread = new Thread(Actor::process, "SHPD Actor Thread pre-stopped sprite wait test");
		actorThreadField.set(null, parkedActorThread);
		parkedActorThread.start();
		parkedActorThread.join(500);

		boolean exited = !parkedActorThread.isAlive();
		if (!exited) {
			synchronized (sprite) {
				sprite.isMoving = false;
				sprite.notifyAll();
			}
			parkedActorThread.join(1000);
		}

		assertTrue("actor process must not enter sprite wait after cooperative shutdown is already requested",
				exited);
		assertFalse("actor must not act after cooperative shutdown is already requested",
				actor.acted.get());
		assertNull(currentActorField.get(null));
		Actor.clear();
	}

	@Test
	public void webSceneSwitchWaitsForBlobToFinishMidTickBeforeGasCanLookEmpty() throws Exception {
		Gdx.app = new TestWebApplication();
		Actor.clear();
		Actor.keepActorThreadAlive = true;
		Dungeon.level = new TestLevel();

		CountDownLatch insideEvolve = new CountDownLatch(1);
		CountDownLatch finishEvolve = new CountDownLatch(1);
		BlockingBlob gas = new BlockingBlob(insideEvolve, finishEvolve);
		gas.seed(Dungeon.level, 12, 30);
		Dungeon.level.blobs.put(BlockingBlob.class, gas);
		Actor.add(gas);

		parkedActorThread = new Thread(Actor::process, "SHPD Actor Thread blob shutdown test");
		actorThreadField.set(null, parkedActorThread);
		parkedActorThread.start();

		assertTrue("blob did not enter evolve", insideEvolve.await(1, TimeUnit.SECONDS));
		assertSame(gas, currentActorField.get(null));
		assertTrue("blob volume should be in the transient zero window", gas.volume == 0);

		GameScene scene = new GameScene();
		assertFalse(scene.readyForSceneSwitch());
		assertFalse(Actor.keepActorThreadAlive);
		assertSame("scene switching must still see the blob actor as processing until evolve completes",
				gas, currentActorField.get(null));

		finishEvolve.countDown();
		parkedActorThread.join(1000);

		assertFalse("blob actor thread should exit after the cooperative shutdown", parkedActorThread.isAlive());
		assertFalse(Actor.processing());
		assertTrue("gas should not be preserved in the transient zero-volume state", gas.volume > 0);
		Actor.clear();
	}

	@Test
	public void blobStorePreservesCurrentCellsDuringMidTickZeroVolumeWindow() throws Exception {
		Actor.clear();
		Actor.keepActorThreadAlive = true;
		Dungeon.level = new TestLevel();

		CountDownLatch insideEvolve = new CountDownLatch(1);
		CountDownLatch finishEvolve = new CountDownLatch(1);
		BlockingBlob gas = new BlockingBlob(insideEvolve, finishEvolve);
		gas.seed(Dungeon.level, 12, 30);
		Dungeon.level.blobs.put(BlockingBlob.class, gas);
		Actor.add(gas);

		parkedActorThread = new Thread(Actor::process, "SHPD Actor Thread blob save test");
		actorThreadField.set(null, parkedActorThread);
		parkedActorThread.start();

		assertTrue("blob did not enter evolve", insideEvolve.await(1, TimeUnit.SECONDS));
		assertTrue("blob volume should be in the transient zero window", gas.volume == 0);

		Bundle bundle = new Bundle();
		gas.storeInBundle(bundle);
		BlockingBlob restored = new BlockingBlob(new CountDownLatch(0), new CountDownLatch(0));
		restored.restoreFromBundle(bundle);

		finishEvolve.countDown();
		Actor.requestThreadShutdown();
		parkedActorThread.join(1000);

		assertTrue("mid-tick save should restore the visible gas cells", restored.volume > 0);
		Actor.clear();
	}

	private void startParkedActorThread() throws InterruptedException {
		Object lock = new Object();
		CountDownLatch parked = new CountDownLatch(1);
		keepParkedThreadAlive.set(true);

		parkedActorThread = new Thread(() -> {
			synchronized (lock) {
				parked.countDown();
				while (keepParkedThreadAlive.get()) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}, "SHPD Actor Thread test double");
		parkedActorThread.start();

		assertTrue("test actor thread did not park", parked.await(1, TimeUnit.SECONDS));
		assertTrue(parkedActorThread.isAlive());
	}

	private void startInterruptTolerantActorThread() throws InterruptedException {
		CountDownLatch started = new CountDownLatch(1);
		keepParkedThreadAlive.set(true);

		parkedActorThread = new Thread(() -> {
			started.countDown();
			while (keepParkedThreadAlive.get()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {
					// Keep the test double alive until tearDown explicitly stops it.
				}
			}
		}, "SHPD Actor Thread web test double");
		parkedActorThread.start();

		assertTrue("test actor thread did not start", started.await(1, TimeUnit.SECONDS));
		assertTrue(parkedActorThread.isAlive());
	}

	private void startSelfParkedActorThread() throws InterruptedException {
		CountDownLatch parked = new CountDownLatch(1);
		keepParkedThreadAlive.set(true);

		parkedActorThread = new Thread(() -> {
			synchronized (Thread.currentThread()) {
				parked.countDown();
				while (Actor.keepActorThreadAlive && keepParkedThreadAlive.get()) {
					try {
						Thread.currentThread().wait();
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}, "SHPD Actor Thread self-parked test double");
		parkedActorThread.start();

		assertTrue("test actor thread did not park on itself", parked.await(1, TimeUnit.SECONDS));
		assertTrue(parkedActorThread.isAlive());
	}

	private CharSprite movingSpriteFor(Char actor) {
		CharSprite sprite = new CharSprite();
		sprite.ch = actor;
		sprite.isMoving = true;
		actor.sprite = sprite;
		return sprite;
	}

	private void waitUntilSpriteWait(MovingWaitChar actor) throws Exception {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
		while (System.nanoTime() < deadline) {
			if (currentActorField.get(null) == actor
					&& parkedActorThread.isAlive()
					&& parkedActorThread.getState() == Thread.State.WAITING) {
				return;
			}
			Thread.sleep(10);
		}
		throw new AssertionError("actor thread did not wait on moving sprite, state="
				+ parkedActorThread.getState()
				+ " current=" + currentActorField.get(null));
	}

	private static class TestActor extends Actor {
		@Override
		protected boolean act() {
			return false;
		}
	}

	private static class ShutdownDuringActActor extends Actor {
		@Override
		protected boolean act() {
			Actor.requestThreadShutdown();
			return true;
		}
	}

	private static class MovingWaitChar extends Char {
		final AtomicBoolean acted = new AtomicBoolean();

		@Override
		protected boolean act() {
			acted.set(true);
			return false;
		}
	}

	private static class BlockingBlob extends Blob {
		private final CountDownLatch insideEvolve;
		private final CountDownLatch finishEvolve;

		private BlockingBlob(CountDownLatch insideEvolve, CountDownLatch finishEvolve) {
			this.insideEvolve = insideEvolve;
			this.finishEvolve = finishEvolve;
		}

		@Override
		protected void evolve() {
			insideEvolve.countDown();
			try {
				finishEvolve.await(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			super.evolve();
		}
	}

	private static class TestLevel extends Level {
		private TestLevel() {
			setSize(5, 5);
			Arrays.fill(map, Terrain.EMPTY);
			Arrays.fill(passable, true);
			blobs = new java.util.HashMap<>();
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

	private static class TestPauseGameScene extends GameScene {
		boolean savedOnPause;

		@Override
		protected void saveGameOnPause() throws IOException {
			savedOnPause = true;
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
