package se.vgregion.portal.filter;

import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Patrik Bergström
 */
public class LogoffFilterTest {
    private void changeUrlInFile(File file) throws IOException {
        FileWriter writer = new FileWriter(file, false);
        writer.write("logoff.redirect.url=http://newurl.se");
        writer.close();
    }

    @Test
    public void testDoFilterNotExternal() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("logoff-test.properties").getFile());
        File path = file.getParentFile();

        LogoffFilter filter = new LogoffFilter(path, file);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletResponse response = mock(HttpServletResponse.class);

        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                baos.write(b);
            }
        });

        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);

        String s = baos.toString();
        assertTrue(s.contains("Du &auml;r nu utloggad."));
    }

    @Test
    public void testDoFilterIsExternal() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("logoff-test.properties").getFile());
        File path = file.getParentFile();

        LogoffFilter filter = new LogoffFilter(path, file);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("x-forwarded-for")).thenReturn("fakjlsdfalkj,111.222.333.444");

        HttpServletResponse response = mock(HttpServletResponse.class);

        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                baos.write(b);
            }
        });

        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);

        verify(response, times(1)).sendRedirect(anyString());
    }

    // This test modifies the logoff-test.properties file and it may not work if this test is executed before the
    // others.
    @Test
    public void testInit() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("logoff-test.properties").getFile());
        File path = file.getParentFile();

        LogoffFilter filter = new LogoffFilter(path, file);

        FilterConfig config = mock(FilterConfig.class);
        when(config.getInitParameter("check.interval")).thenReturn("50");

        filter.init(config);

        Field logoffRedirectUrlField = filter.getClass().getDeclaredField("logoffRedirectUrl");
        logoffRedirectUrlField.setAccessible(true);

        String url = (String) logoffRedirectUrlField.get(filter);

        assertEquals("http://google.se", url); // According to logoff-test.properties

        // Now modify the file to change the url.
        changeUrlInFile(file);

        Thread.sleep(200);

        // Verify the field has been updated
        url = (String) logoffRedirectUrlField.get(filter);
        assertEquals("http://newurl.se", url);
    }

    @Test
    public void testCreateTheFile() {
        // This file exists. Thus we can get the path to it.
        File aFileThatExistsInAGoodLocation = new File(this.getClass().getClassLoader().getResource(
                "logoff-test.properties").getFile());
        File path = aFileThatExistsInAGoodLocation.getParentFile();

        File newFile = new File(path, "newFile.properties");
        if (newFile.exists()) {
            boolean delete = newFile.delete();
            if (!delete) {
                throw new RuntimeException("We can't verify the test if this file still exists.");
            }
        }

        // Now the newFile should be guaranteed to be non-existent.
        try {
            LogoffFilter filter = new LogoffFilter(path, newFile);
            fail();
        } catch (IllegalStateException e) {
            // If the file is just created an exception should be thrown since we haven't edited the file with
            // appropriate values.
        }

        // Verify
        assertTrue(newFile.exists());

        // Cleanup
        newFile.delete();
    }

    // Some extra test coverage :)
    @Test
    public void testDestroy() {
        File file = new File(this.getClass().getClassLoader().getResource("logoff-test.properties").getFile());
        File path = file.getParentFile();

        LogoffFilter filter = new LogoffFilter(path, file);
        filter.destroy();
    }
}
