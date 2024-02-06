package io.github.kc.codequality;

import io.github.kc.api.consume.APIConsumer;
import io.github.kc.utils.EmailUtil;
import io.github.kc.utils.FileUtil;
import io.github.kc.utils.JSONUtil;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SonarMetricsProcessor {
    final static Logger logger = Logger.getLogger("SonarMetricsProcessor.class");
    Properties configs;
    String parentDirectoryPath;

    public SonarMetricsProcessor(Properties configs){
        this.configs = configs;
        this.parentDirectoryPath = configs.getProperty("parentDirectoryPath");
    }
    public void getSonarMetricsAndWriteToCSVFile() throws Exception {
        Map<String,Map<String,String>> components = getComponents(ApplicationConstants.DEBUGMODE);
        String currentMonthMetrics = "";
        for (String key: components.keySet()) {
            try {
                String sonarKey = components.get(key).get("sonar.key");
                String branchName = components.get(key).get("branch.name");
                Map<String,String> sonarMetrics = getSonarMetrics(sonarKey,branchName);
                String minor = sonarMetrics.get("minor");
                String major = sonarMetrics.get("major");
                String blocker = sonarMetrics.get("blocker");
                String critical = sonarMetrics.get("critical");
                String coverage = sonarMetrics.get("coverage");
                String techDebt = sonarMetrics.get("techDebt");
                String total = String.valueOf(Integer.parseInt(major) + Integer.parseInt(critical) + Integer.parseInt(blocker) + Integer.parseInt(minor));
                String currentComponentMetrics = key + "," + blocker + "," + critical + "," + major + "," + total + "," + coverage + "," + techDebt;
                currentMonthMetrics = currentMonthMetrics == "" ? currentComponentMetrics : currentMonthMetrics + System.lineSeparator() + currentComponentMetrics;
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("Exception while collecting metrics for component # " + key);
            }
        }
        LocalDate currentdate = LocalDate.now();
        currentdate = currentdate.minusMonths(0);
        FileUtil.writeContentToFile(parentDirectoryPath + ApplicationConstants.SONARMETRICSPATH + currentdate.getMonth() + "-" + currentdate.getYear() + ".csv", currentMonthMetrics );
    }





    String getSonarMetric(String sonarMetricsJSON, String metric){
        String returnValue = "0";
        JSONUtil jsonUtil = new JSONUtil();
        try{
            returnValue = jsonUtil.getJSONValue(sonarMetricsJSON, "$..measures[?(@.metric =='" + metric + "')].value");
            if(returnValue == null || returnValue == ""){
                returnValue = "0";
            }
        } catch(Exception e){
            //TO-DO
        }
        return returnValue;
    }

    public void buildSonarHTMLReport() throws Exception {
        LocalDate currentdate = LocalDate.now();
        currentdate = currentdate.minusMonths(0);
        Map<String, Map<String,String>> components =  getComponents(ApplicationConstants.DEBUGMODE);
        Map<String,Map<String, Double>> currentPeriodSonarMetrics = getSonarMetricsFromCSVFile(ApplicationConstants.SONARMETRICSPATH + currentdate.getMonth() + "-" + currentdate.getYear() + ".csv");
        currentdate = currentdate.minusMonths(1);
        Map<String,Map<String, Double>> previousPeriodSonarMetrics = getSonarMetricsFromCSVFile(ApplicationConstants.SONARMETRICSPATH + currentdate.getMonth() + "-" + currentdate.getYear() + ".csv");
        currentdate = currentdate.plusMonths(1);
        Map<String, String> sonarHTMLRows = new HashMap<>();
        for (String key: components.keySet()) {
            try {
                Map<String, String> blocker = getMetricsComparisonObject(currentPeriodSonarMetrics.get(key).get(ApplicationConstants.BLOCKER).intValue(), previousPeriodSonarMetrics.get(key).get(ApplicationConstants.BLOCKER).intValue(), ApplicationConstants.DECREASE, 0, false);
                Map<String, String> critical = getMetricsComparisonObject(currentPeriodSonarMetrics.get(key).get(ApplicationConstants.CRITICAL).intValue(), previousPeriodSonarMetrics.get(key).get(ApplicationConstants.CRITICAL).intValue(), ApplicationConstants.DECREASE, 0, false);
                Map<String, String> major = getMetricsComparisonObject(currentPeriodSonarMetrics.get(key).get(ApplicationConstants.MAJOR).intValue(), previousPeriodSonarMetrics.get(key).get(ApplicationConstants.MAJOR).intValue(), ApplicationConstants.DECREASE, 0, false);
                Map<String, String> total = getMetricsComparisonObject(currentPeriodSonarMetrics.get(key).get(ApplicationConstants.SONAR_TOTAL).intValue(), previousPeriodSonarMetrics.get(key).get(ApplicationConstants.SONAR_TOTAL).intValue(), ApplicationConstants.DECREASE, "", false);
                Map<String, String> techDebt = getMetricsComparisonObject(currentPeriodSonarMetrics.get(key).get(ApplicationConstants.TECHDEBT), previousPeriodSonarMetrics.get(key).get(ApplicationConstants.TECHDEBT), ApplicationConstants.DECREASE, 5.0, false);
                Map<String, String> codeCoverage = getMetricsComparisonObject(currentPeriodSonarMetrics.get(key).get(ApplicationConstants.CODECOVERAGE), previousPeriodSonarMetrics.get(key).get(ApplicationConstants.CODECOVERAGE), ApplicationConstants.INCREASE, 80.0, false);

                String htmlRow = getRowString(key, blocker, critical, major, total, codeCoverage, techDebt);
                addRowsIntoCorrespondingHead(sonarHTMLRows, "PRIMARY", htmlRow);
            } catch(Exception e){
                System.out.println("Exception while building report for component # " + key);
            }
        }
        String reportHTML = getResourceFileAsString("CodeQualityReport.html");
        for(String key : sonarHTMLRows.keySet()){
            reportHTML = reportHTML.replace(key,sonarHTMLRows.get(key));
        }
        reportHTML = reportHTML.replace("##DATE##", currentdate.getMonth() + "-" + currentdate.getYear());

        FileUtil.writeContentToFile(parentDirectoryPath + ApplicationConstants.SONARMETRICSPATH + currentdate.getMonth() + "-" + currentdate.getYear() + ".html",reportHTML);
        if(configs.getProperty("email_distribute_report").equals("yes")) {
            EmailUtil emailUtil = new EmailUtil(configs.getProperty("email_host"),
                    Integer.parseInt(configs.getProperty("email_port")),
                    configs.getProperty("email_user"),
                    configs.getProperty("email_password"));
            emailUtil.sendReportEmailTo(reportHTML, configs.getProperty("email_dl"), "", "Code Quality Metrics # " + currentdate.getMonth() + "-" + currentdate.getYear());
        }
    }

    private void printMap(Map<String,Map<String, Double>> map){
        for(String key : map.keySet()){
            System.out.println(key);
        }
    }

    public String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return "";
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    private void addRowsIntoCorrespondingHead(Map<String, String> sonarHTMLRows, String rowKey, String htmlRow){
        if(sonarHTMLRows.containsKey(ApplicationConstants.DOUBLEHASH + rowKey + ApplicationConstants.DOUBLEHASH)){
            sonarHTMLRows.replace(ApplicationConstants.DOUBLEHASH + rowKey + ApplicationConstants.DOUBLEHASH,sonarHTMLRows.get(ApplicationConstants.DOUBLEHASH + rowKey + ApplicationConstants.DOUBLEHASH) + System.lineSeparator() + htmlRow);
        } else {
            sonarHTMLRows.put(ApplicationConstants.DOUBLEHASH + rowKey + ApplicationConstants.DOUBLEHASH, htmlRow);
        }
    }

    private String getRowString(String appName,Map<String,String> blocker, Map<String,String> critical, Map<String,String> major, Map<String,String> total, Map<String,String> coverage,
                                Map<String,String> techDebt){
        String newRow = "<tr>\n" +
                "                    <td colspan=\"1\"> " + appName + " </td>\n" +
                "                    <td class=\"" + blocker.get( "class") + "\" colspan=\"1\">" + blocker.get("content") + "</td>\n" +
                "                    <td class=\"" + critical.get( "class") + "\" colspan=\"1\">" + critical.get("content") + "</td>\n" +
                "                    <td class=\"" + major.get( "class") + "\" colspan=\"1\">" + major.get("content") + "</td>\n" +
                "                    <td class=\"" + total.get( "class") + "\" colspan=\"1\">" + total.get("content") + "</td>\n" +
                "                    <td class=\"" + coverage.get( "class") + "\" colspan=\"1\">" + coverage.get("content") + "</td>\n" +
                "                    <td class=\"" + techDebt.get( "class") + "\" colspan=\"1\">" + techDebt.get("content") + "</td>\n" +
                "                </tr>";
        return newRow;
    }



    private Map<String,Map<String, Double>> getSonarMetricsFromCSVFile(String csvFilePath) throws Exception {
        String csvFileContent = FileUtil.readFileContent(parentDirectoryPath + csvFilePath);
        Map<String,Map<String, Double>> sonarMetricsMap =  new TreeMap<>();
        List<String> sonarMetricsAsList = Arrays.asList(csvFileContent.split("\n"));
        for(String sonarMetrics : sonarMetricsAsList){
            String[] componentSonarMetricsArray = sonarMetrics.split(",");
            Map<String,Double> componentSonarMetrics = new HashMap<>();
            componentSonarMetrics.put(ApplicationConstants.BLOCKER,Double.parseDouble(componentSonarMetricsArray[1]));
            componentSonarMetrics.put(ApplicationConstants.CRITICAL,Double.parseDouble(componentSonarMetricsArray[2]));
            componentSonarMetrics.put(ApplicationConstants.MAJOR,Double.parseDouble(componentSonarMetricsArray[3]));
            componentSonarMetrics.put(ApplicationConstants.SONAR_TOTAL,Double.parseDouble(componentSonarMetricsArray[4]));
            componentSonarMetrics.put(ApplicationConstants.CODECOVERAGE,Double.parseDouble(componentSonarMetricsArray[5]));
            componentSonarMetrics.put(ApplicationConstants.TECHDEBT,Double.parseDouble(componentSonarMetricsArray[6]));
            sonarMetricsMap.put(componentSonarMetricsArray[0],componentSonarMetrics);
        }
        return sonarMetricsMap;
    }


    private Map<String, Map<String,String>> getComponents(String forWhat) throws Exception {
        Map<String, Map<String,String>> components = new TreeMap<>();
        List<String> componentsFileLines = Arrays.asList(FileUtil.readFileContent(parentDirectoryPath + "/configs/components.csv").split("\n"));
        for(String componentsFileLine : componentsFileLines) {
            if(! (componentsFileLine.startsWith ("COMPONENT_NAME") || componentsFileLine.equals(""))){
                Map<String, String> attributes = addAttributesToComponent(componentsFileLine.split(",")[1],componentsFileLine.split(",")[2]);
                components.put(componentsFileLine.split(",")[0],attributes);
            }
        }
        return components;
    }

    private Map<String,String> addAttributesToComponent(String sonarKey, String branchName){
        Map<String, String> attributes = new HashMap<>();
        attributes.put("sonar.key",sonarKey);
        attributes.put("branch.name",branchName);
        return attributes;
    }




    private boolean isGreaterThanEqual(Object currentValue, Object previousValue){
        if(previousValue.toString() == ""){
            return true;
        }
        if(currentValue instanceof Integer) {
            return (Integer) currentValue >= (Integer) previousValue;
        } else {
            return (Double) currentValue >= (Double) previousValue;
        }
    }

    private boolean isLessThanEqual(Object currentValue, Object previousValue){
        if(previousValue.toString() == ""){
            return true;
        }
        if(currentValue instanceof Integer) {
            return (Integer) currentValue <= (Integer) previousValue;
        } else {
            return (Double) currentValue <= (Double) previousValue;
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private Map<String, String> getMetricsComparisonObject(Double value){
        Map<String, String> comparisonObject = new HashMap<>();
        if(value.toString().equals("1.0")){
            comparisonObject.put("content","PASS");
            comparisonObject.put("class", "green");
        } else{
            comparisonObject.put("content","FAIL");
            comparisonObject.put("class", "red");
        }
        return comparisonObject;
    }

    private Map<String,String> getMetricsComparisonObject(Object currentValue, Object previousValue, String comparisonType, Object baselineValue, boolean nonZeroValue){
        Map<String, String> comparisonObject = new HashMap<>();
        if(currentValue instanceof Integer) {
            comparisonObject.put("content",currentValue + "(" + String.valueOf((Integer) currentValue - (Integer) previousValue) + ")");
        } else {
            comparisonObject.put("content",currentValue + "(" + String.valueOf(round((Double) currentValue - (Double) previousValue,2) + ")"));
        }
        if(comparisonType.equals(ApplicationConstants.INCREASE)){
            if(isGreaterThanEqual(currentValue, previousValue) && isGreaterThanEqual(currentValue, baselineValue) ) {
                comparisonObject.put("class", "green");
            } else {
                comparisonObject.put("class", "red");
            }
        } else {
            if(isLessThanEqual(currentValue, previousValue) && isLessThanEqual(currentValue, baselineValue)) {
                comparisonObject.put("class", "green");
            } else {
                comparisonObject.put("class", "red");
            }
        }
        if(nonZeroValue && ((Integer) currentValue == 0)){
            comparisonObject.put("class", "red");
        }
        return comparisonObject;
    }

    private Map<String,String> getSonarMetrics(String componentKey, String branchName) throws Exception {
        Map<String,String> sonarMetrics = new HashMap<>();
        Map<String, String> basicHeaders = new HashMap<>();
        basicHeaders.put("accept", "application/json, text/plain");
        basicHeaders.put("Authorization","Basic " + Base64.getEncoder().encodeToString((configs.getProperty("sonar_user") + ":" + configs.getProperty("sonar_password")).getBytes()));
        APIConsumer apiConsumer = new APIConsumer();
        apiConsumer.setBaseEndPoint(configs.getProperty("sonar_url") + "/api/measures/component?component=" + componentKey);
        try {
            apiConsumer.consumeHTTPGet("Test", "&branch=" + branchName + "&metricKeys=blocker_violations,critical_violations,major_violations,minor_violations,coverage,sqale_index", basicHeaders);
            String sonarMetricsJSON =  apiConsumer.getResponseString("Test");
            sonarMetrics.put("minor", getSonarMetric(sonarMetricsJSON, "minor_violations"));
            sonarMetrics.put("major" , getSonarMetric(sonarMetricsJSON, "major_violations"));
            sonarMetrics.put("blocker" , getSonarMetric(sonarMetricsJSON, "blocker_violations"));
            sonarMetrics.put("critical" , getSonarMetric(sonarMetricsJSON, "critical_violations"));
            sonarMetrics.put("coverage" , getSonarMetric(sonarMetricsJSON, "coverage"));
            sonarMetrics.put("techDebt" , String.valueOf(Integer.parseInt(getSonarMetric(sonarMetricsJSON, "sqale_index")) / (8 * 60)));
            return sonarMetrics;
        } catch(Exception e){
            e.printStackTrace();
            return sonarMetrics;
        }
    }

}
