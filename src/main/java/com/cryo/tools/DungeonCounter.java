package com.cryo.tools;

import com.cryo.CryoDung;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class DungeonCounter {

    private static int entered;
    private static int finished;

    public static void loadStats() {
        Properties prop;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("counter.txt"))) {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null)
                builder.append(line);
            prop = new Gson().fromJson(builder.toString(), Properties.class);
            entered = Integer.parseInt(prop.getProperty("entered"));
            finished = Integer.parseInt(prop.getProperty("finished"));
            CryoDung.INSTANCE.getController().setDungeonsEntered(entered);
            CryoDung.INSTANCE.getController().setDungeonsFinished(finished);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveStats() {
        Properties prop = new Properties();
        prop.put("entered", entered);
        prop.put("finished", finished);
        String line = new Gson().toJson(prop);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("counter.txt"))) {
            writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void entered() {
        entered++;
        saveStats();
        CryoDung.INSTANCE.getController().setDungeonsEntered(entered);
    }

    public static void finished() {
        finished++;
        saveStats();
        CryoDung.INSTANCE.getController().setDungeonsFinished(finished);
    }

    public static int getEntered() {
        return entered;
    }

    public static int getFinished() {
        return finished;
    }
}
