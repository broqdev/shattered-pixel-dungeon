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

package com.watabou.noosa;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.watabou.input.PointerEvent;
import com.watabou.glscripts.Script;
import com.watabou.glwrap.Blending;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.FileUtils;
import com.watabou.utils.Point;

//essentially contains a libGDX text input field, plus a PD-rendered background
public class TextInput extends Component {

	private static TextInput activeInput;

	private Stage stage;
	private Container container;
	private TextField textField;
	private final boolean multiline;
	private boolean inputProcessorRegistered;

	private Skin skin;

	private NinePatch bg;

	public static void clearActiveInput(){
		if (activeInput != null) {
			activeInput.deactivate(true);
			activeInput = null;
		}
	}

	public TextInput( NinePatch bg, boolean multiline, int size ){
		super();
		this.multiline = multiline;
		this.bg = bg;
		add(bg);

		//use a custom viewport here to ensure stage camera matches game camera
		Viewport viewport = new Viewport() {};
		viewport.setWorldSize(Game.width, Game.height);
		viewport.setScreenBounds(0, 0, Game.width, Game.height);
		viewport.setCamera(new OrthographicCamera());
		//TODO this is needed for the moment as Spritebatch switched to using VAOs in libGDX v1.13.1
		//  This results in HARD crashes atm, whereas old vertex arrays work fine
		SpriteBatch.overrideVertexType = Mesh.VertexDataType.VertexArray;
		stage = new Stage(viewport);
		registerInputProcessor();

		container = new Container<TextField>();
		stage.addActor(container);
		container.setTransform(true);

		skin = new Skin(FileUtils.getFileHandle(Files.FileType.Internal, "gdx/textfield.json"));

		TextField.TextFieldStyle style = skin.get(TextField.TextFieldStyle.class);
		style.font = Game.platform.getFont(size, "", false, false);
		style.background = null;
		textField = multiline ? new FocusedTextArea(style) : new FocusedTextField(style);
		textField.setProgrammaticChangeEvents(true);
		textField.setFocusTraversal(false);

		if (!multiline) textField.setAlignment(Align.center);

		textField.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				BitmapFont f = Game.platform.getFont(size, textField.getText(), false, false);
				TextField.TextFieldStyle style = textField.getStyle();
				if (f != style.font){
					style.font = f;
					textField.setStyle(style);
				}
				onChanged();
			}
		});

		if (!multiline){
			textField.setTextFieldListener(new TextField.TextFieldListener(){
				public void keyTyped (TextField textField, char c){
					if (c == '\r' || c == '\n'){
						enterPressed();
					}
				}

			});
		}

		textField.setOnscreenKeyboard(new TextField.OnscreenKeyboard() {
			@Override
			public void show(boolean visible) {
				Game.platform.setOnscreenKeyboardVisible(visible, multiline);
			}
		});
		container.setActor(textField);
	}

	private class FocusedTextArea extends TextArea {

		private FocusedTextArea(TextField.TextFieldStyle style) {
			super("", style);
		}

		@Override
		public void cut() {
			super.cut();
			onClipBoardUpdate();
		}

		@Override
		public void copy() {
			super.copy();
			onClipBoardUpdate();
		}

		@Override
		protected InputListener createInputListener() {
			return new FocusedTextAreaClickListener();
		}

		private class FocusedTextAreaClickListener extends TextFieldClickListener {

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				PointerEvent.clearKeyboardThisPress = false;
				focus();
				return super.touchDown(event, x, y, pointer, button);
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				PointerEvent.clearKeyboardThisPress = false;
				focus();
				super.touchUp(event, x, y, pointer, button);
			}

			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Input.Keys.TAB) {
					tabPressed(tabBackwardsPressed());
					return true;
				}
				return super.keyDown(event, keycode);
			}
		}
	}

	private class FocusedTextField extends TextField {

		private FocusedTextField(TextField.TextFieldStyle style) {
			super("", style);
		}

		@Override
		public void cut() {
			super.cut();
			onClipBoardUpdate();
		}

		@Override
		public void copy() {
			super.copy();
			onClipBoardUpdate();
		}

		@Override
		protected InputListener createInputListener() {
			return new FocusedTextFieldClickListener();
		}

		private class FocusedTextFieldClickListener extends TextFieldClickListener {

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				PointerEvent.clearKeyboardThisPress = false;
				focus();
				return super.touchDown(event, x, y, pointer, button);
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				PointerEvent.clearKeyboardThisPress = false;
				focus();
				super.touchUp(event, x, y, pointer, button);
			}

			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Input.Keys.TAB) {
					tabPressed(tabBackwardsPressed());
					return true;
				}
				return super.keyDown(event, keycode);
			}
		}
	}

	public void enterPressed(){
		//fires any time enter is pressed, do nothing by default
	};

	public void tabPressed(boolean backwards){
		//fires any time tab is pressed in a single-line input, do nothing by default
	}

	public void onChanged(){
		//fires any time the text box is changed, do nothing by default
	}

	public void onClipBoardUpdate(){
		//fires any time the clipboard is updated via cut or copy, do nothing by default
	}

	public void setText(String text){
		textField.setText(text);
		textField.setCursorPosition(textField.getText().length());
	}

	public void setMaxLength(int maxLength){
		textField.setMaxLength(maxLength);
	}

	public void setPasswordMask(char passwordCharacter){
		textField.setPasswordCharacter(passwordCharacter);
		textField.setPasswordMode(true);
	}

	public String getText(){
		return textField.getText();
	}

	public void copyToClipboard(){
		if (textField.getSelection().isEmpty()) {
			textField.selectAll();
		}

		textField.copy();
	}

	public void pasteFromClipboard(){
		String contents = Gdx.app.getClipboard().getContents();
		if (contents == null) return;

		if (!textField.getSelection().isEmpty()){
			//just use cut, but override clipboard
			textField.cut();
			Gdx.app.getClipboard().setContents(contents);
		}

		String existing = textField.getText();
		int cursorIdx = textField.getCursorPosition();

		textField.setText(existing.substring(0, cursorIdx) + contents + existing.substring(cursorIdx));
		textField.setCursorPosition(cursorIdx + contents.length());
	}

	public void focus(){
		if (stage != null && textField != null) {
			if (activeInput != this && activeInput != null) {
				activeInput.deactivate(false);
			}
			activeInput = this;
			registerInputProcessor();
			stage.setKeyboardFocus(textField);
			Game.platform.setOnscreenKeyboardVisible(true, multiline);
		}
	}

	private boolean tabBackwardsPressed() {
		return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
				|| Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
	}

	private void registerInputProcessor() {
		if (stage == null) {
			return;
		}
		if (!inputProcessorRegistered) {
			Game.inputHandler.addInputProcessor(stage);
			inputProcessorRegistered = true;
		}
	}

	private void unregisterInputProcessor() {
		if (stage != null && inputProcessorRegistered) {
			Game.inputHandler.removeInputProcessor(stage);
		}
		inputProcessorRegistered = false;
	}

	private void deactivate(boolean hideKeyboard) {
		if (stage != null) {
			stage.setKeyboardFocus(null);
		}
		if (hideKeyboard) {
			Game.platform.setOnscreenKeyboardVisible(false, false);
		}
	}

	@Override
	protected void layout() {
		super.layout();

		float contX = x;
		float contY = y;
		float contW = width;
		float contH = height;

		if (bg != null){
			bg.x = x;
			bg.y = y;
			bg.size(width, height);

			contX += bg.marginLeft();
			contY += bg.marginTop();
			contW -= bg.marginHor();
			contH -= bg.marginVer();
		}

		float zoom = Camera.main.zoom;
		Camera c = camera();
		if (c != null){
			zoom = c.zoom;
			Point p = c.cameraToScreen(contX, contY);
			contX = p.x/zoom;
			contY = p.y/zoom;
		}

		container.align(Align.topLeft);
		container.setPosition(contX*zoom, (Game.height-(contY*zoom)));
		container.size(contW*zoom, contH*zoom);
	}

	@Override
	public void update() {
		super.update();
		stage.act(Game.elapsed);
	}

	@Override
	public void draw() {
		super.draw();
		Quad.releaseIndices();
		Script.unuse();
		Texture.clear();
		stage.draw();
		Quad.bindIndices();
		Blending.useDefault();
	}

	@Override
	public synchronized void destroy() {
		super.destroy();
		if (stage != null) {
			boolean wasActive = activeInput == this;
			if (wasActive) {
				deactivate(true);
				activeInput = null;
			}
			unregisterInputProcessor();
			if (DeviceCompat.isWeb()) {
				stage = null;
				skin = null;
				return;
			}
			stage.dispose();
			skin.dispose();
			stage = null;
			skin = null;
			if (!DeviceCompat.isDesktop()) Game.platform.updateSystemUI();
		}
	}
}
