package io.swammdoc.common;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

/**
 * Created by chengpanwang on 2017/2/8.
 */
public class HttpClientUtils {

    private static final Logger        logger  = LoggerFactory.getLogger(HttpClientUtils.class);

    private static final String        CHARSET = "UTF-8";
    private static CloseableHttpClient httpClient;
    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(5);
        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    public static String get(String url, Map<String, String> params) {
        try {
            logger.info("get请求，url:{}, params:{}", url, params);

            if (MapUtils.isEmpty(params)) {
                return doGet(url, null);
            }

            List<NameValuePair> nameValuePairs = Lists.newArrayListWithCapacity(params.size());
            params.forEach((k, v) -> {
                nameValuePairs.add(new BasicNameValuePair(k, v));
            });
            HttpEntity requestEntity = new UrlEncodedFormEntity(nameValuePairs, CHARSET);

            return doGet(url, requestEntity);

        } catch (Exception e) {
            logger.error("", e);
        }
        return StringUtils.EMPTY;
    }

    public static String post(String url, String body) {
        try {
            logger.info("执行post请求，url:{}, body:{}", url, body);

            HttpEntity requestEntity = new StringEntity(body, ContentType.APPLICATION_JSON);

            return doPost(url, requestEntity);

        } catch (Exception e) {
            logger.error("", e);
        }
        return StringUtils.EMPTY;
    }

    public static String post(String url, Map<String, String> param) {
        try {
            logger.info("执行post请求，url:{}, body:{}", url, JSON.toJSONString(param));

            List<NameValuePair> nameValuePairs = Lists.newArrayList();
            if (MapUtils.isNotEmpty(param)) {
                param.forEach((k, v) -> {
                    nameValuePairs.add(new BasicNameValuePair(k, v));
                });
            }

            UrlEncodedFormEntity requestEntity = new UrlEncodedFormEntity(nameValuePairs, CHARSET);

            return doPost(url, requestEntity);

        } catch (Exception e) {
            logger.error("", e);
        }
        return StringUtils.EMPTY;
    }

    public static String doDelete(String url, HttpEntity httpEntity) {
        try {
            if (httpEntity != null) {
                url = url + "?" + EntityUtils.toString(httpEntity);
            }
            HttpDelete httpDelete = new HttpDelete(url);
            return execute(httpDelete);
        } catch (IOException e) {
            logger.error("", e);
        }
        return StringUtils.EMPTY;
    }

    public static String doGet(String url, HttpEntity httpEntity) {
        try {
            if (httpEntity != null) {
                url = url + "?" + EntityUtils.toString(httpEntity);
            }
            HttpGet httpGet = new HttpGet(url);
            return execute(httpGet);
        } catch (IOException e) {
            logger.error("", e);
        }
        return StringUtils.EMPTY;
    }

    public static String doPost(String url, HttpEntity httpEntity) {
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(httpEntity);

        return execute(httppost);
    }

    public static String execute(HttpUriRequest httpUriRequest) {
        String result = StringUtils.EMPTY;
        try {

            CloseableHttpResponse response = httpClient.execute(httpUriRequest);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    result = EntityUtils.toString(entity, CHARSET);
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        logger.info("执行http请求, result:{}", result);
        return result;
    }
}
