
package cn.ayl.util;

import cn.ayl.util.json.JsonObject;
import cn.ayl.util.json.JsonObjects;
import cn.ayl.util.json.JsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * String 工具类
 */
public class StringUtil {

    //邮件正则
    public static final Pattern EmailPattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    private static String delims = " +-*=/!:;{}(),.?'\"\\\t\n\r";
    public static final char SPLITCHAR = '☉';

    //判断是否为Json
    public static boolean isJson(String content) {
        try {
            JsonUtil.parse(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String append(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String v : values) {
            builder.append(v);
        }
        return builder.toString();
    }

    public static String json(String[] values) {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        for (int i = 0; i < values.length; i++) {
            buf.append("\"");
            buf.append(values[i]);
            buf.append("\"");
            if (i < values.length - 1) buf.append(",");
        }
        buf.append("]");
        return buf.toString();
    }

    public static byte[] toBytes(String content) {
        if (content == null) return null;
        try {
            return content.getBytes("UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    public static String toString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String content = sw.toString();
        String[] lines = content.split("\n");
        StringBuilder buf = new StringBuilder();
        for (String line : lines) {
            String v = line.trim();
            if (v.startsWith("at io.netty")) continue;
            if (v.startsWith("at rx.internal")) continue;
            if (v.startsWith("at com.netflix")) continue;
            if (v.startsWith("at java.lang")) continue;
            buf.append(line);
            buf.append("\n");
        }
        return buf.toString();
    }

    public static String toString(byte[] bytes) {
        if (bytes == null) return null;
        try {
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    public static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        } catch (Exception e) {
            return value;
        }
    }

    public static String trimHostUri(String uri) {
        if (StringUtil.isEmpty(uri)) return "";
        String c = "http";
        if (uri.startsWith(c)) {
            uri = uri.substring(c.length());
        }
        c = "s";
        if (uri.startsWith(c)) {
            uri = uri.substring(c.length());
        }
        c = "://";
        if (uri.startsWith(c)) {
            uri = uri.substring(c.length());
        }
        c = "www.yiqihi.com";
        if (uri.startsWith(c)) {
            uri = uri.substring(c.length());
        }
        c = "m.yiqihi.com";
        if (uri.startsWith(c)) {
            uri = uri.substring(c.length());
        }
        c = "/";
        if (uri.startsWith(c)) {
            uri = uri.substring(1);
        }
        c = "#/";
        if (uri.startsWith(c)) {
            uri = uri.substring(c.length());
        }
        return uri;
    }

    public static String toString(byte[] bytes, String encode) {
        if (bytes == null)
            return null;
        try {
            return new String(bytes, encode);
        } catch (Exception e) {
            return new String(bytes);
        }
    }

    public static String printSqlValues(Object[] values) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            buf.append("'");
            buf.append(values[i].toString());
            buf.append("'");
            if (i < values.length - 1) buf.append(",");
        }
        return buf.toString();
    }


    public static String quoted(String value) {
        return "'" + value + "'";
    }

    public static String quotedSpace(String value) {
        return " " + value + " ";
    }

    public static boolean isEmpty(String v) {
        if (StringUtils.isEmpty(v)) {
            return true;
        }
        if (v.equalsIgnoreCase("null") || v.equalsIgnoreCase("undefined")) {
            return true;
        }
        return false;
    }

    public static boolean notEmpty(String v) {
        return !isEmpty(v);
    }

    public static String urlDecode(String v) {
        try {
            return URLDecoder.decode(v, "UTF-8");
        } catch (Exception e) {
            return v;
        }
    }

    public static String htmlBy(String content) {
        StringBuilder buffer = new StringBuilder();
        content.replaceAll("\r", "");
        int pos = content.indexOf("\n");
        buffer.append("<p>");
        while (pos >= 0) {
            buffer.append(content.substring(0, pos));
            buffer.append("</p><p>");
            content = content.substring(pos + 1);
            pos = content.indexOf("\n");
        }
        buffer.append(content);
        buffer.append("</p>");
        return buffer.toString();
    }

    public static String normalize(String content) {
        int count = 1;
        int index = (int) Math.pow(2, count++);
        char[] values = content.toCharArray();
        while (index < values.length) {
            if (index == 0) {
                continue;
            }
            char c = values[index];
            values[index] = values[index - 1];
            values[index - 1] = c;
            index = (int) Math.pow(2, count++);
        }
        return String.valueOf(values);
    }

    public static String join(HashSet<?> table, String token) {
        StringBuilder buffer = new StringBuilder();
        for (Iterator<?> i = table.iterator(); i.hasNext(); ) {
            if (buffer.length() > 0) {
                buffer.append(token);
            }
            buffer.append(String.valueOf(i.next()));
        }
        return buffer.toString();
    }

    public static String insertParamsBy(int count) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (buffer.length() > 0) {
                buffer.append(",");
            }
            buffer.append("?");
        }
        return buffer.toString();
    }

