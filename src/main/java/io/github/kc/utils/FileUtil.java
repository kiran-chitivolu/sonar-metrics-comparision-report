package io.github.kc.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class FileUtil {
    public static String readFileContent(String strPath) throws Exception {
        String strLine = "";
        String strContent = "";
        BufferedReader br = new BufferedReader(new FileReader(strPath));
        while ((strLine = br.readLine()) != null) {
            strContent += strLine + "\n";
        }
        br.close();
        return strContent;
    }
    
    public static String readFileContentFromClassPath(String fileName) throws Exception {
        String strLine = "";
        String strContent = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(FileUtil.class.getClassLoader().getResourceAsStream(fileName)));
        while ((strLine = br.readLine()) != null) {
            strContent += strLine + "\n";
        }
        br.close();
        return strContent;
    }

    public static void writeContentToFile(String strPath, String fileContent) throws Exception {
        File newFile = new File(strPath);
        // if path doesn't exist create it
        new File(Paths.get(strPath).getParent().toString()).mkdirs();
        // if file doesn't exists, then create it
        if (!newFile.exists()) {
            newFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(newFile.getAbsoluteFile());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(fileContent);
        bufferedWriter.close();
        bufferedWriter = null;
        fileWriter = null;
        newFile = null;
    }

    public static ArrayList<String> listFiles(String directoryName, String fileExtension) {
        ArrayList<String> fileList = new ArrayList<String>();
        File directory = new File(directoryName);
        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile() && file.getName().contains(fileExtension)) {
                fileList.add(file.getAbsolutePath());
            }
        }
        return fileList;
    }

    public static void copyAndReplace(String source, String target) throws Exception {
        Path from = Paths.get(source); // convert from File to Path
        Path to = Paths.get(target); // convert from String to Path
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
    }
}
