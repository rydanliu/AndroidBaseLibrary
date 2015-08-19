package com.tom.basecore.http.cache;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:用于解析http响应头信息
 * User： yuanzeyao.
 * Date： 2015-08-18 13:17
 */
public class HttpHeaderParser {

    /**
     * 解析头部中和缓存相关的字段
     * @param response
     * @return
     */
    public static final CacheEntry parseCacheHeaders(HttpResponse response){
        long now=System.currentTimeMillis();
        Map<String, String> headers= Collections.emptyMap();
        Header[] tmp_headers = response.getAllHeaders();
        if(tmp_headers!=null){
            headers=new HashMap<String,String>();
            for(Header header : tmp_headers){
                headers.put(header.getName(),header.getValue());
            }
        }

        long serverDate = 0;
        long serverExpires = 0;
        long serverMaxAge = 0;
        long maxAge = 0;
        boolean hasCacheControl = false;

        String serverEtag = null;
        String headerValue;

        headerValue = headers.get("Date");if (headerValue != null) {
            serverDate = parseDateAsEpoch(headerValue);
        }

        headerValue = headers.get("Cache-Control");
        if (headerValue != null) {
            hasCacheControl = true;
            String[] tokens = headerValue.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if (token.equals("no-cache") || token.equals("no-store")) {
                    maxAge=0;
                } else if (token.startsWith("max-age=")) {
                    try {
                        maxAge = Long.parseLong(token.substring(8));
                    } catch (Exception e) {
                    }
                } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                    maxAge = 0;
                }
            }
        }

        headerValue = headers.get("Expires");
        if (headerValue != null) {
            serverExpires = parseDateAsEpoch(headerValue);
        }

        serverEtag = headers.get("ETag");

        if (hasCacheControl) {
            serverMaxAge = now + maxAge * 1000;
        } else if (serverDate > 0 && serverExpires >= serverDate) {
            serverMaxAge = now + (serverExpires - serverDate);
        }


        CacheEntry entry =new CacheEntry();
        entry.etag = serverEtag;
        entry.ttl = serverMaxAge;
        entry.softTtl = entry.ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;
        return entry;

    }

    public static long parseDateAsEpoch(String dateStr) {
        try {
            // Parse date in RFC1123 format if this header contains one
            return DateUtils.parseDate(dateStr).getTime();
        } catch (DateParseException e) {
            // Date in invalid format, fallback to 0
            return 0;
        }
    }
}
