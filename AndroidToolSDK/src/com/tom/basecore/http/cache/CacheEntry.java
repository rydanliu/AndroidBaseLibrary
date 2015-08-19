package com.tom.basecore.http.cache;

import java.util.Collections;
import java.util.Map;

/**
 * Description:存放http响应的缓存
 * User： yuanzeyao.
 * Date： 2015-08-18 11:20
 */
public class CacheEntry {
    /** http请求的数据 */
    public byte[] data;

    /** 验证令牌 用于缓存过期后，资源是否被修改，客户端自动在If-None-MatchHTTP 请求头中提供 ETag 令牌，
     * 服务器针对当前的资源检查令牌，如果未被修改过，则返回304 Not Modified响应，
     * 告诉浏览器缓存中的响应未被修改过。
     * 注意，我们不必再次下载响应 - 这节约了时间和带宽 */
    public String etag;

    /** 服务器响应此请求时间(本地时间) */
    public long serverDate;

    /** 服务端规定的过期时间 */
    public long ttl;

    /** 客户端规定的过期时间 */
    public long softTtl;

    /** 服务端响应的头信息 */
    public Map<String, String> responseHeaders = Collections.emptyMap();

    /** 判断是缓存是否过期 */
    public boolean isExpired() {
        return this.ttl < System.currentTimeMillis()|| this.softTtl<System.currentTimeMillis();
    }

}
