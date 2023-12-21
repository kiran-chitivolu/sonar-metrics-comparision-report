package io.github.kc.api.httputil;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HttpMethodsUtil {
    public final String  BASIC_HEADERS = "_RequestHeaders";
    public final String  NAME_VALUE_PAIRS = "_RequestNameValuePairs";
    private HttpClientUtil httpClientUtil;// = new HttpClientUtil();
    private HashMap<String, Object> reqResHash = new HashMap<String, Object>();
    
    public HashMap<String, Object> getReqResHash() {
        return reqResHash;
    }
    
    public HttpMethodsUtil() {
        try {
            httpClientUtil = new HttpClientUtil();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public HttpMethodsUtil(String userName, String password) {
        try {
            httpClientUtil = new HttpClientUtil(userName, password);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public void clearReqResHashMap() {
        reqResHash.clear();
    }
    
    public void clearReqResHashMap(String key) {
        reqResHash.remove(key);
    }
    
    public void clearHttpClientInstances() {
        try {
            httpClientUtil = null;
            httpClientUtil = new HttpClientUtil();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public void clearHTTPClientAndReqResHashMap() {
        try {
            clearReqResHashMap();
            clearHttpClientInstances();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public void setReqParams(String strReqParams) {
        httpClientUtil.setHttpRequestParams(strReqParams);
    }
    
    
    public void setReqParams(String strEndPoint, String strReqParams) {
        setEndPoint(strEndPoint);
        httpClientUtil.setHttpRequestParams(strReqParams);
    }
    
    public void setEndPoint(String strEndPoint) {
        httpClientUtil.setHttpEndPoint(strEndPoint);
    }
    
    public void putRequestIntoHash(Object req, String reqName) throws Exception {
        reqResHash.put(reqName + "_Request", req);
    }
    
    public void putRequestBasicHeadersIntoHash(ArrayList<BasicHeader> basicHeaders, String reqName) throws Exception {
        reqResHash.put(reqName + BASIC_HEADERS, basicHeaders);
    }
    
    public void putRequestNameValuePairsIntoHash(List<NameValuePair> nameValuePairs, String reqName) throws Exception {
        reqResHash.put(reqName + NAME_VALUE_PAIRS, nameValuePairs);
    }
    
    public void putResponseIntoHash(Object res, String reqName) throws Exception {
        reqResHash.put(reqName + "_Response", res);
    }
    
    public void consumePostUsingStringEntity(String reqName, boolean overProxy) throws Exception {
        httpClientUtil.setInput(getRequestString(reqName));
        httpClientUtil.consumePostUsingStringEntity(overProxy);
        putResponseDetailsIntoHash(reqName);
    }
    
    public void consumePostUsingStringEntityWithHeaders(String reqName, boolean overProxy) throws Exception {
        Header[] headers = new BasicHeader[(getRequestBasicHeaders(reqName)).size()];
        headers = ((ArrayList<BasicHeader>) getRequestBasicHeaders(reqName)).toArray(headers);
        httpClientUtil.setHeaders(headers);
        httpClientUtil.setInput(getRequestString(reqName));
        httpClientUtil.consumePostUsingStringEntityWithHeaders(overProxy);
        putResponseDetailsIntoHash(reqName);
    }
    
    public void consumePostUsingUrlEncodedFormEntity(String reqName, boolean overProxy) throws Exception {
        httpClientUtil.setNameValuePairs(getRequestNameValuePairs(reqName));
        httpClientUtil.consumePostUsingUrlEncodedFormEntity(overProxy);
        putResponseDetailsIntoHash(reqName);
    }
    
    public void consumePostUsingUrlEncodedFormEntityWithHeaders(String reqName, boolean overProxy) throws Exception {
        Header[] headers = new BasicHeader[(getRequestBasicHeaders(reqName)).size()];
        headers = ((ArrayList<BasicHeader>) getRequestBasicHeaders(reqName)).toArray(headers);
        httpClientUtil.setHeaders(headers);
        httpClientUtil.setNameValuePairs(getRequestNameValuePairs(reqName));
        httpClientUtil.consumePostUsingUrlEncodedFormEntityWithHeaders(overProxy);
        putResponseDetailsIntoHash(reqName);
    }
    
    public void consumeGet(String reqName, boolean overProxy) throws Exception {
        httpClientUtil.consumeGet(overProxy);
        putResponseDetailsIntoHash(reqName);
    }
    
    public void consumeGetWithHeaders(String reqName, boolean overProxy) throws Exception {
        Header[] headers = new BasicHeader[(getRequestBasicHeaders(reqName)).size()];
        headers = ((ArrayList<BasicHeader>) getRequestBasicHeaders(reqName)).toArray(headers);
        httpClientUtil.setHeaders(headers);
        httpClientUtil.consumeGetWithHeaders(overProxy);
        putResponseDetailsIntoHash(reqName);
    }
    
    private void putResponseDetailsIntoHash(String reqName) throws Exception {
        reqResHash.put(reqName + "_Response", httpClientUtil.getResponseString());
        reqResHash.put(reqName + "_ResponseCode", String.valueOf(httpClientUtil.getResponseCode()));
        reqResHash.put(reqName + "_ContentType", httpClientUtil.getResponseContentType());
        reqResHash.put(reqName + "_FileType", httpClientUtil.getResponseFileType());
        reqResHash.put(reqName + "_ResponseTime", httpClientUtil.getResponseTimeInMilliSeconds());
        reqResHash.put(reqName + "_ResponseHeaders", httpClientUtil.getResonseHeaders());
    }
    
    @SuppressWarnings("unchecked")
    private ArrayList<BasicHeader> getRequestBasicHeaders(String reqName) {
        return (ArrayList<BasicHeader>) reqResHash.get(reqName + BASIC_HEADERS);
    }
    
    @SuppressWarnings("unchecked")
    private List<NameValuePair> getRequestNameValuePairs(String reqName) {
        return (List<NameValuePair>) reqResHash.get(reqName + NAME_VALUE_PAIRS);
    }
    
    private String getRequestString(String reqName) {
        return reqResHash.get(reqName + "_Request").toString();
    }
    
    
}
