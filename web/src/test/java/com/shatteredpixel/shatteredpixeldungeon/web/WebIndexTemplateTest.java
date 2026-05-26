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

package com.shatteredpixel.shatteredpixeldungeon.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class WebIndexTemplateTest {

	@Test
	public void gameBootstrapStartsOnLoadWithoutStartGate() throws IOException {
		String html = readIndexTemplate();
		assertFalse(html.contains("audio-start-gate"));
		assertFalse(html.contains("startGameFromGesture"));
		assertTrue(html.contains("function installCanvasInputOffsetShim()"));
		assertTrue(html.contains("function focusGameCanvas()"));
		assertTrue(html.contains("function focusActiveGameSurface()"));
		assertTrue(html.contains("offsetTop: {"));
		assertTrue(html.contains("function installDirectStartHowlerPolicy()"));
		assertFalse(html.contains("howler.usingWebAudio = false"));
		assertTrue(html.contains("howler.autoUnlock = true"));
		assertTrue(html.contains("audio direct start webaudio policy"));

		int loadStart = html.indexOf("function start()");
		int loadListener = html.indexOf("window.addEventListener(\"load\", start);");
		assertTrue(loadStart >= 0);
		assertTrue(loadListener > loadStart);
		assertTrue(html.substring(loadStart, loadListener).contains("gameStarted = true"));
		assertTrue(html.substring(loadStart, loadListener).contains("%MODE%"));
	}

	@Test
	public void releaseParityStripKeepsBootstrapHelpers() throws IOException {
		String html = BuildWeb.configureWebParityLogging(readIndexTemplate(), false);
		assertFalse(html.contains("%WEB_PARITY_LOGGING%"));
		assertFalse(html.contains("WEB_PARITY_LOGGING_ENABLED"));
		assertFalse(html.contains("WEB_PARITY_LOGGING_BEGIN"));
		assertFalse(html.contains("WEB_PARITY_LOGGING_END"));
		assertTrue(html.contains("window.__shpdWebParityLogging = false"));
		assertTrue(html.contains("function logWebParity()"));
		assertTrue(html.contains("function focusGameCanvas()"));
		assertTrue(html.contains("function focusActiveGameSurface()"));
		assertTrue(html.contains("window.addEventListener(\"focus\", focusActiveGameSurface);"));

		int loadStart = html.indexOf("function start()");
		int loadListener = html.indexOf("window.addEventListener(\"load\", start);");
		assertTrue(loadStart >= 0);
		assertTrue(loadListener > loadStart);
		assertTrue(html.substring(loadStart, loadListener).contains("focusGameCanvas()"));
	}

	private static String readIndexTemplate() throws IOException {
		try (InputStream input = WebIndexTemplateTest.class.getResourceAsStream("/webapp/index.html")) {
			assertTrue(input != null);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int read;
			while ((read = input.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
			return new String(output.toByteArray(), StandardCharsets.UTF_8);
		}
	}
}
