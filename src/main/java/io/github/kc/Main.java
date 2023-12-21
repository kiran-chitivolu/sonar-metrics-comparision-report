package io.github.kc;

import io.github.kc.codequality.SonarMetricsProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception {
        Properties configs = new Properties();
        configs.load(new FileInputStream(System.getProperty("user.dir") + "/configs/config.properties"));
        configs.setProperty("parentDirectoryPath",new File(System.getProperty("user.dir")).getAbsolutePath());
        SonarMetricsProcessor sonarMetricsProcessor = new SonarMetricsProcessor(configs);
        sonarMetricsProcessor.getSonarMetricsAndWriteToCSVFile();
        sonarMetricsProcessor.buildSonarHTMLReport();
    }
}