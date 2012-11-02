package se.vgregion.portal.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Properties;

/**
 * @author Patrik Bergström
 */
public class LogoffFilter implements Filter {

    private String logoffRedirectUrl;
    private final File path = new File(System.getProperty("user.home") + "/.rp/logoff");
    private final File file = new File(path, "logoff.properties");

    public LogoffFilter() {
        if (!path.isDirectory()) {
            System.out.println("Path " + path.getAbsolutePath() + " does not exist so create it.");
            path.mkdirs();

            createTheFile(file);

            throw new IllegalStateException("You must edit the file " + file.getAbsolutePath() + " before we can"
                    + " continue.");
        } else {
            if (!file.exists()) {
                System.out.println("The file " + file.getAbsolutePath() + " does not exist.");
                createTheFile(file);

                throw new IllegalStateException("You must edit the file " + file.getAbsolutePath() + " before we can"
                        + " continue.");
            } else {
                logoffRedirectUrl = loadProperty(file);
            }
        }
    }

    private String loadProperty(File file) {
        Properties properties = new Properties();
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            properties.load(bis);

            return (String) properties.get("logoff.redirect.url");

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

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        String newLogoffRedirectUrl = loadProperty(file);
                        if (!newLogoffRedirectUrl.equals(logoffRedirectUrl)) {
                            System.out.println("Change logoffRedirectUrl to " + newLogoffRedirectUrl + ".");
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
        System.out.println("Create the properties file.");
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

        //httpServletResponse.sendRedirect(logoffRedirectUrl);

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(
                "se/vgregion/portal/filter/logoff.html");
        BufferedInputStream bis = new BufferedInputStream(resourceAsStream);

        ServletOutputStream sos = httpServletResponse.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(sos);

        byte[] buf = new byte[1024];

        int i;

        while ((i = bis.read(buf)) != -1) {
            bos.write(buf, 0, i);
        }

        bos.close();
        sos.close();
        bis.close();
        resourceAsStream.close();
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
}
