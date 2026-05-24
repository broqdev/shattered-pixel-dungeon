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

import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

public class ButtonKeyRepeatTest {

	private static final int TEST_KEY = 33;

	private TestButton button;

	@Before
	public void setUp() {
		KeyEvent.clearListeners();
		PointerEvent.clearListeners();
		KeyBindings.setAllBindings(new LinkedHashMap<>());
		KeyBindings.setAllControllerBindings(new LinkedHashMap<>());
		Button.pressedButton = null;

		LinkedHashMap<Integer, GameAction> bindings = new LinkedHashMap<>();
		bindings.put(TEST_KEY, SPDAction.EXAMINE);
		KeyBindings.setAllBindings(bindings);

		button = new TestButton(SPDAction.EXAMINE);
	}

	@After
	public void tearDown() {
		if (button != null) {
			button.destroy();
		}
		KeyEvent.clearListeners();
		PointerEvent.clearListeners();
		KeyBindings.setAllBindings(new LinkedHashMap<>());
		KeyBindings.setAllControllerBindings(new LinkedHashMap<>());
		Button.pressedButton = null;
	}

	@Test
	public void repeatedKeyDownDoesNotRestartArmedButton() {
		sendKey(true);
		sendKey(true);
		sendKey(false);

		assertEquals(1, button.pointerDowns);
		assertEquals(1, button.pointerUps);
		assertEquals(1, button.clicks);
	}

	@Test
	public void keyUpCompletesArmedButtonEvenIfItBecameInactive() {
		sendKey(true);
		button.active = false;
		sendKey(false);

		assertEquals(1, button.pointerDowns);
		assertEquals(1, button.pointerUps);
		assertEquals(1, button.clicks);
	}

	private void sendKey(boolean pressed) {
		KeyEvent.addKeyEvent(new KeyEvent(TEST_KEY, pressed));
		KeyEvent.processKeyEvents();
	}

	private static class TestButton extends Button {

		private final GameAction action;
		int pointerDowns;
		int pointerUps;
		int clicks;

		private TestButton(GameAction action) {
			this.action = action;
		}

		@Override
		public GameAction keyAction() {
			return action;
		}

		@Override
		protected void onPointerDown() {
			pointerDowns++;
		}

		@Override
		protected void onPointerUp() {
			pointerUps++;
		}

		@Override
		protected void onClick() {
			clicks++;
		}
	}
}
