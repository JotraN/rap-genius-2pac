package com.trasselback.rapgenius.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import android.content.Context;
import android.widget.Toast;

public class FavoritesManager {

	public static String getFavorites(Context context) {
		String favs = "";
		try {
			File file = new File(context.getFilesDir(), "favorites");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = "";
			StringBuilder stringBuilder = new StringBuilder();
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
			bufferedReader.close();
			fileReader.close();
			favs = stringBuilder.toString();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return favs;
	}

	public static boolean checkFavorites(Context context, String favoritedSong) {
		String currFavs = getFavorites(context);
		String song = favoritedSong.replace("-", " ");
		return currFavs.contains(song.toUpperCase(Locale.ENGLISH));
	}

	public static void manageFavorite(Context context, String favoritedSong) {
		// Get current favorites if available.
		String currFavs = getFavorites(context);
		// Cleans up name
		String song = favoritedSong.replace("-", " ").toUpperCase(
				Locale.ENGLISH);

		// Remove if the song was already added
		if (currFavs.contains(song)) {
			removeFavorites(context, song, currFavs);
		// Add the song to favorites file
		} else if (currFavs.length() <= 1650) {
			String favorites = currFavs + song + "<BR>";
			try {
				File file = new File(context.getFilesDir(), "favorites");
				FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(favorites);
				bufferedWriter.close();
				fileWriter.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			CharSequence text = "Song added to favorites list.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		} else {
			CharSequence text = "Can't add, favorites list full.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	public static void removeFavorites(Context context, String favoritedSong,
			String currFavs) {
		try {
			File file = new File(context.getFilesDir(), "favorites");
			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			currFavs = currFavs.replace(
					favoritedSong.toUpperCase(Locale.ENGLISH) + "<BR>", "");
			bufferedWriter.write(currFavs);
			bufferedWriter.close();
			fileWriter.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		CharSequence text = "Song removed from favorites list.";
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
}