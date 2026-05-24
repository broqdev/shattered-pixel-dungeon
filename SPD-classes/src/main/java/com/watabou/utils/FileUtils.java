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

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Base64;
import java.util.logging.Logger;

public class FileUtils {

	private static final Logger LOG = Logger.getLogger(FileUtils.class.getName());
	private static final String WEB_SAVE_MIRROR_PREFS = "web-save-mirror";
	private static final String WEB_SAVE_MIRROR_INDEX = "__index";
	private static final String WEB_SAVE_MIRROR_FILE_PREFIX = "file.";
	
	// Helper methods for setting/using a default base path and file address mode
	
	private static Files.FileType defaultFileType = null;
	private static String defaultPath = "";
	
	public static void setDefaultFileProperties( Files.FileType type, String path ){
		defaultFileType = type;
		defaultPath = path;
	}
	
	public static FileHandle getFileHandle( String name ){
		return getFileHandle( defaultFileType, defaultPath, name );
	}
	
	public static FileHandle getFileHandle( Files.FileType type, String name ){
		return getFileHandle( type, "", name );
	}
	
	public static FileHandle getFileHandle( Files.FileType type, String basePath, String name ){
		switch (type){
			case Classpath:
				return Gdx.files.classpath( basePath + name );
			case Internal:
				return Gdx.files.internal( basePath + name );
			case External:
				return Gdx.files.external( basePath + name );
			case Absolute:
				return Gdx.files.absolute( basePath + name );
			case Local:
				return Gdx.files.local( basePath + name );
			default:
				return null;
		}
	}
	
	// Files

	//looks to see if there is any evidence of interrupted saving
	public static boolean cleanTempFiles(){
		return cleanTempFiles("");
	}

	public static boolean cleanTempFiles( String dirName ){
		FileHandle dir = getFileHandle(dirName);
		boolean foundTemp = false;
		for (FileHandle file : dir.list()){
			if (file.isDirectory()){
				foundTemp = cleanTempFiles(dirName + file.name()) || foundTemp;
			} else if (file.length() == 0) {
				file.delete();
			} else {
				if (file.name().endsWith(".spdtmp")){
					FileHandle temp = file;
					FileHandle original = getFileHandle( defaultFileType, "", temp.path().replace(".spdtmp", "") );

					//replace the base file with the temp one if base is invalid or temp is valid and newer
					try {
						bundleFromStream(temp.read());

						try {
							bundleFromStream(original.read());

							if (temp.lastModified() > original.lastModified()) {
								temp.moveTo(original);
							} else {
								temp.delete();
							}

						} catch (Exception e) {
							temp.moveTo(original);
						}

					} catch (Exception e) {
						temp.delete();
					}

					foundTemp = true;
				}
			}
		}
		return foundTemp;
	}
	
	public static boolean fileExists( String name ){
		FileHandle file = getFileHandle( name );
		return file.exists() && !file.isDirectory() && file.length() > 0;
	}

	//returns length of a file in bytes, or 0 if file does not exist
	public static long fileLength( String name ){
		if (shouldMirrorWebSave(name)) {
			byte[] mirrored = getMirroredWebSave(name);
			if (mirrored != null) {
				return mirrored.length;
			}
		}
		FileHandle file = getFileHandle( name );
		if (!file.exists() || file.isDirectory()){
			return 0;
		} else {
			return file.length();
		}
	}
	
	public static boolean deleteFile( String name ){
		removeMirroredWebSave(name);
		return getFileHandle( name ).delete();
	}

	//replaces a file with junk data, for as many bytes as given
	//This is helpful as some cloud sync systems do not persist deleted, empty, or zeroed files
	public static void overwriteFile( String name, int bytes ){
		byte[] data = new byte[bytes];
		Arrays.fill(data, (byte)1);
		getFileHandle( name ).writeBytes(data, false);
		mirrorWebSave(name, data);
	}
	
	// Directories
	
	public static boolean dirExists( String name ){
		FileHandle dir = getFileHandle( name );
		return dir.exists() && dir.isDirectory() || webSaveMirrorContainsDir(name);
	}
	
