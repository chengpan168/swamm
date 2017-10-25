package com.swamm.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class HttpUtils {

    public static final String USER_AGENT_DEFAULT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.58 Safari/537.36s";
    public static final String CHARSET_UTF8       = "UTF-8";
    public static String       COOKIE             = "account=chengpanwang;password=123456";
    public static String       SESSION_ID         = "";

    public static String doGet(String link) throws IOException {
        return doGet(link, null);
    }

    public static String doGet(String link, Map<String, String> headers) {
        return doGet(link, CHARSET_UTF8, headers);
    }

    public static String doGet(String link, String charset, Map<String, String> headers) {
        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT_DEFAULT);
            conn.setRequestProperty("Cookie", COOKIE);
            conn.setReadTimeout(60000);
            if (null != headers && !headers.isEmpty()) {
                for (Iterator<String> iter = headers.keySet().iterator(); iter.hasNext();) {
                    String key = iter.next();
                    conn.setRequestProperty(key, headers.get(key));
                }
            }
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int i = 0; (i = in.read(buf)) > 0;) {
                out.write(buf, 0, i);
            }

            //获取cookie
            if (SESSION_ID == null || SESSION_ID.isEmpty()) {

                Map<String, List<String>> map = conn.getHeaderFields();
                Set<String> set = map.keySet();
                if (set != null && set.size() > 0) {

                    for (Iterator iterator = set.iterator(); iterator.hasNext();) {
                        String key = (String) iterator.next();
                        if (key == null || key.isEmpty()) {
                            continue;
                        }
                        if (key.equals("Set-Cookie")) {
                            System.out.println("key=" + key + ",开始获取cookie");
                            List<String> list = map.get(key);
                            StringBuilder builder = new StringBuilder();
                            for (String str : list) {
                                builder.append(str).toString();
                            }
                            SESSION_ID = builder.toString();
                        }
                    }

                    COOKIE += ";" + SESSION_ID;
                }
            }

            out.flush();
            String s = new String(out.toByteArray(), charset);
            return s;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String doPost(String reqUrl, String jsonBody) {
        HttpURLConnection urlConn = null;
        try {
            urlConn = sendPost(reqUrl, jsonBody);
            String responseContent = getContent(urlConn);
            return responseContent.trim();
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
                urlConn = null;
            }
        }
    }

    public static String doPost(String reqUrl, Map<String, String> parameters) {
        return doPost(reqUrl, parameters, CHARSET_UTF8);
    }

    public static String doPost(String reqUrl, Map<String, String> parameters, String charset) {
        HttpURLConnection urlConn = null;
        try {
            urlConn = sendPost(reqUrl, parameters, charset);
            String responseContent = getContent(urlConn);
            return responseContent.trim();
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
                urlConn = null;
            }
        }
    }

    private static String getContent(HttpURLConnection urlConn) {
        try {
            String responseContent = null;
            InputStream in = urlConn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, CHARSET_UTF8));
            String tempLine = rd.readLine();
            StringBuffer tempStr = new StringBuffer();
            String crlf = System.getProperty("line.separator");
            while (tempLine != null) {
                tempStr.append(tempLine);
                tempStr.append(crlf);
                tempLine = rd.readLine();
            }
            responseContent = tempStr.toString();
            rd.close();
            in.close();
            return responseContent;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String doUploadFile(String reqUrl, Map<String, String> parameters, String fileParamName, String filename, String contentType,
                                      byte[] data) {
        HttpURLConnection urlConn = null;
        try {
            urlConn = sendFormdata(reqUrl, parameters, fileParamName, filename, contentType, data);
            String responseContent = new String(getBytes(urlConn));
            return responseContent.trim();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }
    }

    public static byte[] getBytes(String reqUrl) throws Exception {
        URL url = new URL(reqUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT_DEFAULT);
        BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        for (int i = 0; (i = in.read(buf)) > 0;) {
            out.write(buf, 0, i);
        }
        out.flush();
        return out.toByteArray();
    }

    private static byte[] getBytes(URLConnection urlConn) throws Exception {
        InputStream input = null;
        try {
            input = urlConn.getInputStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int i = 0; (i = input.read(buf)) > 0;) {
                os.write(buf, 0, i);
            }
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (null != input) {
                input.close();
            }
        }
    }

    private static HttpURLConnection sendFormdata(String reqUrl, Map<String, String> parameters, String fileParamName, String filename,
                                                  String contentType, byte[] data) {
        HttpURLConnection urlConn = null;
        try {
            URL url = new URL(reqUrl);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setConnectTimeout(10000);// （单位：毫秒）jdk
            urlConn.setReadTimeout(30000);// （单位：毫秒）jdk 1.5换成这个,读操作超时
            urlConn.setDoOutput(true);

            urlConn.setRequestProperty("connection", "keep-alive");

            String boundary = "-----------------------------114975832116442893661388290519"; // 分隔符
            urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            boundary = "--" + boundary;
            StringBuffer params = new StringBuffer();
            if (parameters != null) {
                for (Iterator<String> iter = parameters.keySet().iterator(); iter.hasNext();) {
                    String name = iter.next();
                    String value = parameters.get(name);
                    params.append(boundary + "\r\n");
                    params.append("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
                    // params.append(URLEncoder.encode(value,
                    // CHARSET));
                    params.append(value);
                    params.append("\r\n");
                }
            }

            StringBuilder sb = new StringBuilder();
            // sb.append("\r\n");
            sb.append(boundary);
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"" + fileParamName + "\"; filename=\"" + filename + "\"\r\n");
            sb.append("Content-Type: " + contentType + "\r\n\r\n");
            byte[] fileDiv = sb.toString().getBytes();
            byte[] endData = ("\r\n" + boundary + "--\r\n").getBytes();
            byte[] ps = params.toString().getBytes();

            OutputStream os = urlConn.getOutputStream();
            os.write(ps);
            os.write(fileDiv);
            os.write(data);
            os.write(endData);

            os.flush();
            os.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return urlConn;
    }

    public static HttpURLConnection sendPost(String reqUrl, Map<String, String> parameters, String charset) {
        String params = generatorParamString(parameters, charset);
        HttpURLConnection urlConn = sendPost(reqUrl, params);
        return urlConn;
    }

    public static HttpURLConnection sendPost(String reqUrl, String httpBody) {
        HttpURLConnection urlConn = null;
        try {
            URL url = new URL(reqUrl);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("User-Agent", USER_AGENT_DEFAULT);
            urlConn.setRequestProperty("Cookie", COOKIE);

            urlConn.setConnectTimeout(10000);// （单位：毫秒）jdk
            urlConn.setReadTimeout(30000);// （单位：毫秒）jdk 1.5换成这个,读操作超时
            urlConn.setDoOutput(true);

            byte[] b = httpBody.getBytes();
            urlConn.getOutputStream().write(b, 0, b.length);

            urlConn.getOutputStream().flush();
            urlConn.getOutputStream().close();


            //获取cookie
            if (SESSION_ID == null || SESSION_ID.isEmpty()) {

                Map<String, List<String>> map = urlConn.getHeaderFields();
                Set<String> set = map.keySet();
                if (set != null && set.size() > 0) {

                    for (Iterator iterator = set.iterator(); iterator.hasNext();) {
                        String key = (String) iterator.next();
                        if (key == null || key.isEmpty()) {
                            continue;
                        }
                        if (key.equals("Set-Cookie")) {
                            System.out.println("key=" + key + ",开始获取cookie");
                            List<String> list = map.get(key);
                            StringBuilder builder = new StringBuilder();
                            for (String str : list) {
                                builder.append(str).toString();
                            }
                            SESSION_ID = builder.toString();
                        }
                    }

                    COOKIE += ";" + SESSION_ID;
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return urlConn;
    }

    /**
     * 将parameters中数据转换成用"&"链接的http请求参数形式
     * 
     * @param parameters
     * @return
     */
    public static String generatorParamString(Map<String, String> parameters, String charset) {
        StringBuffer params = new StringBuffer();
        if (parameters != null) {
            for (Iterator<String> iter = parameters.keySet().iterator(); iter.hasNext();) {
                String name = iter.next();
                String value = parameters.get(name);
                params.append(name + "=");
                try {
                    params.append(URLEncoder.encode(value, StringUtils.isBlank(charset) ? CHARSET_UTF8 : charset));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                } catch (Exception e) {
                    String message = String.format("'%s'='%s'", name, value);
                    throw new RuntimeException(message, e);
                }
                if (iter.hasNext()) params.append("&");
            }
        }
        return params.toString();
    }

    public static void main(String[] args) {
        //		IpRegionDto ipregion = getIpRegion("115.238.80.156");
        //		System.out.print("dd");

        String regex = "\\d+(\\.\\d+)+";
        Pattern p = Pattern.compile(regex);

        System.out.println(p.matcher("1.90.6").matches());
    }

    /**
     * 从url获取结果
     * 
     * @param path
     * @param params
     * @param encoding
     * @return
     */
    public static String getRs(String path, String params, String encoding) {

        URL url = null;

        HttpURLConnection connection = null;

        try {

            url = new URL(path);

            connection = (HttpURLConnection) url.openConnection();// 新建连接实例

            connection.setConnectTimeout(2000);// 设置连接超时时间

            connection.setReadTimeout(2000);// 设置读取数据超时时间

            connection.setDoInput(true);// 是否打开输出

            connection.setDoOutput(true);// 是否打开输入流true|false

            connection.setRequestMethod("POST");// 提交方法POST|GET

            connection.setUseCaches(false);// 是否缓存true|false

            connection.connect();// 打开连接端口

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.writeBytes(params);

            out.flush();

            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));

            StringBuffer buffer = new StringBuffer();

            String line = "";

            while ((line = reader.readLine()) != null) {

                buffer.append(line);

            }

            reader.close();

            return buffer.toString();

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            connection.disconnect();// 关闭连接

        }

        return null;
    }

    private static long getIpNum(String ipAddress) {
        String[] ip = ipAddress.split("\\.");
        long a = Integer.parseInt(ip[0]);
        long b = Integer.parseInt(ip[1]);
        long c = Integer.parseInt(ip[2]);
        long d = Integer.parseInt(ip[3]);

        long ipNum = a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
        return ipNum;
    }

    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }

    public static boolean isInnerIP(String ipAddress) {
        boolean isInnerIp = false;
        try {

            long ipNum = getIpNum(ipAddress);
            /**
             * 私有IP：A类 10.0.0.0-10.255.255.255 B类 172.16.0.0-172.31.255.255 C类
             * 192.168.0.0-192.168.255.255 当然，还有127这个网段是环回地址
             **/
            long aBegin = getIpNum("10.0.0.0");
            long aEnd = getIpNum("10.255.255.255");
            long bBegin = getIpNum("172.16.0.0");
            long bEnd = getIpNum("172.31.255.255");
            long cBegin = getIpNum("192.168.0.0");
            long cEnd = getIpNum("192.168.255.255");
            isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd) || ipAddress.equals("127.0.0.1");
        } catch (Exception e) {
        }
        return isInnerIp;
    }

}
