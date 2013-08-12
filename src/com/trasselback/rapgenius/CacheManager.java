package com.trasselback.rapgenius;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;

public class CacheManager {

	public static void saveData(Context context, String name, String lyrics) {
		// Only save if lyrics found.
		if (!lyrics.contains("Lyrics not found."))
			try {
				File file = new File(context.getCacheDir(), name + ".cache");
				FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(lyrics);
				bufferedWriter.close();
				fileWriter.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
	}

	public static String getCache(Context context, String name) {
		String cachedData = "";
		try {
			File file = new File(context.getCacheDir(), name + ".cache");
			FileReader fileReader = new FileReader(file.getAbsoluteFile());
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = "";
			StringBuilder stringBuilder = new StringBuilder();
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
			bufferedReader.close();
			fileReader.close();
			cachedData = stringBuilder.toString();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return cachedData;
	}

	public static void deleteCache(Context context) {
		for (File file : context.getCacheDir().listFiles())
			file.delete();
	}

	public static double getCacheSize(Context context) {
		double size = 0;
		for (File file : context.getCacheDir().listFiles())
			size += file.length();
		return size / (1024 * 1024);
	}
}
