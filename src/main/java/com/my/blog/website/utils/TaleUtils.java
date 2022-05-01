package com.my.blog.website.utils;

import com.my.blog.website.constant.WebConst;
import com.my.blog.website.controller.admin.AttachController;
import com.my.blog.website.exception.TipException;
import com.my.blog.website.model.Vo.UserVo;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.awt.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * <p>Tale工具类
 *
 * @author Zhang Zhe
 * @date 2021/2/21
 */
public class TaleUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaleUtils.class);

    private volatile static DataSource newDataSource;

    /**
     * 一个月
     */
    private static final int one_month = 30 * 24 * 60 * 60;

    /**
     * 匹配邮箱正则
     */
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SLUG_REGEX = Pattern.compile("^[A-Za-z0-9_-]{5,100}$", Pattern.CASE_INSENSITIVE);

    /**
     * markdown解析器
     */
    private static Parser parser = Parser.builder().build();

    /**
     * 获取文件所在目录
     */
    private static String location = TaleUtils.class.getClassLoader().getResource("").getPath();

    /**
     * linux中储存文件的路径
     */
    private static String uploadFilePath = "/usr/local/blog-website/file/";




    /**
     * 判断是否是邮箱
     *
     * @param emailStr
     * @return
     */
    public static boolean isEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    /**
     * jdbc:mysql://127.0.0.1:3306/tale?useUnicode=true&characterEncoding=utf-8&useSSL=false 保存jdbc数据到文件中
     *
     * @param url      数据库连接地址 127.0.0.1:3306
     * @param dbName   数据库名称
     * @param userName 用户
     * @param password 密码
     */
    public static void updateJDBCFile(String url, String dbName, String userName, String password) {
        LOGGER.info("Enter updateJDBCFile method");
        Properties props = new Properties();
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream("application-jdbc.properties");
            props.setProperty("spring.datasource.url", url);
            props.setProperty("spring.datasource.dbname", dbName);
            props.setProperty("spring.datasource.username", userName);
            props.setProperty("spring.datasource.password", password);
            props.setProperty("spring.datasource.driver-class-name", "com.mysql.jdbc.Driver");
            props.store(fos, "update jdbc info.");
        } catch (IOException e) {
            LOGGER.error("updateJDBCFile method fail:{}", e.getMessage());
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        LOGGER.info("Exit updateJDBCFile method");
    }

    /**
     * 获取properties配置数据,
     *
     * @param fileName 文件名 如 application-jdbc.properties来自jar中
     * @return
     */
    public static Properties getPropFromJar(String fileName) {
        Properties properties = new Properties();
        try {
//            默认是classPath路径
            InputStream resourceAsStream = TaleUtils.class.getClassLoader().getResourceAsStream(fileName);
            if (resourceAsStream == null) {
                throw new TipException("get resource from path fail");
            }
            properties.load(resourceAsStream);
        } catch (TipException | IOException e) {
            LOGGER.error("get properties file fail={}", e.getMessage());
        }
        return properties;
    }

    /**
     * @param fileName 获取jar外部的文件
     * @return 返回属性
     */
    private static Properties getPropFromFile(String fileName) {
        Properties properties = new Properties();
        try {
            // 默认是classPath路径
            InputStream resourceAsStream = new FileInputStream(fileName);
            properties.load(resourceAsStream);
        } catch (TipException | IOException e) {
            LOGGER.error("get properties file fail={}", e.getMessage());
        }
        return properties;
    }

    /**
     * md5加密
     *
     * @param source 数据源
     * @return 加密字符串
     */
    public static String MD5encode(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ignored) {
        }
        byte[] encode = messageDigest.digest(source.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte anEncode : encode) {
            String hex = Integer.toHexString(0xff & anEncode);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 获取新的数据源
     *
     * @return
     */
    public static DataSource getNewDataSource() {
        if (newDataSource == null) {
            synchronized (TaleUtils.class) {
                if (newDataSource == null) {
                    DriverManagerDataSource managerDataSource = new DriverManagerDataSource();
                    //        TODO 对不同数据库支持
                    String driver = "com.mysql.jdbc.Driver";
                    String url = "jdbc:mysql://localhost:3306/blog?useSSL=false&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&autoReconnect=true";
                    String username = "root";
                    String password = "3333";
                    managerDataSource.setDriverClassName(driver);
                    managerDataSource.setUrl(url);
                    managerDataSource.setUsername(username);
                    managerDataSource.setPassword(password);
                    newDataSource = managerDataSource;
                }
            }
        }
        return newDataSource;
    }

    /**
     * 返回当前登录用户
     *
     * @return
     */
    public static UserVo getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (null == session) {
            return null;
        }
        return (UserVo) session.getAttribute(WebConst.LOGIN_SESSION_KEY);
    }


    /**
     * 获取cookie中的用户id
     *
     * @param request
     * @return
     */
    public static Integer getCookieUid(HttpServletRequest request) {
        if (null != request) {
            Cookie cookie = cookieRaw(WebConst.USER_IN_COOKIE, request);
            if (cookie != null && cookie.getValue() != null) {
                try {
                    String uid = Tools.deAes(cookie.getValue(), WebConst.AES_SALT);
                    return StringUtils.isNotBlank(uid) && Tools.isNumber(uid) ? Integer.valueOf(uid) : null;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * 从cookies中获取指定cookie
     *
     * @param name    名称
     * @param request 请求
     * @return cookie
     */
    private static Cookie cookieRaw(String name, HttpServletRequest request) {
        javax.servlet.http.Cookie[] servletCookies = request.getCookies();
        if (servletCookies == null) {
            return null;
        }
        for (javax.servlet.http.Cookie c : servletCookies) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    /**
     * 设置记住密码cookie
     *
     * @param response
     * @param uid
     */
    public static void setCookie(HttpServletResponse response, Integer uid) {
        try {
            String val = Tools.enAes(uid.toString(), WebConst.AES_SALT);
            boolean isSSL = false;
            Cookie cookie = new Cookie(WebConst.USER_IN_COOKIE, val);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 30);
            cookie.setSecure(isSSL);
            response.addCookie(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 提取html中的文字
     *
     * @param html
     * @return
     */
    public static String htmlToText(String html) {
        if (StringUtils.isNotBlank(html)) {
            return html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ");
        }
        return "";
    }

    /**
     * markdown转换为html
     *
     * @param markdown
     * @return
     */
    public static String mdToHtml(String markdown) {
        if (StringUtils.isBlank(markdown)) {
            return "";
        }
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String content = renderer.render(document);
        content = Commons.emoji(content);

        // TODO 支持网易云音乐输出
//        if (TaleConst.BCONF.getBoolean("app.support_163_music", true) && content.contains("[mp3:")) {
//            content = content.replaceAll("\\[mp3:(\\d+)\\]", "<iframe frameborder=\"no\" border=\"0\" marginwidth=\"0\" marginheight=\"0\" width=350 height=106 src=\"//music.163.com/outchain/player?type=2&id=$1&auto=0&height=88\"></iframe>");
//        }
        // 支持gist代码输出
//        if (TaleConst.BCONF.getBoolean("app.support_gist", true) && content.contains("https://gist.github.com/")) {
//            content = content.replaceAll("&lt;script src=\"https://gist.github.com/(\\w+)/(\\w+)\\.js\">&lt;/script>", "<script src=\"https://gist.github.com/$1/$2\\.js\"></script>");
//        }
        return content;
    }

    /**
     * 退出登录状态
     *
     * @param session
     * @param response
     */
    public static void logout(HttpSession session, HttpServletResponse response) {
        session.removeAttribute(WebConst.LOGIN_SESSION_KEY);
        Cookie cookie = new Cookie(WebConst.USER_IN_COOKIE, "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        try {
            response.sendRedirect(Commons.site_url());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 替换HTML脚本
     *
     * @param value
     * @return
     */
    public static String cleanXSS(String value) {
        //You'll need to remove the spaces from the html entities below
        value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
        value = value.replaceAll("'", "&#39;");
        value = value.replaceAll("eval\\((.*)\\)", "");
        value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        value = value.replaceAll("script", "");
        return value;
    }

    /**
     * 过滤XSS注入
     *
     * @param value
     * @return
     */
    public static String xssUtil(String value) {
        if (value != null) {
            //删除script标签
            Pattern compile = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
            value = compile.matcher(value).replaceAll("");
            compile = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = compile.matcher(value).replaceAll("");
            compile = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = compile.matcher(value).replaceAll("");
            // 删除单个的 </script> 标签
            compile = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
            value = compile.matcher(value).replaceAll("");
            // 删除单个的<script ...> 标签
            compile = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = compile.matcher(value).replaceAll("");
            // 避免 eval(...) 形式表达式
            compile = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = compile.matcher(value).replaceAll("");
            // 避免 e­xpression(...) 表达式
            compile = Pattern.compile("e­xpression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = compile.matcher(value).replaceAll("");
            // 避免 javascript: 表达式
            compile = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
            value = compile.matcher(value).replaceAll("");
            // 避免 vbscript:表达式
            compile = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
            value = compile.matcher(value).replaceAll("");
            value = cleanEventAttact(value);
            //替换特殊标签
            value = value
                    .replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("'","\\'")
                    .replaceAll("\"","\"")
                    .replaceAll("'","|")
                    .replaceAll("\\\\","~")
                    .replaceAll("\\|","\\|")
                    .replaceAll(";","\\;");
        }
        return value;
    }

    /**
     * 屏蔽页面注入的所有html事件攻击
     *
     * @param value
     * @return
     */
    public static String cleanEventAttact(String value) {
        //避免οnclick= 表达式
        Pattern compile = Pattern.compile("onafterprint(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onbeforeprint(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onbeforeunload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onhaschange(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onmessage(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onoffline(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("ononline(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onpagehide(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onpageshow(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onpopstate(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onredo(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onresize(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onstorage(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onundo(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onunload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onblur(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onchange(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("oncontextmenu(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onfocus(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onformchange(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onforminput(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("oninput(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("oninvalid(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onreset(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onselect(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onsubmit(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onkeydown(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onkeypress(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onkeyup(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("ondblclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("ondrag(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("ondragend(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("ondragenter(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("ondragleave(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("ondragover(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("ondragstart(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("ondrop(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onmousedown(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onmousemove(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onmouseout(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onmouseover(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onmouseenter(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onmouseup(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onmousewheel(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        compile = Pattern.compile("onscroll(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = compile.matcher(value).replaceAll("");
        // 页面屏蔽document字样
        value = value.replace("document", "");
        // 页面屏蔽alert字样
        value = value.replace("alert", "");
        return value;
    }


    /**
     * 判断是否是合法路径
     *
     * @param slug
     * @return
     */
    public static boolean isPath(String slug) {
        if (StringUtils.isNotBlank(slug)) {
            if (slug.contains("/") || slug.contains(" ") || slug.contains(".")) {
                return false;
            }
            Matcher matcher = SLUG_REGEX.matcher(slug);
            return matcher.find();
        }
        return false;
    }

    public static String getFileKey(String name) {
        String prefix = "/upload/" + DateKit.dateFormat(new Date(), "yyyy/MM");
        if (!new File(AttachController.CLASSPATH + prefix).exists()) {
            new File(AttachController.CLASSPATH + prefix).mkdirs();
        }

        name = StringUtils.trimToNull(name);
        if (name == null) {
            return prefix + "/" + UUID.UU32() + "." + null;
        } else {
            name = name.replace('\\', '/');
            name = name.substring(name.lastIndexOf("/") + 1);
            int index = name.lastIndexOf(".");
            String ext = null;
            if (index >= 0) {
                ext = StringUtils.trimToNull(name.substring(index));
            }
            return prefix +"/"+ UUID.UU32() + (ext == null ? null : (ext));
        }
    }

    /**
     * 判断文件是否是图片类型
     *
     * @param imageFile
     * @return
     */
    public static boolean isImage(InputStream imageFile) {
        try {
            Image img = ImageIO.read(imageFile);
            if (img == null || img.getWidth(null) <= 0 || img.getHeight(null) <= 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 随机数
     *
     * @param size
     * @return
     */
    public static String getRandomNumber(int size) {
        String num = "";

        for (int i = 0; i < size; ++i) {
            double a = Math.random() * 9.0D;
            a = Math.ceil(a);
            int randomNum = (new Double(a)).intValue();
            num = num + randomNum;
        }

        return num;
    }

    /**
     * 返回文件保存路径
     *
     * @return
     */
    public static String getUploadFilePath() {
        // 开发环境是windows,所以使用不同的路径.若实际部署可删除这个if
        if ((System.getProperty("os.name").startsWith("Windows"))){
            return new File("").getAbsolutePath()+"\\";
        }
        return TaleUtils.uploadFilePath;
    }


    /**
     * 根据操作系统返回不同的根路径
     * @return
     */
    public static String getRootByOS(){
        if ((System.getProperty("os.name").startsWith("Windows"))){
            return "\\";
        }
        return "/";
    }
}
