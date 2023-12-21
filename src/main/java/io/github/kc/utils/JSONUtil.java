package io.github.kc.utils;

import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Utility class for JSON Manipulation
 */
public class JSONUtil {
    /**
     * Updates given json File/String and returns updated jsonString, returns Json in String format without any modifications if path is incorrect/json is wrong/key is not found
     *
     * @param jsonPath
     * @param valueTobeUpdated
     * @return
     */
    public String updateJSON(String jsonFileOrString, String jsonPath, String valueTobeUpdated) {
        try {
            return JsonPath.parse(createJsonString(jsonFileOrString)).set(jsonPath, valueTobeUpdated).jsonString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Fetches Json value for the given file path/json string, returns null if path is incorrect/json is wrong/key is not found
     *
     * @param jsonFileOrString
     * @param jsonPath
     * @return
     */
    public String getJSONValue(String jsonFileOrString, String jsonPath) {
        String returnValue = "null";
        boolean isLengthRequested = false;
        try {
            if(jsonPath.contains(".length()")){
                jsonPath = jsonPath.replace(".length()","");
                isLengthRequested = true;
            }
            Object jsonPathEvaluationObject = JsonPath.read(createJsonString(jsonFileOrString),jsonPath);
            returnValue = isLengthRequested ? getJsonPathEvaluationObjectLength(jsonPathEvaluationObject) : getJsonPathEvaluationObjectStringValue(jsonPathEvaluationObject);
        } catch (Exception e) {
            returnValue = isLengthRequested ? "0" : "null";
        }
        return returnValue;
    }
    public boolean isJsonArray(String jsonFileOrString, String jsonPath){
        boolean returnValue = false;
        if(jsonPath.contains(".length()")){
            jsonPath = jsonPath.replace(".length()","");

        }
        try{
            Object jsonPathEvaluationObject = JsonPath.read(createJsonString(jsonFileOrString),jsonPath);
            JsonParser jsonParser = new JsonParser();
            return  jsonParser.parse(jsonPathEvaluationObject.toString()).isJsonArray();
        } catch (Exception e){
            return false;
        }
    }

    private String createJsonString(String jsonFileOrString) {
        try {
            return new File(jsonFileOrString).isFile() ? FileUtil.readFileContent(jsonFileOrString) : jsonFileOrString;
        } catch (Exception e) {
            return null;
        }
    }

    private String getJsonPathEvaluationObjectLength(Object jsonPathEvaluationObject){
        String returnValue = "0";
        if(jsonPathEvaluationObject instanceof ArrayList){
            returnValue = String.valueOf(((List)jsonPathEvaluationObject).size());
        } else {
            returnValue = jsonPathEvaluationObject.toString() == "null" || jsonPathEvaluationObject.toString().equals("")  ? "0" : "1";
        }
        return returnValue;
    }


    private String getJsonPathEvaluationObjectStringValue(Object jsonPathEvaluationObject ){
        String returnValue = "null";
        if(jsonPathEvaluationObject instanceof ArrayList){
            returnValue = (String)((List)jsonPathEvaluationObject).stream().map(n -> n.toString()).sorted().distinct().collect(Collectors.joining(","));
        } else {
            returnValue = jsonPathEvaluationObject.toString();
        }
        return returnValue;
    }


}