	public static boolean deleteDir( String name ){
		removeMirroredWebSaveDir(name);
		FileHandle dir = getFileHandle( name );
		
		if (dir == null || !dir.isDirectory()){
			return false;
		} else {
			return dir.deleteDirectory();
		}
	}

	public static ArrayList<String> filesInDir( String name ){
		FileHandle dir = getFileHandle( name );
		ArrayList result = new ArrayList();
		if (dir != null && dir.isDirectory()){
			for (FileHandle file : dir.list()){
				result.add(file.name());
			}
		}
		return result;
	}
	
	// bundle reading
	
	//only works for base path
	public static Bundle bundleFromFile( String fileName ) throws IOException{
		byte[] mirrored = getMirroredWebSave(fileName);
		if (mirrored != null) {
			try {
				return bundleFromStream(new ByteArrayInputStream(mirrored));
			} catch (IOException e) {
				webParityLog("web save mirror read failed file=" + fileName
						+ " bytes=" + mirrored.length
						+ " error=" + e.getClass().getName());
				throw e;
			}
		}

		try {
			FileHandle file = getFileHandle( fileName );
			if (!file.exists() || file.isDirectory() || file.length() == 0) {
				throw new IOException("file does not exist!");
			}
			return bundleFromStream(file.read());
		} catch (GdxRuntimeException e){
			//game classes expect an IO exception, so wrap the GDX exception in that
			throw new IOException(e);
		}
	}
	
	private static Bundle bundleFromStream( InputStream input ) throws IOException{
		Bundle bundle = Bundle.read( input );
		input.close();
		return bundle;
	}
	
	// bundle writing
	
	//only works for base path
	public static void bundleToFile( String fileName, Bundle bundle ) throws IOException{
		try {
			FileHandle file = getFileHandle(fileName);

			if (Gdx.app != null && DeviceCompat.isWeb()) {
				// Web local files already update in-memory state atomically on close, while
				// IndexedDB persistence happens asynchronously. Avoid expanding one save
				// into temp/delete/rename transactions that can commit independently.
				byte[] bytes = bundleToBytes(bundle);
				OutputStream output = file.write(false);
				output.write(bytes);
				output.close();
				mirrorWebSave(fileName, bytes);
				return;
			}

			//write to a temp file, then move the files.
			// This helps prevent save corruption if writing is interrupted
			if (file.exists()){
				FileHandle temp = getFileHandle(fileName + ".spdtmp");
				bundleToStream(temp.write(false), bundle);
				file.delete();
				temp.moveTo(file);
			} else {
				bundleToStream(file.write(false), bundle);
			}

		} catch (GdxRuntimeException e){
			//game classes expect an IO exception, so wrap the GDX exception in that
			throw new IOException(e);
		}
	}
	
	private static void bundleToStream( OutputStream output, Bundle bundle ) throws IOException{
		Bundle.write( bundle, output );
		output.close();
	}

