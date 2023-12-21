package io.github.kc.api.consume;

import io.github.kc.api.httputil.HttpMethodsUtil;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIConsumer {
	private String baseEndPoint = "";
	private HttpMethodsUtil httpMethodsUtil;
	private String cookies = "";

	public APIConsumer(){
		httpMethodsUtil = new HttpMethodsUtil();
	}

	public APIConsumer(String userName, String password){
		httpMethodsUtil = new HttpMethodsUtil(userName, password);
	}
	
	public String getBaseEndPoint() {
		return baseEndPoint;
	}
	
	public void setBaseEndPoint(String baseEndPoint) throws Exception {
		this.baseEndPoint = baseEndPoint;
		setServiceEndPoint();
	}
	
	public void clearRestClientInstance() {
		httpMethodsUtil.clearHttpClientInstances();
	}
	
	private HashMap<String,Object> getReqResHash(){
		return httpMethodsUtil.getReqResHash();
	}
	
	public void clear() {
		clearCookies();
		httpMethodsUtil.clearHTTPClientAndReqResHashMap();
	}
	
	public void clearReqResHashInstance() {
		httpMethodsUtil.clearReqResHashMap();
	}
	
	public void clearEntryFromReqResHash(String reqName) {
		httpMethodsUtil.clearReqResHashMap(reqName);
	}
	
	public void setServiceEndPoint() throws Exception {
		httpMethodsUtil.setEndPoint(getBaseEndPoint());
	}
	
	public void setServiceEndPoint(String resourcePath) throws Exception {
		httpMethodsUtil.setEndPoint(getBaseEndPoint() + resourcePath);
	}
	
	public void setRequestParams(String strReqParams) throws Exception {
		httpMethodsUtil.setReqParams(strReqParams);
	}
	
	public void setRequest(String reqFilePathOrString, String reqName) throws Exception {
		httpMethodsUtil.putRequestIntoHash(reqFilePathOrString, reqName);
	}
	
	private void setCookies(String reqName) {
		cookies += getCookies(reqName);
	}
	
	public void setPassedCookies(String cookies){
		this.cookies += cookies;
	}
	
	public String getCookies(String reqName) {
		String returnValue = "";
		List<Header> reqHeaders = getResponseHeaders(reqName);
		for(Header reqHeader : reqHeaders) {
			if(reqHeader.getName().equalsIgnoreCase("Set-cookie")) {
				returnValue = returnValue + reqHeader.getValue().trim().split(";")[0] + ";";
			}
		}
		return returnValue;
	}
	
	public String getCookies() {
		return cookies;
	}
	
	public void clearCookies() {
		cookies = "";
	}
	
	public void addRequestHeader(String headerName, String headerValue, String reqName) throws Exception {
		if (getRequestBasicHeaders(reqName) != null) {
			ArrayList<BasicHeader> basicHeaderArrayList = (ArrayList<BasicHeader>) getRequestBasicHeaders(reqName);
			basicHeaderArrayList.add(new BasicHeader(headerName, headerValue));
			httpMethodsUtil.putRequestBasicHeadersIntoHash(basicHeaderArrayList,reqName);
		} else {
			ArrayList<BasicHeader> basicHeaderArrayList = new ArrayList<BasicHeader>();
			basicHeaderArrayList.add(new BasicHeader(headerName, headerValue));
			httpMethodsUtil.putRequestBasicHeadersIntoHash(basicHeaderArrayList, reqName);
		}
	}
	
	public void addRequestNameValuePair(String name, String value, String reqName) throws Exception {
		if (getRequestNameValuePairs(reqName) != null) {
			ArrayList<NameValuePair> reqNameValuePairArrayList = (ArrayList<NameValuePair>) getRequestNameValuePairs(reqName);
			reqNameValuePairArrayList.add(new BasicNameValuePair(name, value));
			httpMethodsUtil.putRequestNameValuePairsIntoHash(reqNameValuePairArrayList, reqName);
		} else {
			ArrayList<NameValuePair> reqNameValuePairArrayList = new ArrayList<NameValuePair>();
			reqNameValuePairArrayList.add(new BasicNameValuePair(name, value));
			httpMethodsUtil.putRequestNameValuePairsIntoHash(reqNameValuePairArrayList, reqName);
		}
	}
	
	public void consumeSOAPService(String reqName) throws Exception {
		addRequestHeader("Content-Type", "text/xml", reqName);
		addRequestHeader("Accept-Charset", "UTF-8",reqName);
		addRequestHeader("charset", "UTF-8",reqName);
		httpMethodsUtil.consumePostUsingStringEntityWithHeaders(reqName,false);
	}
	
	public void consumeHTTPPost(String reqName, String strReqParamsOrContext, Map<String, String> headers, String reqFilePathOrString) throws Exception {
		setRequestParams(strReqParamsOrContext);
		setRequest(reqFilePathOrString,reqName);
		consumeHTTPPostUsingStringEntiry(reqName,headers);
	}
	
	public void consumeHTTPPost(String reqName, String strReqParamsOrContext, Map<String, String> headers, Map<String, String> formData) throws Exception {
		setRequestParams(strReqParamsOrContext);
		for(String formDataName : formData.keySet()) {
			addRequestNameValuePair(formDataName,formData.get(formDataName),reqName);
		}
		consumeHTTPPostUsingUrlEncodedFormEntity(reqName,headers);
	}
	
	private void consumeHTTPPostUsingStringEntiry(String reqName, Map<String,String> headers) throws Exception {
		if(!cookies.equals("")) {
			addRequestHeader("cookie",cookies,reqName);
		}
		for(String headerName : headers.keySet()) {
			addRequestHeader(headerName,headers.get(headerName),reqName);
		}
		httpMethodsUtil.consumePostUsingStringEntityWithHeaders(reqName, false);
		setCookies(reqName);
		setBaseEndPoint(getBaseEndPoint());
	}
	
	private void consumeHTTPPostUsingUrlEncodedFormEntity(String reqName, Map<String,String> headers) throws Exception {
		if(!cookies.equals("")) {
			addRequestHeader("cookie",cookies,reqName);
		}
		for(String headerName : headers.keySet()) {
			addRequestHeader(headerName,headers.get(headerName),reqName);
		}
		httpMethodsUtil.consumePostUsingUrlEncodedFormEntityWithHeaders(reqName, false);
		setCookies(reqName);
		setBaseEndPoint(getBaseEndPoint());
	}
	
	public void consumePostRestfulMethod(String reqName, Map<String,String> headers) throws Exception {
		for(String headerName : headers.keySet()) {
			addRequestHeader(headerName,headers.get(headerName),reqName);
		}
		httpMethodsUtil.consumePostUsingStringEntityWithHeaders(reqName, false);
	}
	
	public void consumePostRestfulMethod(String reqName) throws Exception {
		httpMethodsUtil.consumePostUsingStringEntity(reqName, false);
	}
	
	public void consumeGetRestfulMethod(String reqName) throws Exception {
		httpMethodsUtil.consumeGet(reqName, false);
	}
	
	public void consumeHTTPGet(String reqName, String strReqParamsOrContext,Map<String,String> headers) throws Exception {
		setRequestParams(strReqParamsOrContext);
		consumeHTTPGetRequest(reqName,headers);
	}
	
	private void consumeHTTPGetRequest(String reqName, Map<String,String> headers) throws Exception {
		if(!cookies.equals("")) {
			addRequestHeader("cookie",cookies,reqName);
		}
		for(String headerName : headers.keySet()) {
			addRequestHeader(headerName,headers.get(headerName),reqName);
		}
		httpMethodsUtil.consumeGetWithHeaders(reqName, false);
		setCookies(reqName);
		setBaseEndPoint(getBaseEndPoint());
	}
	
	public void consumeGetRestfulMethod(String reqName, Map<String,String> headers) throws Exception {
		for(String headerName : headers.keySet()) {
			addRequestHeader(headerName,headers.get(headerName),reqName);
		}
		httpMethodsUtil.consumeGetWithHeaders(reqName, false);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<BasicHeader> getRequestBasicHeaders(String reqName) {
		return (ArrayList<BasicHeader>) getReqResHash().get(reqName + httpMethodsUtil.BASIC_HEADERS);
	}
	
	@SuppressWarnings("unchecked")
	public List<NameValuePair> getRequestNameValuePairs(String reqName) {
		return (List<NameValuePair>) getReqResHash().get(reqName + httpMethodsUtil.NAME_VALUE_PAIRS);
	}
	
	public String getRequestString(String reqName) {
		return getReqResHash().get(reqName + "_Request").toString();
	}
	
	public String getResponseString(String reqName) {
		return getReqResHash().get(reqName + "_Response").toString();
	}
	
	public String getResponseCode(String reqName) {
		return getReqResHash().get(reqName + "_ResponseCode").toString();
	}
	
	public String getResponseContentType(String reqName) {
		return getReqResHash().get(reqName + "_ContentType").toString();
	}
	
	public String getResponseFileType(String reqName) {
		return getReqResHash().get(reqName + "_FileType").toString();
	}
	
	public String getResponseTime(String reqName) {
		return getReqResHash().get(reqName + "_ResponseTime").toString();
	}
	
	@SuppressWarnings("unchecked")
	public List<Header> getResponseHeaders(String reqName){
		return (List<Header>) getReqResHash().get(reqName + "_ResponseHeaders");
	}
}