package org.ohdsi.webapi.shiro.realms;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbcAuthRealmTest {
    // Generated and verified with Spring Security 5.8.16, before removing that dependency.
    private static final String SPRING_HASH = "$2a$04$acVg1IYFu8mgx/2qsfLot.U/AMTNHpnffnrKnJdma4TGoQNvkzoHi";

    @Test
    public void acceptsExistingSpringHashes() throws Exception {
        for (String version : new String[] {"2a", "2b", "2y"}) {
            assertNotNull(realm(SPRING_HASH.replace("2a", version))
                    .doGetAuthenticationInfo(new UsernamePasswordToken("user", "password")));
        }
    }

    @Test
    public void acceptsExistingUnicodeHash() throws Exception {
        assertNotNull(realm("$2a$04$/mvdYvNua4qmSLgPK1nJHe9LPk7Dk7.ii0ezbjRa62fH9igJQG/JO")
                .doGetAuthenticationInfo(new UsernamePasswordToken("user", "pässwörd")));
    }

    @Test
    public void preservesExistingEmptyPasswordHashBehavior() throws Exception {
        assertNotNull(realm("$2a$04$r.FQHdNpNs9.TjzZ/Kjgle850oRnPA6gaZ9W/Ci9PLFKaY9PgKVvK")
                .doGetAuthenticationInfo(new UsernamePasswordToken("user", "")));
    }

    @Test
    public void accepts71And72BytePasswords() throws Exception {
        for (int length : new int[] {71, 72}) {
            String password = repeat('a', length);
            assertNotNull(realm(hash(password)).doGetAuthenticationInfo(new UsernamePasswordToken("user", password)));
        }
    }

    @Test
    public void rejects73BytePasswordEvenWhenPrefixMatches() throws Exception {
        rejects(hash(repeat('a', 72)), repeat('a', 72) + "b");
    }

    @Test
    public void enforcesUtf8BytesRatherThanCharacters() throws Exception {
        String password = repeat('é', 36);
        assertNotNull(realm(hash(password)).doGetAuthenticationInfo(new UsernamePasswordToken("user", password)));
        rejects(hash(password), password + "x");
    }

    @Test
    public void handlesSupplementaryUnicodeAtByteBoundary() throws Exception {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 18; i++) builder.appendCodePoint(0x1F600);
        String password = builder.toString(); // 36 UTF-16 units, 72 UTF-8 bytes.
        assertNotNull(realm(hash(password)).doGetAuthenticationInfo(new UsernamePasswordToken("user", password)));
        rejects(hash(password), password + "x");
    }

    @Test
    public void rejectsWrongPassword() throws Exception {
        rejects(SPRING_HASH, "incorrect");
    }

    @Test
    public void rejectsMissingPassword() throws Exception {
        rejects(SPRING_HASH, null);
    }

    @Test
    public void rejectsMalformedAndUnsupportedHashes() throws Exception {
        for (String stored : new String[] {null, "", "not-a-hash", "$2a$04$invalid",
                SPRING_HASH.replace("2a", "2z"), SPRING_HASH.replace("$04$", "$03$"),
                SPRING_HASH.replace("$04$", "$32$")}) {
            rejects(stored, "password");
        }
    }

    private static String hash(String password) {
        return OpenBSDBCrypt.generate(password.toCharArray(), new byte[16], 4);
    }

    private static String repeat(char c, int length) {
        char[] chars = new char[length];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    private static void rejects(String hash, String password) throws Exception {
        try {
            realm(hash).doGetAuthenticationInfo(new UsernamePasswordToken("user", password));
            fail("Authentication must fail");
        } catch (AuthenticationException expected) {
            // Malformed hashes and passwords must fail closed, not cause a server error.
        }
    }

    private static JdbcAuthRealm realm(String hash) throws Exception {
        DataSource ds = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet rows = mock(ResultSet.class);
        ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        when(ds.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(rows);
        when(rows.next()).thenReturn(true, false);
        when(rows.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(1);
        when(rows.getString(1)).thenReturn(hash);
        return new JdbcAuthRealm(ds, "select password from users where username = ?");
    }
}
