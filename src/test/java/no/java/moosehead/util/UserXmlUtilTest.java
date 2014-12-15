package no.java.moosehead.util;

import org.fest.assertions.Assertions;
import org.junit.Test;

import java.io.*;

import static org.fest.assertions.Assertions.assertThat;

public class UserXmlUtilTest {
    @Test
    public void shouldReadUserInfo() throws Exception {
        String xmlex = toString(getClass().getClassLoader().getResourceAsStream("testxml.xml"));
        UserInfo userInfo = UserXmlUtil.read(xmlex, "988");
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.username).isEqualTo("dummyuname");

    }

    private static String toString(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char)c);
            }
            return result.toString();
        }
    }
}