    public static String updateParamsBy(String[] values) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (buffer.length() > 0) {
                buffer.append(",");
            }
            buffer.append(values[i] + "=?");
        }
        return buffer.toString();
    }

    public static String join(String[] values, String token) {
        StringBuilder buffer = new StringBuilder();
        HashSet<String> table = new HashSet();
        for (int i = 0; i < values.length; i++) {
            String value = trim(values[i]);
            if (StringUtil.isEmpty(value) == true) {
                continue;
            }
            if (table.contains(value)) {
                continue;
            }
            table.add(value);
            if (buffer.length() > 0) {
                buffer.append(" ");
                buffer.append(token);
                buffer.append(" ");
            }
            buffer.append(value);
        }
        return buffer.toString();
    }

    public static String join(List<String> values, String token) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            String value = trim(values.get(i));
            if (buffer.length() > 0) {
                buffer.append(token);
            }
            buffer.append(value);
        }
        return buffer.toString();
    }

    public static String join(JsonObjects values, String token) {
        StringBuilder buffer = new StringBuilder();
        HashSet<String> table = new HashSet();
        for (int i = 0; values != null && i < values.size(); i++) {
            if (values.get(i) == null)
                continue;
            String value = trim(values.get(i).toJson());
            if (StringUtil.isEmpty(value) == true) {
                continue;
            }
            if (table.contains(value)) {
                continue;
            }
            table.add(value);
            if (buffer.length() > 0) {
                buffer.append(" ");
                buffer.append(token);
                buffer.append(" ");
            }
            buffer.append(value);
        }
        return buffer.toString();
    }

    private static boolean isTrimChar(char ch) {
        return ch == (char) 160 || ch == '\t' || ch == '\n' || ch == '\r' || ch == ' ' || ch == '　';
    }


    public static String trim(String text) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isTrimChar(c)) continue;
            buf.append(c);
        }
        return buf.toString();
    }

    /**
     * 正则验证是否为 电子邮件
     *
     * @param hex
     * @return
     */
    public static boolean validateEmail(final String hex) {
        Matcher matcher = EmailPattern.matcher(hex);
        return matcher.matches();
    }

    public static boolean isAlphanumeric(String v) {
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (StringUtils.isAsciiPrintable(String.valueOf(c)) == false) {
                return false;
            }
        }
        return true;
    }

    public static String trimContinue(String textBuffer) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(textBuffer);
        return trimContinue(buffer);
    }

    public static String trimContinue(StringBuilder textBuffer) {
        int index = 0;
        boolean lastIsEmpty = false;
        while (index < textBuffer.length()) {
            char ch = textBuffer.charAt(index);
            boolean isEmpty = false;
            if (ch == (char) 160 || ch == '\t' || ch == '\n' || ch == '\r' || ch == ' ' || ch == '　' || ch == '.' || ch == '-') {
                isEmpty = true;
            }
            if (!isEmpty) {
                lastIsEmpty = false;
                index++;
                continue;
            }
            if (lastIsEmpty && isEmpty) {
                textBuffer.deleteCharAt(index);
                continue;
            }
            textBuffer.setCharAt(index, ' ');
            lastIsEmpty = true;
            index++;
        }
        return textBuffer.toString();
    }

    public static String compressContinueChar(String content, char c, String replace) {
        int index = 0;
        StringBuilder buffer = new StringBuilder();
        int count = 0;
        while (index < content.length()) {
            char c1 = content.charAt(index++);
            if (c1 == c) {
                count++;
                continue;
            } else if (count >= 2) {
                buffer.append(replace);
                count = 0;
            }
            buffer.append(c1);
        }
        return buffer.toString();
    }


    public static String escape(String content) {
        if (content == null) {
            return "";
        }
        content = content.replaceAll("\r\n", "\\\\n").replaceAll("\n", "\\\\n");
        StringBuffer sb = new StringBuffer();
        for (int i = 0, len = content.length(); i < len; i++) {
            char c = content.charAt(i);
            switch (c) {
                case ' ':
                    sb.append("&nbsp;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String toHtml(String content) {
        if (content == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, len = content.length(); i < len; i++) {
            char c = content.charAt(i);
            switch (c) {
                case ' ':
                    sb.append("&nbsp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString().replaceAll("\r\n", "<br/>").replaceAll("\n", "<br/>");
    }

    public static String createPassword(int length) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            buffer.append(RandomUtils.nextInt(0, 9));
        }
        return buffer.toString();
    }

    public static String zeroString(int size) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < size; i++) {
            buffer.append("0");
        }
        return buffer.toString();
    }

    public static String getIP(String url) {
        try {
            URL uri = new URL(url);
            InetAddress address = InetAddress.getByName(uri.getHost());
            return address.getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getLocalName() {
        try {
            Runtime run = Runtime.getRuntime();
            Process proc = run.exec("hostname");
            StringWriter writer = new StringWriter();
            IOUtils.copy(proc.getInputStream(), writer, "utf-8");
            String name = StringUtil.trim(writer.toString());
            return name;
        } catch (Exception e) {
            return "unknow";
        }
    }

    public static JsonObject getLocal() {
        JsonObject oResult = new JsonObject().append("name", getLocalName());
        Enumeration<NetworkInterface> netInterfaces;
        JsonObjects items = new JsonObjects();
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                String name = ni.getName().toLowerCase();
                if (name.startsWith("lo") || name.startsWith("vir") || name.startsWith("vmnet") || name.startsWith("wlan")) {
                    continue;
                }
                JsonObject oItem = new JsonObject();
                oItem.append("name", name);
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                byte[] mac = ni.getHardwareAddress();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; mac != null && i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], ""));
                }
                oItem.append("mac", sb.toString());
                while (ips.hasMoreElements()) {
                    InetAddress ia = ips.nextElement();
                    if (ia instanceof Inet4Address) {
                        if (ia.getHostAddress().toString().startsWith("127")) {
                            continue;
                        } else {
                            oItem.append("ip", ia.getHostAddress());
                            items.add(oItem);
                            break;
                        }
                    }
                }
            }
            Collections.sort(items, (o1, o2) -> {
                String n1 = o1.getString("name");
                String n2 = o2.getString("name");
                return n1.compareTo(n2);
            });
            if (items.size() == 0) {
                oResult.append("ip", "").append("mac", "");
            } else {
                oResult.append("ip", items.get(0).getString("ip")).append("mac", items.get(0).getString("mac"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oResult;
    }

    /**
     * 获取一个唯一性的长id
     *
     * @return
     */
    public static String newId() {
        ObjectId id = new ObjectId();
        return id.toString();
    }


    public static String append(Object... strs) {
        if (strs == null || strs.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Object str : strs) {
            sb.append(str);
        }
        return sb.toString();
    }


    public static List<String> toList(String... names) {
        return Arrays.stream(names).collect(Collectors.toList());
    }


    public static String[] split(String content, String chars) {
        String[] values = content.split(chars);
        List<String> results = new ArrayList();
        for (int i = 0; i < values.length; i++) {
            if (StringUtil.isEmpty(values[i])) continue;
            results.add(values[i]);
        }
        values = new String[results.size()];
        results.toArray(values);
        return values;
    }


    public static String splitContent(String content, char... chars) {
        if (StringUtils.isEmpty(content))
            return "";
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            boolean isBreak = Character.isSpaceChar(c) || c == '\n' || c == '\t' || c == '\r' || c == ' ';
            for (int t = 0; !isBreak && t < chars.length; t++) {
                if (c == chars[t]) {
                    isBreak = true;
                    break;
                }
            }
            if (!isBreak) {
                buffer.append(c);
                continue;
            }
            if (buffer.length() > 0 && (i < content.length() - 1) && buffer.charAt(buffer.length() - 1) != SPLITCHAR) {
                buffer.append(SPLITCHAR);
            }
        }
        return buffer.toString();

    }

    public static ArrayList<String> formatLocations(String content) {
        String token = "";
        ArrayList<String> result = new ArrayList();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            switch (c) {
                case '→':
                case '、':
                case '。':
                case '-':
                case '/':
                case SPLITCHAR:
                    if (token.length() > 0) {
                        result.add(token);
                    }
                    token = "";
                    break;
                default:
                    token += c;
                    break;
            }
        }
        if (token.length() > 0) {
            result.add(token.trim());
        }
        return result;
    }
}
