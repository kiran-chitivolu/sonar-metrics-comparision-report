package io.github.kc.api.httputil;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

public class HttpClientUtil {
    private HttpClient client;
    private HttpClient clientWithProxy;
    private HttpPost post;
    private HttpGet get;
    private HttpResponse response;
    private String strInput;
    private String strEndPoint;
    private List<NameValuePair> nameValuePairs;
    private Header[] basicHeaders;
    private long responseTimeInMilliSeconds;
    private String proxyServer = System.getProperty("http.proxyHost","");
    private int proxyPort = Integer.parseInt(System.getProperty("http.proxyPort","80"));
    private String userName = System.getProperty("http.proxyUser","");
    private String password = System.getProperty("http.proxyPassword","");
    
    public HttpClientUtil() {
        try {
            client = getHTTPClient();
            clientWithProxy = getHTTPProxyClient();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    public HttpClientUtil(String userName, String password){
        try {
            client = getHTTPClient(userName, password);
            clientWithProxy = getHTTPProxyClient(userName, password);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    private HttpClientBuilder createHttpClientBuilder_AcceptsUntrustedCerts() throws Throwable {
        HttpClientBuilder b = HttpClientBuilder.create();
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                return true;
            }
        }).build();
        //b.setSSLContext(sslContext);
        // don't check Hostnames, either.
        // -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to
        // weaken
        //HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        // here's the special part:
        // -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
        // -- and create a Registry, to register it.
        //HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        //SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslSocketFactory).build();
        // now, we create connection-manager using our Registry.
        // -- allows multi-threaded use
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        b.setConnectionManager(connMgr);
        return b;
    }
    
    private HttpClient getHTTPClient() throws Throwable {
        return createHttpClientBuilder_AcceptsUntrustedCerts().build();
    }

    private HttpClient getHTTPClient(String userName1, String password1) throws Throwable {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(userName1, password1);
        provider.setCredentials(AuthScope.ANY, credentials);
        HttpClientBuilder httpClientBuilder = createHttpClientBuilder_AcceptsUntrustedCerts();
        httpClientBuilder = httpClientBuilder.setDefaultCredentialsProvider(provider);
        HttpClient httpclient = httpClientBuilder.build();
        return httpclient;
    }
    
    private HttpClient getHTTPProxyClient() throws Throwable {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxyServer, proxyPort), new UsernamePasswordCredentials(userName, password));
        HttpClientBuilder httpClientBuilder = createHttpClientBuilder_AcceptsUntrustedCerts();
        httpClientBuilder = httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
        HttpClient httpclient = httpClientBuilder.build();
        return httpclient;
    }

    private HttpClient getHTTPProxyClient(String userName1, String password1) throws Throwable {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxyServer, proxyPort), new UsernamePasswordCredentials(userName, password));
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(userName1, password1);
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClientBuilder httpClientBuilder = createHttpClientBuilder_AcceptsUntrustedCerts();
        httpClientBuilder = httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
        HttpClient httpclient = httpClientBuilder.build();
        return httpclient;
    }
    
    private RequestConfig getRequestConfigWithProxy() {
        HttpHost proxy = new HttpHost(proxyServer, proxyPort);
        RequestConfig config = RequestConfig.custom().setProxy(proxy).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
        return config;
    }
    
    public void setHttpEndPoint(String strEndPoint) {
        this.strEndPoint = strEndPoint;
        // post = new HttpPost(strEndPoint);
    }
    
    public void setHttpRequestParams(String strParams) {
        this.strEndPoint = this.strEndPoint + strParams;
    }
    
    public void setInput(String strInput) {
        this.strInput = strInput;
    }
    
    public void setNameValuePairs(List<NameValuePair> nameValuePairs) {
        this.nameValuePairs = nameValuePairs;
    }
    
    public void setHeaders(Header[] basicHeaders) {
        this.basicHeaders = basicHeaders;
    }
    
    public HttpResponse getHTTPReponse() {
        return this.response;
    }
    
    public void consumePostUsingStringEntityWithHeaders(boolean overProxy) throws ClientProtocolException, Exception {
        StringEntity input = new StringEntity(this.strInput, "UTF-8");
        post = new HttpPost(this.strEndPoint);
        post.setHeaders(basicHeaders);
        if (overProxy) {
            post.setConfig(getRequestConfigWithProxy());
        } else {
            post.setConfig(RequestConfig.custom().build());
        }
        post.setEntity(input);
        long startTimeInMilliSeconds = System.currentTimeMillis();
        this.response = client.execute(post);
        long endTimeInMilliSeconds = System.currentTimeMillis();
        responseTimeInMilliSeconds = endTimeInMilliSeconds - startTimeInMilliSeconds;
    }
    
    public void consumePostUsingUrlEncodedFormEntityWithHeaders(boolean overProxy) throws ClientProtocolException, Exception {
        UrlEncodedFormEntity input = new UrlEncodedFormEntity(this.nameValuePairs, "UTF-8");
        post = new HttpPost(this.strEndPoint);
        post.setHeaders(basicHeaders);
        if (overProxy) {
            post.setConfig(getRequestConfigWithProxy());
        } else {
            post.setConfig(RequestConfig.custom().build());
        }
        post.setEntity(input);
        long startTimeInMilliSeconds = System.currentTimeMillis();
        this.response = clientWithProxy.execute(post);
        long endTimeInMilliSeconds = System.currentTimeMillis();
        responseTimeInMilliSeconds = endTimeInMilliSeconds - startTimeInMilliSeconds;
    }
    
    public void consumeGet(boolean overProxy) throws ClientProtocolException, Exception {
        get = new HttpGet(this.strEndPoint);
        if (overProxy) {
            get.setConfig(getRequestConfigWithProxy());
        } else {
            get.setConfig(RequestConfig.custom().build());
        }
        long startTimeInMilliSeconds = System.currentTimeMillis();
        this.response = clientWithProxy.execute(get);
        long endTimeInMilliSeconds = System.currentTimeMillis();
        responseTimeInMilliSeconds = endTimeInMilliSeconds - startTimeInMilliSeconds;
    }
    
    public void consumeGetWithHeaders(boolean overProxy) throws ClientProtocolException, Exception {
        get = new HttpGet(this.strEndPoint);
        get.setHeaders(basicHeaders);
        if (overProxy) {
            get.setConfig(getRequestConfigWithProxy());
        } else {
            get.setConfig(RequestConfig.custom().build());
        }
        long startTimeInMilliSeconds = System.currentTimeMillis();
        this.response = clientWithProxy.execute(get);
        long endTimeInMilliSeconds = System.currentTimeMillis();
        responseTimeInMilliSeconds = endTimeInMilliSeconds - startTimeInMilliSeconds;
    }
    
    public void consumePostUsingStringEntity(boolean overProxy) throws ClientProtocolException, Exception {
        StringEntity input = new StringEntity(this.strInput, "UTF-8");
        post = new HttpPost(this.strEndPoint);
        if (overProxy) {
            post.setConfig(getRequestConfigWithProxy());
        } else {
            post.setConfig(RequestConfig.custom().build());
        }
        post.setEntity(input);
        long startTimeInMilliSeconds = System.currentTimeMillis();
        this.response = client.execute(post);
        long endTimeInMilliSeconds = System.currentTimeMillis();
        responseTimeInMilliSeconds = endTimeInMilliSeconds - startTimeInMilliSeconds;
    }
    
    public void consumePostUsingUrlEncodedFormEntity(boolean overProxy) throws ClientProtocolException, Exception {
        UrlEncodedFormEntity input = new UrlEncodedFormEntity(this.nameValuePairs, "UTF-8");
        post = new HttpPost(this.strEndPoint);
        if (overProxy) {
            post.setConfig(getRequestConfigWithProxy());
        } else {
            post.setConfig(RequestConfig.custom().build());
        }
        post.setEntity(input);
        long startTimeInMilliSeconds = System.currentTimeMillis();
        this.response = clientWithProxy.execute(post);
        long endTimeInMilliSeconds = System.currentTimeMillis();
        responseTimeInMilliSeconds = endTimeInMilliSeconds - startTimeInMilliSeconds;
    }
    
    public HttpResponse getResponse() {
        return response;
    }
    
    public long getResponseTimeInMilliSeconds() {
        return responseTimeInMilliSeconds;
    }
    
    public List<Header> getResonseHeaders() {
        return Arrays.asList(response.getAllHeaders());
    }
    
    public String getResponseCode() {
        return String.valueOf(response.getStatusLine().getStatusCode());
    }
    
    public String getResponseString() throws Exception {
        try {
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            return "";
        }
    }
    
    public String getResponseContentType() {
        try {
            return ContentType.get(response.getEntity()).getMimeType();
        } catch (Exception e) {
            return "";
        }
    }
    
    public String getResponseFileType() {
        String returnVal = "txt";
        if (getResponseContentType().contains("/")) {
            returnVal = getResponseContentType().split("/")[1];
        }
        return returnVal.equalsIgnoreCase("plain") ? "txt" : returnVal;
    }
}
