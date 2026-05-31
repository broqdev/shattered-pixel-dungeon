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

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;

public class MultiplayerChaseBanner extends Component {

	private static final int MAX_WIDTH = 150;

	private NinePatch bg;
	private RenderedTextBlock text;
	private String message;

	public MultiplayerChaseBanner(String message) {
		super();

		text(message);
		text.hardlight(0xFFD36A);
		alpha(1f);
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		bg = Chrome.get(Chrome.Type.TOAST_TR_HEAVY);
		add(bg);

		text = PixelScene.renderTextBlock(9);
		add(text);
	}

	@Override
	protected void layout() {
		text.setPos(x + bg.marginLeft() + 4, y + bg.marginTop() + 2);
		bg.x = x;
		bg.y = y;
		bg.size(text.width() + bg.marginHor() + 8, text.height() + bg.marginVer() + 4);

		width = bg.width;
		height = bg.height;
	}

	public void text(String message) {
		if (message.equals(this.message)) {
			return;
		}
		this.message = message;
		text.text(message, MAX_WIDTH);
		layout();
	}

	private void alpha(float value) {
		bg.alpha(value * 0.92f);
		text.alpha(value);
	}
}
