package se.vgregion.portal.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Properties;

/**
 * @author Patrik Bergström
 */
public class LogoffFilter implements Filter {

    private final File path;
    private final File file;

    private String logoffRedirectUrl;
    private byte[] logoffHtml;
    private final Properties properties = new Properties();

    public LogoffFilter() {
        //Default values
        this.path = new File(System.getProperty("user.home") + "/.rp/logoff");
        this.file = new File(path, "logoff.properties");

        initAll();
    }

    public LogoffFilter(File path, File file) {
        this.path = path;
        this.file = file;

        initAll();
    }

    void initAll() {
        if (!path.isDirectory()) {
            print("Path " + path.getAbsolutePath() + " does not exist so create it.");
            path.mkdirs();

            createTheFile(file);

            throw new IllegalStateException("You must edit the file " + file.getAbsolutePath() + " before we can"
                    + " continue.");
        } else {
            if (!file.exists()) {
                print("The file " + file.getAbsolutePath() + " does not exist.");
                createTheFile(file);

                throw new IllegalStateException("You must edit the file " + file.getAbsolutePath() + " before we can"
                        + " continue.");
            } else {
                initProperties(file);
                logoffRedirectUrl = properties.getProperty("logoff.redirect.url");
            }
        }

        initLogoffHtml();
    }

    void initLogoffHtml() {

        try {
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(
                    "se/vgregion/portal/filter/logoff.html");
            BufferedInputStream bis = new BufferedInputStream(resourceAsStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buf = new byte[1024];

            int i;

            while ((i = bis.read(buf)) != -1) {
                baos.write(buf, 0, i);
            }

            logoffHtml = baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    synchronized void initProperties(File file) {
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            properties.load(bis);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeClosables(bis, fis);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        String checkIntervalConfig = filterConfig.getInitParameter("check.interval");

        // 20 seconds is default.
        final int checkInterval = checkIntervalConfig == null ? 20000 : Integer.valueOf(checkIntervalConfig);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        initProperties(file);
                        Thread.sleep(checkInterval);
                        String newLogoffRedirectUrl = properties.getProperty("logoff.redirect.url");
                        if (!newLogoffRedirectUrl.equals(logoffRedirectUrl)) {
                            print("Change logoffRedirectUrl to " + newLogoffRedirectUrl + ".");
                            logoffRedirectUrl = newLogoffRedirectUrl;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void createTheFile(File file) {
        // Also create the file and write some helping text.
        print("Create the properties file.");
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        try {
            file.createNewFile();
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("logoff.redirect.url{put equals sign here}{put the redirect url here}");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeClosables(bufferedWriter, fileWriter);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        if (isExternal((HttpServletRequest) request)) {
            httpServletResponse.sendRedirect(logoffRedirectUrl);
        } else {
            ServletOutputStream sos = null;
            BufferedOutputStream bos = null;
            try {
                sos = httpServletResponse.getOutputStream();
                bos = new BufferedOutputStream(sos);

                bos.write(logoffHtml);
            } finally {
                closeClosables(bos, sos);
            }
        }
    }

    boolean isExternal(HttpServletRequest request) {
        String header = request.getHeader("x-forwarded-for");
        String[] externalIps = properties.getProperty("external.ips").replaceAll(" ", "").split(",");
        if (header != null) {
            // Iterate over the ip:s. We'll find a match if the user is located externally.
            for (String ip : externalIps) {
                if (header.contains(ip)) { // String.contains(...) since the header value may be a comma-separated list.
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void destroy() {

    }

    private void closeClosables(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void print(String string) {
        System.out.println(this.getClass().getName() + ": " + string);
    }
}