	private static byte[] bundleToBytes(Bundle bundle) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		if (!Bundle.write(bundle, output)) {
			throw new IOException("bundle write failed");
		}
		output.close();
		return output.toByteArray();
	}

	private static boolean shouldMirrorWebSave(String fileName) {
		if (Gdx.app == null || !DeviceCompat.isWeb() || fileName == null) {
			return false;
		}
		int slash = fileName.indexOf('/');
		if (!fileName.startsWith("game") || slash < 5 || !fileName.endsWith(".dat")) {
			return false;
		}
		String leaf = fileName.substring(slash + 1);
		return leaf.equals("game.dat") || (leaf.startsWith("depth") && leaf.endsWith(".dat"));
	}

	private static Preferences webSaveMirrorPrefs() {
		return Gdx.app == null ? null : Gdx.app.getPreferences(WEB_SAVE_MIRROR_PREFS);
	}

	private static String webSaveMirrorKey(String fileName) {
		String encoded = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(fileName.getBytes(StandardCharsets.UTF_8));
		return WEB_SAVE_MIRROR_FILE_PREFIX + encoded;
	}

	private static void mirrorWebSave(String fileName, byte[] bytes) {
		if (!shouldMirrorWebSave(fileName)) {
			return;
		}
		Preferences prefs = webSaveMirrorPrefs();
		if (prefs == null) {
			return;
		}
		prefs.putString(webSaveMirrorKey(fileName), Base64.getEncoder().encodeToString(bytes));
		Set<String> index = webSaveMirrorIndex(prefs);
		index.add(fileName);
		prefs.putString(WEB_SAVE_MIRROR_INDEX, joinIndex(index));
		prefs.flush();
		webParityLog("web save mirror wrote file=" + fileName + " bytes=" + bytes.length);
	}

	private static byte[] getMirroredWebSave(String fileName) {
		if (!shouldMirrorWebSave(fileName)) {
			return null;
		}
		Preferences prefs = webSaveMirrorPrefs();
		if (prefs == null) {
			return null;
		}
		String value = prefs.getString(webSaveMirrorKey(fileName), null);
		if (value == null || value.isEmpty()) {
			return null;
		}
		try {
			return Base64.getDecoder().decode(value);
		} catch (IllegalArgumentException e) {
			removeMirroredWebSave(fileName);
			webParityLog("web save mirror decode failed file=" + fileName);
			return null;
		}
	}

	private static void removeMirroredWebSave(String fileName) {
		if (!shouldMirrorWebSave(fileName)) {
			return;
		}
		Preferences prefs = webSaveMirrorPrefs();
		if (prefs == null) {
			return;
		}
		prefs.remove(webSaveMirrorKey(fileName));
		Set<String> index = webSaveMirrorIndex(prefs);
		if (index.remove(fileName)) {
			prefs.putString(WEB_SAVE_MIRROR_INDEX, joinIndex(index));
		}
		prefs.flush();
	}

	private static void removeMirroredWebSaveDir(String dirName) {
		if (Gdx.app == null || !DeviceCompat.isWeb()) {
			return;
		}
		Preferences prefs = webSaveMirrorPrefs();
		if (prefs == null) {
			return;
		}
		String prefix = dirName.endsWith("/") ? dirName : dirName + "/";
		Set<String> index = webSaveMirrorIndex(prefs);
		boolean changed = false;
		for (String fileName : new ArrayList<>(index)) {
			if (fileName.startsWith(prefix)) {
				prefs.remove(webSaveMirrorKey(fileName));
				index.remove(fileName);
				changed = true;
			}
		}
		if (changed) {
			prefs.putString(WEB_SAVE_MIRROR_INDEX, joinIndex(index));
			prefs.flush();
		}
	}

	private static boolean webSaveMirrorContainsDir(String dirName) {
		if (Gdx.app == null || !DeviceCompat.isWeb()) {
			return false;
		}
		Preferences prefs = webSaveMirrorPrefs();
		if (prefs == null) {
			return false;
		}
		String prefix = dirName.endsWith("/") ? dirName : dirName + "/";
		for (String fileName : webSaveMirrorIndex(prefs)) {
			if (fileName.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	private static Set<String> webSaveMirrorIndex(Preferences prefs) {
		LinkedHashSet<String> index = new LinkedHashSet<>();
		String raw = prefs.getString(WEB_SAVE_MIRROR_INDEX, "");
		if (raw == null || raw.isEmpty()) {
			return index;
		}
		for (String fileName : raw.split("\n")) {
			if (!fileName.isEmpty()) {
				index.add(fileName);
			}
		}
		return index;
	}

	private static String joinIndex(Set<String> index) {
		StringBuilder joined = new StringBuilder();
		for (String fileName : index) {
			if (joined.length() > 0) joined.append('\n');
			joined.append(fileName);
		}
		return joined.toString();
	}

	private static void webParityLog(String message) {
		if (DeviceCompat.webParityLoggingEnabled()) {
			LOG.info("[WEB-PARITY] " + message);
		}
	}

}
