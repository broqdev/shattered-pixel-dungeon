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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Logger;

public class GamesInProgress {

	private static final Logger LOG = Logger.getLogger(GamesInProgress.class.getName());
	
	public static final int MAX_SLOTS = HeroClass.values().length;
	
	//null means we have loaded info and it is empty, no entry means unknown.
	private static HashMap<Integer, Info> slotStates = new HashMap<>();
	public static int curSlot;
	
	public static HeroClass selectedClass;
	public static boolean randomizedClass = false;
	
	private static final String GAME_FOLDER = "game%d";
	private static final String GAME_FILE	= "game.dat";
	private static final String DEPTH_FILE	= "depth%d.dat";
	private static final String DEPTH_BRANCH_FILE	= "depth%d-branch%d.dat";
	
	public static boolean gameExists( int slot ){
		boolean dirExists = FileUtils.dirExists(gameFolder(slot));
		long gameFileLength = FileUtils.fileLength(gameFile(slot));
		boolean exists = dirExists && gameFileLength > 1;
		webParityLog("gameExists slot=" + slot
				+ " exists=" + exists
				+ " dir=" + gameFolder(slot)
				+ " dirExists=" + dirExists
				+ " gameFile=" + gameFile(slot)
				+ " fileLength=" + gameFileLength);
		return exists;
	}
	
	public static String gameFolder( int slot ){
		return String.format(Locale.ENGLISH, GAME_FOLDER, slot);
	}
	
	public static String gameFile( int slot ){
		return gameFolder(slot) + "/" + GAME_FILE;
	}
	
	public static String depthFile( int slot, int depth, int branch ) {
		if (branch == 0) {
			return gameFolder(slot) + "/" + String.format(Locale.ENGLISH, DEPTH_FILE, depth);
		} else {
			return gameFolder(slot) + "/" + String.format(Locale.ENGLISH, DEPTH_BRANCH_FILE, depth, branch);
		}
	}
	
	public static int firstEmpty(){
		for (int i = 1; i <= MAX_SLOTS; i++){
			if (check(i) == null) return i;
		}
		return -1;
	}
	
	public static ArrayList<Info> checkAll(){
		webParityLog("checkAll begin");
		ArrayList<Info> result = new ArrayList<>();
		for (int i = 1; i <= MAX_SLOTS; i++){
			Info curr = check(i);
			if (curr != null) result.add(curr);
		}
		switch (SPDSettings.gamesInProgressSort()){
			case "level": default:
				Collections.sort(result, levelComparator);
				break;
			case "last_played":
				Collections.sort(result, lastPlayedComparator);
				break;
		}

		webParityLog("checkAll complete count=" + result.size() + " slots=" + slotSummary(result));
		return result;
	}
	
	public static Info check( int slot ) {
		
		if (slotStates.containsKey( slot )) {
			
			Info cached = slotStates.get( slot );
			webParityLog("check slot=" + slot + " cached=" + infoSummary(cached));
			return cached;
			
		} else if (!gameExists( slot )) {
			
			slotStates.put(slot, null);
			webParityLog("check slot=" + slot + " missing");
			return null;
			
		} else {
			
			Info info;
			try {
				
				Bundle bundle = FileUtils.bundleFromFile(gameFile(slot));

				if (bundle.getInt( "version" ) < ShatteredPixelDungeon.v2_5_4) {
					info = null;
				} else {

					info = new Info();
					info.slot = slot;
					Dungeon.preview(info, bundle);
				}

			} catch (IOException e) {
				webParityLog("check slot=" + slot + " readFailed=" + e.getClass().getName());
				info = null;
			} catch (Exception e){
				ShatteredPixelDungeon.reportException( e );
				webParityLog("check slot=" + slot + " previewFailed=" + e.getClass().getName());
				info = null;
			}
			
			slotStates.put( slot, info );
			webParityLog("check slot=" + slot + " loaded=" + infoSummary(info));
			return info;
			
		}
	}

	public static void set(int slot) {
		Info info = new Info();
		info.slot = slot;

		info.lastPlayed = Dungeon.lastPlayed;
		
		info.depth = Dungeon.depth;
		info.challenges = Dungeon.challenges;

		info.seed = Dungeon.seed;
		info.customSeed = Dungeon.customSeedText;
		info.daily = Dungeon.daily;
		info.dailyReplay = Dungeon.dailyReplay;
		
		info.level = Dungeon.hero.lvl;
		info.str = Dungeon.hero.STR;
		info.strBonus = Dungeon.hero.STR() - Dungeon.hero.STR;
		info.exp = Dungeon.hero.exp;
		info.hp = Dungeon.hero.HP;
		info.ht = Dungeon.hero.HT;
		info.shld = Dungeon.hero.shielding();
		info.heroClass = Dungeon.hero.heroClass;
		info.subClass = Dungeon.hero.subClass;
		info.armorTier = Dungeon.hero.tier();
		
		info.goldCollected = Statistics.goldCollected;
		info.maxDepth = Statistics.deepestFloor;

		slotStates.put( slot, info );
		webParityLog("set slot=" + slot + " " + infoSummary(info));
	}
	
	public static void setUnknown( int slot ) {
		slotStates.remove( slot );
		webParityLog("setUnknown slot=" + slot);
	}
	
	public static void delete( int slot ) {
		slotStates.put( slot, null );
		webParityLog("delete slot=" + slot);
	}

	private static void webParityLog(String message) {
		if (DeviceCompat.webParityLoggingEnabled()) {
			LOG.info("[WEB-PARITY] " + message);
		}
	}

	private static String slotSummary(ArrayList<Info> infos) {
		StringBuilder summary = new StringBuilder();
		for (Info info : infos) {
			if (summary.length() > 0) summary.append(',');
			summary.append(info.slot);
		}
		return summary.toString();
	}

	private static String infoSummary(Info info) {
		if (info == null) {
			return "null";
		}
		return "slot=" + info.slot
				+ " depth=" + info.depth
				+ " level=" + info.level
				+ " heroClass=" + info.heroClass
				+ " lastPlayed=" + info.lastPlayed;
	}
	
	public static class Info {
		public int slot;

		public int depth;
		public int version;
		public int challenges;

		public long seed;
		public String customSeed;
		public boolean daily;
		public boolean dailyReplay;
		public long lastPlayed;

		public int level;
		public int str;
		public int strBonus;
		public int exp;
		public int hp;
		public int ht;
		public int shld;
		public HeroClass heroClass;
		public HeroSubClass subClass;
		public int armorTier;
		
		public int goldCollected;
		public int maxDepth;
	}
	
	public static final Comparator<GamesInProgress.Info> levelComparator = new Comparator<GamesInProgress.Info>() {
		@Override
		public int compare(GamesInProgress.Info lhs, GamesInProgress.Info rhs ) {
			if (rhs.level != lhs.level){
				return (int)Math.signum( rhs.level - lhs.level );
			} else {
				return lastPlayedComparator.compare(lhs, rhs);
			}
		}
	};

	public static final Comparator<GamesInProgress.Info> lastPlayedComparator = new Comparator<GamesInProgress.Info>() {
		@Override
		public int compare(GamesInProgress.Info lhs, GamesInProgress.Info rhs ) {
			return (int)Math.signum( rhs.lastPlayed - lhs.lastPlayed );
		}
	};
}
