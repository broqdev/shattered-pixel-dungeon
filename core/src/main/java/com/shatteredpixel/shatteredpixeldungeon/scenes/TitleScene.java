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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.BannerSprites;
import com.shatteredpixel.shatteredpixeldungeon.effects.Fireball;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.multiplayer.WebMultiplayer;
import com.shatteredpixel.shatteredpixeldungeon.services.news.News;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.AvailableUpdateData;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.Updates;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TitleBackground;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMultiplayerLobby;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMultiplayerRoom;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSettings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndVictoryCongrats;
import com.watabou.glwrap.Blending;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.ColorMath;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.RectF;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class TitleScene extends PixelScene {

	private static final Logger LOG = Logger.getLogger(TitleScene.class.getName());

	private Image title;
	private Fireball leftFB;
	private Fireball rightFB;
	private Image signs;

	private StyledButton btnPlay;
	private StyledButton btnMultiplayer;
	private StyledButton btnSupport;
	private StyledButton btnRankings;
	private StyledButton btnJournal;
	private StyledButton btnNews;
	private StyledButton btnChanges;
	private StyledButton btnSettings;
	private StyledButton btnAbout;
	private StyledButton btnExportData;
	private StyledButton btnImportData;
	private boolean browserDataBackupBusy;

	private BitmapText version;
	private IconButton btnFade;
	private ExitButton btnExit;

	enum MultiplayerRoomMenuAction {
		CREATE,
		JOIN,
		REJOIN
	}

	@Override
	public void create() {
		
		super.create();

		if (WebMultiplayer.resumeWatcherIfRequested() || WebMultiplayer.resumeCloneIfRequested()) {
			return;
		}

		Music.INSTANCE.playTracks(
				new String[]{Assets.Music.THEME_1, Assets.Music.THEME_2},
				new float[]{1, 1},
				false);

		uiCamera.visible = false;
		
		int w = Camera.main.width;
		int h = Camera.main.height;

		RectF insets = getCommonInsets();

		TitleBackground BG = new TitleBackground( w, h );
		add( BG );

		w -= insets.left + insets.right;
		h -= insets.top + insets.bottom;

		title = BannerSprites.get( landscape() ? BannerSprites.Type.TITLE_LAND : BannerSprites.Type.TITLE_PORT);
		add( title );

		float topRegion = Math.max(title.height - 6, h*0.45f);

		title.x = insets.left + (w - title.width()) / 2f;
		title.y = insets.top + 2 + (topRegion - title.height()) / 2f;

		align(title);

		if (landscape()){
			leftFB = placeTorch(title.x + 30, title.y + 35);
			rightFB = placeTorch(title.x + title.width - 30, title.y + 35);
		} else {
			leftFB = placeTorch(title.x + 16, title.y + 70);
			rightFB = placeTorch(title.x + title.width - 16, title.y + 70);
		}

		signs = new Image(BannerSprites.get( landscape() ? BannerSprites.Type.TITLE_GLOW_LAND : BannerSprites.Type.TITLE_GLOW_PORT)){
			private float time = 0;
			@Override
			public void update() {
				super.update();
				am = Math.max(0f, (float)Math.sin( time += Game.elapsed ));
				am = Math.min(am, title.am);
				if (time >= 1.5f*Math.PI) time = 0;
			}
			@Override
			public void draw() {
				Blending.setLightMode();
				super.draw();
				Blending.setNormalMode();
			}
		};
		signs.x = title.x + (title.width() - signs.width())/2f;
		signs.y = title.y;
		add( signs );

		final Chrome.Type GREY_TR = Chrome.Type.GREY_BUTTON_TR;
		
		btnPlay = new StyledButton(GREY_TR, Messages.get(this, "enter")){
			@Override
			protected void onClick() {
				ArrayList<GamesInProgress.Info> games = GamesInProgress.checkAll();
				webParityLog("title enter games=" + games.size()
						+ " route=" + (games.isEmpty() ? "heroSelect" : "startScene"));
				if (games.size() == 0){
					GamesInProgress.selectedClass = null;
					GamesInProgress.curSlot = 1;
					ShatteredPixelDungeon.switchScene(HeroSelectScene.class);
				} else {
					ShatteredPixelDungeon.switchNoFade( StartScene.class );
				}
			}
			
			@Override
			protected boolean onLongClick() {
				//making it easier to start runs quickly while debugging
				if (DeviceCompat.isDebug()) {
					GamesInProgress.selectedClass = null;
					GamesInProgress.curSlot = 1;
					ShatteredPixelDungeon.switchScene(HeroSelectScene.class);
					return true;
				}
				return super.onLongClick();
			}
		};
		btnPlay.icon(Icons.get(Icons.ENTER));
		add(btnPlay);

		if (multiplayerRoomEntryAvailable(Game.platform)) {
			btnMultiplayer = new StyledButton(GREY_TR, Messages.get(this, "multiplayer")){
				@Override
				protected void onClick() {
					showMultiplayerMenu();
				}
			};
			btnMultiplayer.icon(Icons.get(Icons.CHALLENGE_COLOR));
			add(btnMultiplayer);
		}

		btnSupport = new SupportButton(GREY_TR, Messages.get(this, "support"));
		add(btnSupport);

		btnRankings = new StyledButton(GREY_TR,Messages.get(this, "rankings")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchNoFade( RankingsScene.class );
			}
		};
		btnRankings.icon(Icons.get(Icons.RANKINGS));
		add(btnRankings);
		Dungeon.daily = Dungeon.dailyReplay = false;

		btnJournal = new StyledButton(GREY_TR, Messages.get(this, "journal")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchNoFade( JournalScene.class );
			}
		};
		btnJournal.icon(Icons.get(Icons.JOURNAL));
		add(btnJournal);

		btnNews = new NewsButton(GREY_TR, Messages.get(this, "news"));
		btnNews.icon(Icons.get(Icons.NEWS));
		add(btnNews);

		btnChanges = new ChangesButton(GREY_TR, Messages.get(this, "changes"));
		btnChanges.icon(Icons.get(Icons.CHANGES));
		add(btnChanges);

		btnSettings = new SettingsButton(GREY_TR, Messages.get(this, "settings"));
		add(btnSettings);

		btnAbout = new StyledButton(GREY_TR, Messages.get(this, "about")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchScene( AboutScene.class );
			}
		};
		btnAbout.icon(Icons.get(Icons.SHPX));
		add(btnAbout);

		boolean browserDataBackupAvailable = Game.platform != null
				&& Game.platform.browserDataBackup().isAvailable();
		if (browserDataBackupAvailable) {
			btnExportData = new StyledButton(GREY_TR, Messages.get(this, "browser_data_export")){
				@Override
				protected void onClick() {
					exportBrowserDataBackup();
				}
			};
			btnExportData.icon(Icons.get(Icons.COPY));
			add(btnExportData);

			btnImportData = new StyledButton(GREY_TR, Messages.get(this, "browser_data_import")){
				@Override
				protected void onClick() {
					confirmBrowserDataImport();
				}
			};
			btnImportData.icon(Icons.get(Icons.PASTE));
			add(btnImportData);
		}
		
		final int BTN_HEIGHT = 20;
		ArrayList<StyledButton> titleButtons = new ArrayList<>();
		titleButtons.add(btnPlay);
		if (btnMultiplayer != null) {
			titleButtons.add(btnMultiplayer);
		}
		titleButtons.add(btnSupport);
		titleButtons.add(btnRankings);
		titleButtons.add(btnJournal);
		titleButtons.add(btnNews);
		titleButtons.add(btnChanges);
		titleButtons.add(btnSettings);
		titleButtons.add(btnAbout);
		if (browserDataBackupAvailable) {
			titleButtons.add(btnExportData);
			titleButtons.add(btnImportData);
		}

		int rowCountForGap = titleButtonRowCount(titleButtons.size());
		int gapDivisor = landscape() ? 3 : 5;
		if (browserDataBackupAvailable) {
			gapDivisor = landscape() ? 4 : 7;
		}
		int GAP = (int)(h - topRegion - rowCountForGap*BTN_HEIGHT)/3;
		GAP /= gapDivisor;
		GAP = Math.max(GAP, 2);

		float buttonAreaWidth = landscape() ? PixelScene.MIN_WIDTH_L-6 : PixelScene.MIN_WIDTH_P-2;
		float btnAreaLeft = insets.left + (w - buttonAreaWidth) / 2f;
		layoutTitleButtons(titleButtons, btnAreaLeft, insets.top + topRegion+GAP,
				buttonAreaWidth, BTN_HEIGHT, GAP);
		logTitleButtonBounds("play", btnPlay);
		logTitleButtonBounds("multiplayer", btnMultiplayer);

		version = new BitmapText( "v" + Game.version, pixelFont);
		version.measure();
		version.hardlight( 0x888888 );
		version.x = insets.left + w - version.width() - (DeviceCompat.isDesktop() ? 4 : 8);
		version.y = insets.top + h - version.height() - (DeviceCompat.isDesktop() ? 2 : 4);
		add( version );

		btnFade = new IconButton(Icons.CHEVRON.get()){
			@Override
			protected void onClick() {
				enable(false);
				parent.add(new Tweener(parent, 0.5f) {
					@Override
					protected void updateValues(float progress) {
						if (!btnFade.active) {
							uiAlpha = 1 - progress;
							updateFade();
						}
					}
				});
			}
		};
		btnFade.icon().originToCenter();
		btnFade.icon().angle = 180f;
		btnFade.setRect(btnAreaLeft + (buttonAreaWidth-16)/2, camera.main.height - 16 - insets.bottom, 16, 16);
		add(btnFade);

		PointerArea fadeResetter = new PointerArea(0, 0, Camera.main.width, Camera.main.height){
			@Override
			public boolean onSignal(PointerEvent event) {
				if (event != null && event.type == PointerEvent.Type.UP && !btnPlay.active){
					parent.add(new Tweener(parent, 0.5f) {
						@Override
						protected void updateValues(float progress) {
							uiAlpha = progress;
							updateFade();
							if (progress >= 1){
								btnFade.enable(true);
							}
						}
					});
				}
				return false;
			}
		};
		add(fadeResetter);

		if (DeviceCompat.isDesktop()) {
			btnExit = new ExitButton();
			btnExit.setPos( w - btnExit.width(), 0 );
			add( btnExit );
		}

		Badges.loadGlobal();
		if (Badges.isUnlocked(Badges.Badge.VICTORY) && !SPDSettings.victoryNagged()
				&& !WebMultiplayer.suppressSupportPrompts()) {
			SPDSettings.victoryNagged(true);
			add(new WndVictoryCongrats());
		}

		fadeIn();
	}

	private float uiAlpha;

	public void updateFade() {
		float alpha = GameMath.gate(0f, uiAlpha, 1f);

		title.am = alpha;
		leftFB.am = alpha;
		rightFB.am = alpha;
		//signs.am = alpha; handles this itself

		btnPlay.enable(alpha != 0);
		if (btnMultiplayer != null) btnMultiplayer.enable(alpha != 0);
		btnSupport.enable(alpha != 0);
		btnRankings.enable(alpha != 0);
		btnJournal.enable(alpha != 0);
		btnNews.enable(alpha != 0);
		btnChanges.enable(alpha != 0);
		btnSettings.enable(alpha != 0);
		btnAbout.enable(alpha != 0);
		if (btnExportData != null) btnExportData.enable(alpha != 0 && !browserDataBackupBusy);
		if (btnImportData != null) btnImportData.enable(alpha != 0 && !browserDataBackupBusy);

		btnPlay.alpha(alpha);
		if (btnMultiplayer != null) btnMultiplayer.alpha(alpha);
		btnSupport.alpha(alpha);
		btnRankings.alpha(alpha);
		btnJournal.alpha(alpha);
		btnNews.alpha(alpha);
		btnChanges.alpha(alpha);
		btnSettings.alpha(alpha);
		btnAbout.alpha(alpha);
		if (btnExportData != null) btnExportData.alpha(alpha);
		if (btnImportData != null) btnImportData.alpha(alpha);

		version.alpha(alpha);
		btnFade.icon().alpha(alpha);
		if (btnExit != null){
			btnExit.enable(alpha != 0);
			btnExit.icon().alpha(alpha);
		}

	}

	private int titleButtonRowCount(int buttonCount) {
		if (landscape()) {
			return 1 + (int)Math.ceil(Math.max(0, buttonCount - 2) / 3f);
		} else {
			int fullRows = Math.min(buttonCount, btnMultiplayer == null ? 2 : 3);
			return fullRows + (int)Math.ceil(Math.max(0, buttonCount - fullRows) / 2f);
		}
	}

	private void layoutTitleButtons(ArrayList<StyledButton> buttons, float left, float top,
			float width, int height, int gap) {
		if (buttons.isEmpty()) {
			return;
		}
		if (landscape()) {
			int index = 0;
			float y = top;
			float halfWidth = (width/2)-1;
			for (int column = 0; column < 2 && index < buttons.size(); column++, index++) {
				StyledButton button = buttons.get(index);
				button.setRect(left + column * (halfWidth + 2), y, halfWidth, height);
				align(button);
			}
			y += height + gap;
			float thirdWidth = (float)(Math.floor(width/3f)-1);
			while (index < buttons.size()) {
				for (int column = 0; column < 3 && index < buttons.size(); column++, index++) {
					StyledButton button = buttons.get(index);
					button.setRect(left + column * (thirdWidth + 2), y, thirdWidth, height);
					align(button);
				}
				y += height + gap;
			}
		} else {
			int index = 0;
			float y = top;
			int fullRows = Math.min(buttons.size(), btnMultiplayer == null ? 2 : 3);
			for (; index < fullRows; index++) {
				StyledButton button = buttons.get(index);
				button.setRect(left, y, width, height);
				align(button);
				y += height + gap;
			}
			float halfWidth = (width/2)-1;
			while (index < buttons.size()) {
				for (int column = 0; column < 2 && index < buttons.size(); column++, index++) {
					StyledButton button = buttons.get(index);
					button.setRect(left + column * (halfWidth + 2), y, halfWidth, height);
					align(button);
				}
				y += height + gap;
			}
		}
	}

	private void showMultiplayerMenu() {
		if (!multiplayerRoomEntryAvailable(Game.platform)) {
			ShatteredPixelDungeon.scene().addToFront(
					new WndMessage(Messages.get(TitleScene.class, "multiplayer_unavailable")));
			return;
		}
		webParityLog("title multiplayer menu opened");
		boolean rejoinAvailable = Game.platform != null && Game.platform.multiplayerRoomRejoinAvailable();
		ShatteredPixelDungeon.scene().addToFront(new WndMultiplayerMenu(Game.platform, rejoinAvailable));
	}

	static boolean multiplayerRoomEntryAvailable(PlatformSupport platform) {
		return shouldShowMultiplayerEntry(platform != null,
				platform != null && platform.multiplayerRoomEntryAvailable());
	}

	static boolean shouldShowMultiplayerEntry(boolean platformAvailable, boolean roomEntryAvailable) {
		return platformAvailable && roomEntryAvailable;
	}

	static WndMultiplayerRoom.Mode multiplayerRoomModeForMenuIndex(int index) {
		MultiplayerRoomMenuAction action = multiplayerRoomActionForMenuIndex(index, false);
		if (action == MultiplayerRoomMenuAction.CREATE) {
			return WndMultiplayerRoom.Mode.CREATE;
		}
		if (action == MultiplayerRoomMenuAction.JOIN) {
			return WndMultiplayerRoom.Mode.JOIN;
		}
		return null;
	}

	static MultiplayerRoomMenuAction multiplayerRoomActionForMenuIndex(int index, boolean rejoinAvailable) {
		if (rejoinAvailable) {
			if (index == 0) {
				return MultiplayerRoomMenuAction.REJOIN;
			}
			if (index == 1) {
				return MultiplayerRoomMenuAction.CREATE;
			}
			if (index == 2) {
				return MultiplayerRoomMenuAction.JOIN;
			}
			return null;
		}
		if (index == 0) {
			return MultiplayerRoomMenuAction.CREATE;
		}
		if (index == 1) {
			return MultiplayerRoomMenuAction.JOIN;
		}
		return null;
	}

	private static String[] multiplayerMenuOptions(boolean rejoinAvailable) {
		if (rejoinAvailable) {
			return new String[]{
					Messages.get(TitleScene.class, "multiplayer_rejoin"),
					Messages.get(TitleScene.class, "multiplayer_create"),
					Messages.get(TitleScene.class, "multiplayer_join")
			};
		}
		return new String[]{
				Messages.get(TitleScene.class, "multiplayer_create"),
				Messages.get(TitleScene.class, "multiplayer_join")
		};
	}

	private void exportBrowserDataBackup() {
		if (browserDataBackupBusy || Game.platform == null) {
			return;
		}
		webParityLog("title browser data export click");
		browserDataBackupBusy = true;
		updateFade();
		Game.platform.browserDataBackup().exportData(this::completeBrowserDataBackup);
	}

	private void confirmBrowserDataImport() {
		if (browserDataBackupBusy || Game.platform == null) {
			return;
		}
		webParityLog("title browser data import confirm opened");
		ShatteredPixelDungeon.scene().addToFront(new WndOptions(
				Icons.get(Icons.WARNING),
				Messages.get(this, "browser_data_import_title"),
				Messages.get(this, "browser_data_import_confirm"),
				Messages.get(this, "browser_data_import_yes"),
				Messages.get(this, "browser_data_import_no")
		) {
			@Override
			protected void onSelect(int index) {
				if (index == 0) {
					importBrowserDataBackup();
				}
			}
		});
	}

	private void importBrowserDataBackup() {
		if (browserDataBackupBusy || Game.platform == null) {
			return;
		}
		webParityLog("title browser data import confirmed");
		browserDataBackupBusy = true;
		updateFade();
		Game.platform.browserDataBackup().importData(this::completeBrowserDataBackup);
	}

	private void completeBrowserDataBackup(PlatformSupport.BrowserDataBackupResult result) {
		browserDataBackupBusy = false;
		updateFade();
		webParityLog("title browser data result success=" + result.success
				+ " localStorageKeys=" + result.localStorageKeys
				+ " indexedDbRecords=" + result.indexedDbRecords
				+ " bytes=" + result.bytes);

		if (result.message != null && !result.message.isEmpty()
				&& ShatteredPixelDungeon.scene() instanceof TitleScene) {
			ShatteredPixelDungeon.scene().addToFront(new WndMessage(browserDataBackupMessage(result)));
		}
	}

	private String browserDataBackupMessage(PlatformSupport.BrowserDataBackupResult result) {
		if (result.localStorageKeys == 0 && result.indexedDbRecords == 0 && result.bytes == 0) {
			return result.message;
		}
		return Messages.get(this, "browser_data_result_counts", result.message,
				result.localStorageKeys, result.indexedDbRecords, result.bytes);
	}

	private Fireball placeTorch(float x, float y ) {
		Fireball fb = new Fireball();
		fb.x = x - fb.width()/2f;
		fb.y = y - fb.height();

		align(fb);
		add( fb );
		return fb;
	}

	private static class NewsButton extends StyledButton {

		public NewsButton(Chrome.Type type, String label ){
			super(type, label);
			if (SPDSettings.news()) News.checkForNews();
		}

		int unreadCount = -1;

		@Override
		public void update() {
			super.update();

			if (unreadCount == -1 && News.articlesAvailable()){
				long lastRead = SPDSettings.newsLastRead();
				if (lastRead == 0){
					if (News.articles().get(0) != null) {
						SPDSettings.newsLastRead(News.articles().get(0).date.getTime());
					}
				} else {
					unreadCount = News.unreadArticles(new Date(SPDSettings.newsLastRead()));
					if (unreadCount > 0) {
						unreadCount = Math.min(unreadCount, 9);
						text(text() + "(" + unreadCount + ")");
					}
				}
			}

			if (unreadCount > 0){
				textColor(ColorMath.interpolate( 0xFFFFFF, Window.SHPX_COLOR, 0.5f + (float)Math.sin(Game.timeTotal*5)/2f));
			}
		}

		@Override
		protected void onClick() {
			super.onClick();
			ShatteredPixelDungeon.switchNoFade( NewsScene.class );
		}
	}

	private static class ChangesButton extends StyledButton {

		public ChangesButton( Chrome.Type type, String label ){
			super(type, label);
			if (SPDSettings.updates()) Updates.checkForUpdate();
		}

		boolean updateShown = false;

		@Override
		public void update() {
			super.update();

			if (!updateShown && Updates.updateAvailable()){
				updateShown = true;
				text(Messages.get(TitleScene.class, "update"));
			}

			if (updateShown){
				textColor(ColorMath.interpolate( 0xFFFFFF, Window.SHPX_COLOR, 0.5f + (float)Math.sin(Game.timeTotal*5)/2f));
			}
		}

		@Override
		protected void onClick() {
			if (Updates.updateAvailable()){
				AvailableUpdateData update = Updates.updateData();

				ShatteredPixelDungeon.scene().addToFront( new WndOptions(
						Icons.get(Icons.CHANGES),
						update.versionName == null ? Messages.get(this,"title") : Messages.get(this,"versioned_title", update.versionName),
						update.desc == null ? Messages.get(this,"desc") : update.desc,
						Messages.get(this,"update"),
						Messages.get(this,"changes")
				) {
					@Override
					protected void onSelect(int index) {
						if (index == 0) {
							Updates.launchUpdate(Updates.updateData());
						} else if (index == 1){
							ChangesScene.changesSelected = 0;
							ShatteredPixelDungeon.switchNoFade( ChangesScene.class );
						}
					}
				});

			} else {
				ChangesScene.changesSelected = 0;
				ShatteredPixelDungeon.switchNoFade( ChangesScene.class );
			}
		}

	}

	private static class WndMultiplayerMenu extends WndOptions {

		private final PlatformSupport platform;
		private final boolean rejoinAvailable;
		private boolean pendingRejoin;
		private boolean closed;

		WndMultiplayerMenu(PlatformSupport platform, boolean rejoinAvailable) {
			super(
					Messages.get(TitleScene.class, "multiplayer_menu_title"),
					Messages.get(TitleScene.class, "multiplayer_menu_body"),
					multiplayerMenuOptions(rejoinAvailable));
			this.platform = platform;
			this.rejoinAvailable = rejoinAvailable;
		}

		@Override
		protected boolean hideOnSelect(int index) {
			return multiplayerRoomActionForMenuIndex(index, rejoinAvailable)
					!= MultiplayerRoomMenuAction.REJOIN;
		}

		@Override
		protected void onSelect(int index) {
			if (pendingRejoin) {
				return;
			}
			MultiplayerRoomMenuAction action = multiplayerRoomActionForMenuIndex(index, rejoinAvailable);
			webParityLog("title multiplayer menu selected index=" + index
					+ " action=" + (action == null ? "" : action.name()));
			if (action == MultiplayerRoomMenuAction.CREATE || action == MultiplayerRoomMenuAction.JOIN) {
				WndMultiplayerRoom.Mode mode = action == MultiplayerRoomMenuAction.CREATE
						? WndMultiplayerRoom.Mode.CREATE
						: WndMultiplayerRoom.Mode.JOIN;
				ShatteredPixelDungeon.scene().addToFront(new WndMultiplayerRoom(mode));
			} else if (action == MultiplayerRoomMenuAction.REJOIN) {
				rejoin();
			}
		}

		private void rejoin() {
			webParityLog("title multiplayer menu rejoin submit");
			if (platform == null || !platform.requestMultiplayerRoomRejoin()) {
				hideRejoinOption();
				setError(Messages.get(WndMultiplayerRoom.class, "rejoin_unavailable"));
				return;
			}
			pendingRejoin = true;
			setOptionsEnabled(false);
			setStatus(Messages.get(WndMultiplayerRoom.class, "rejoining"));
		}

		private void setOptionsEnabled(boolean enabled) {
			for (int i = 0; i < optionButtons.size(); i++) {
				if (!isRejoinIndex(i) || optionButtons.get(i).visible) {
					optionButtons.get(i).enable(enabled);
				}
			}
		}

		private boolean isRejoinIndex(int index) {
			return multiplayerRoomActionForMenuIndex(index, rejoinAvailable)
					== MultiplayerRoomMenuAction.REJOIN;
		}

		private void hideRejoinOption() {
			for (int i = 0; i < optionButtons.size(); i++) {
				if (isRejoinIndex(i)) {
					optionButtons.get(i).visible = false;
					optionButtons.get(i).active = false;
					optionButtons.get(i).enable(false);
					return;
				}
			}
		}

		private void setStatus(String message) {
			setMessage(message, Window.SHPX_COLOR);
		}

		private void setError(String message) {
			setMessage(message, 0xFF8E75);
		}

		private void setMessage(String message, int color) {
			if (messageText == null) {
				return;
			}
			int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
			messageText.text(message, width);
			messageText.setPos(0, messageText.top());
			messageText.hardlight(color);
		}

		@Override
		public void update() {
			super.update();
			if (!pendingRejoin || platform == null) {
				return;
			}
			String event;
			while (!closed && (event = platform.pollMultiplayerRoomEvent()) != null) {
				handleRoomEvent(event);
			}
		}

		private void handleRoomEvent(String event) {
			String[] parts = event.split("\\|", -1);
			if (parts.length == 0) {
				return;
			}
			webParityLog("title multiplayer menu event " + parts[0]);
			if ("room-status".equals(parts[0]) && parts.length >= 2) {
				setStatus(WndMultiplayerLobby.statusMessage(parts[1]));
			} else if ("room-error".equals(parts[0]) && parts.length >= 2) {
				pendingRejoin = false;
				setOptionsEnabled(true);
				hideRejoinOption();
				setError(WndMultiplayerLobby.statusMessage(parts[1]));
			} else if ("room-lobby".equals(parts[0])) {
				WndMultiplayerLobby lobby = new WndMultiplayerLobby(WndMultiplayerLobby.LobbyState.fromEvent(parts));
				hide();
				ShatteredPixelDungeon.scene().addToFront(lobby);
			} else if ("room-launch".equals(parts[0])) {
				WebMultiplayer.RoomLaunch launch = WebMultiplayer.RoomLaunch.fromEvent(parts);
				if (!launch.valid) {
					pendingRejoin = false;
					setOptionsEnabled(true);
					hideRejoinOption();
					setError(Messages.get(WndMultiplayerLobby.class, "launch_invalid"));
				} else if (WebMultiplayer.launchRoomRun(launch)) {
					hide();
				} else if (launch.watcher) {
					setStatus(Messages.get(WndMultiplayerLobby.class, "launch_waiting"));
				} else {
					pendingRejoin = false;
					setOptionsEnabled(true);
					hideRejoinOption();
					setError(Messages.get(WndMultiplayerLobby.class, "launch_unavailable"));
				}
			}
		}

		@Override
		public void hide() {
			closed = true;
			super.hide();
		}
	}

	private static class SettingsButton extends StyledButton {

		public SettingsButton( Chrome.Type type, String label ){
			super(type, label);
			if (Messages.lang().status() == Languages.Status.X_UNFINISH){
				icon(Icons.get(Icons.LANGS));
				icon.hardlight(1.5f, 0, 0);
			} else {
				icon(Icons.get(Icons.PREFS));
			}
		}

		@Override
		public void update() {
			super.update();

			if (Messages.lang().status() == Languages.Status.X_UNFINISH){
				textColor(ColorMath.interpolate( 0xFFFFFF, CharSprite.NEGATIVE, 0.5f + (float)Math.sin(Game.timeTotal*5)/2f));
			}
		}

		@Override
		protected void onClick() {
			if (Messages.lang().status() == Languages.Status.X_UNFINISH){
				WndSettings.last_index = 5;
			}
			ShatteredPixelDungeon.scene().add(new WndSettings());
		}
	}

	private static class SupportButton extends StyledButton{

		public SupportButton( Chrome.Type type, String label ){
			super(type, label);
			icon(Icons.get(Icons.GOLD));
			textColor(Window.TITLE_COLOR);
		}

		@Override
		protected void onClick() {
			if (WebMultiplayer.suppressSupportPrompts()) {
				return;
			}
			ShatteredPixelDungeon.switchNoFade(SupporterScene.class);
		}
	}

	private static void webParityLog(String message) {
		if (DeviceCompat.webParityLoggingEnabled()) {
			LOG.info("[WEB-PARITY] " + message);
		}
	}

	private static void logTitleButtonBounds(String name, StyledButton button) {
		if (DeviceCompat.webParityLoggingEnabled() && button != null) {
			webParityLog("title " + name + " button bounds x=" + button.left()
					+ " y=" + button.top()
					+ " width=" + button.width()
					+ " height=" + button.height()
					+ " centerX=" + button.centerX()
					+ " centerY=" + button.centerY());
		}
	}
}
