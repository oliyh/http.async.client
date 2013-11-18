package ahc;

import java.io.File;
import java.io.InputStream;
import org.asynchttpclient.Realm;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.ProxyServer;
import org.asynchttpclient.Cookie;
import org.asynchttpclient.generators.InputStreamBodyGenerator;

public class RequestBuilderWrapper {
    private RequestBuilder rb;
    public RequestBuilderWrapper(RequestBuilder rb) { this.rb = rb; }
    public RequestBuilderWrapper addHeader(String k, String v) {
        rb.addHeader(k, v);
        return this;}
    public RequestBuilderWrapper setHeader(String k, String v) {
        rb.setHeader(k, v);
        return this;}
    public RequestBuilderWrapper addParameter(String k, String v) {
        rb.addParameter(k, v);
        return this;}
    public RequestBuilderWrapper addQueryParameter(String k, String v) {
        rb.addQueryParameter(k,v);
        return this;}
    public RequestBuilderWrapper setBody(byte[] data) {
        rb.setBody(data);
        return this;}
    public RequestBuilderWrapper setBody(InputStream stream) {
        rb.setBody(new InputStreamBodyGenerator(stream));
        return this;}
    public RequestBuilderWrapper setBody(File f) {
        rb.setBody(f);
        return this;}
    public RequestBuilderWrapper setProxyServer(ProxyServer proxy) {
        rb.setProxyServer(proxy);
        return this;}
    public RequestBuilderWrapper addCookie(Cookie cookie) {
        rb.addCookie(cookie);
        return this;}
    public RequestBuilderWrapper setRealm(Realm realm) {
        rb.setRealm(realm);
        return this;}
    public RequestBuilderWrapper setTimeout(int timeout) {
        rb.setRequestTimeoutInMs(timeout);
        return this;}
    public RequestBuilder getRequestBuilder() { return rb; }}
