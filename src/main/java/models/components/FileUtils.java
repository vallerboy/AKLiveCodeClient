package models.components;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Created by Lenovo on 23.07.2017.
 */
public class FileUtils {
    public static String readAllFile(String path){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (String s : Files.readAllLines(new File(path).toPath())) {
                stringBuilder.append(s).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static void saveFile(String  path, byte[] bytes){
        try {
            Files.write(new File(path).toPath(), bytes, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
