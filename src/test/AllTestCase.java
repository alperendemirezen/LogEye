package test;

import model.AlertMessage;
import model.ConnectionOfClient;
import model.FilterOfClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import server.AlertSender;
import server.LogAnalyzer;
import server.LogArchiver;
import server.Server;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class AllTestCase {

    public static class LogArchiverTests {

        private LogArchiver archiver;

        @Before
        public void setUp() {
            archiver = new LogArchiver("jdbc:sqlite:test.db", "test.log", 30);
        }

        @Test
        public void testParseLogLineWithCritical() {
            String[] result = archiver.parseLogLine("2024-12-13 11:45:30 [CRITICAL] [SYS] System crash");

            assertNotNull(result);
            assertEquals("2024-12-13 11:45:30", result[0]);
            assertNotEquals("ERROR", result[1]);
            assertEquals("SYS", result[2]);
            assertEquals("System crash", result[3]);
        }
    }

    public static class LogAnalyzerTests {

        private LogAnalyzer analyzer;
        private BlockingQueue<String> logQueue;
        private BlockingQueue<AlertMessage> alertQueue;

        @Before
        public void setUp() {
            logQueue = new LinkedBlockingQueue<>();
            alertQueue = new LinkedBlockingQueue<>();
            analyzer = new LogAnalyzer(logQueue, alertQueue);
        }

        @Test
        public void testCreateAlertWithError() throws InterruptedException {
            String logLine = "2024-12-13 10:30:45 [ERROR] [Database] Connection failed";

            analyzer.createAlert(logLine);

            assertEquals(1, alertQueue.size());
            AlertMessage alert = alertQueue.take();
            assertEquals("2024-12-13 10:30:45", alert.getTimestamp());
            assertEquals("ERROR", alert.getLevel());
            assertEquals("Database", alert.getSection());
            assertEquals("Connection failed", alert.getMessage());
        }

        @Test
        public void testCreateAlertIgnoresWarning() throws InterruptedException {
            String logLine = "2024-12-13 10:00:00 [WARNING] [Network] Slow connection";

            analyzer.createAlert(logLine);

            // WARNING ignored
            assertEquals(0, alertQueue.size());

        }
    }

    public static class AlertSenderTests {

        private AlertSender sender;
        private BlockingQueue<AlertMessage> alertQueue;
        private List<ConnectionOfClient> clients;
        private SecretKey testKey;

        @Before
        public void setUp() throws Exception {
            alertQueue = new LinkedBlockingQueue<>();
            clients = new ArrayList<>();
            sender = new AlertSender(alertQueue, clients);

            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(256);
            testKey = kg.generateKey();
        }

        @Test
        public void testEncrypt() throws Exception {
            String plainText = "Test message";

            String encrypted = sender.encrypt(plainText, testKey);

            assertNotNull(encrypted);
            assertNotEquals(plainText, encrypted);
            assertTrue(encrypted.length() > 0);
        }

    }

    public static class ServerTests {

        private Server server;
        private List<ConnectionOfClient> clients;

        @Before
        public void setUp() {
            clients = new ArrayList<>();
            server = new Server(7070, clients);
        }

        @Test
        public void testParseFilter() {
            String json = "{\"levels\":[\"ERROR\"],\"sections\":[\"DB\"]}";

            FilterOfClient filter = server.parseFilter(json);

            assertNotNull(filter);
            assertEquals(1, filter.getLevels().size());
            assertEquals("ERROR", filter.getLevels().get(0));
            assertEquals("DB", filter.getSections().get(0));
        }

        @Test
        public void testGenerateSecretKey() throws Exception {
            SecretKey key = server.generateSecretKey();

            assertNotNull(key);
        }

        @Test
        public void testEncryptWithPublicKey() throws Exception {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair pair = kpg.generateKeyPair();

            byte[] data = "test-data".getBytes();
            byte[] encrypted = server.encryptWithPublicKey(data, pair.getPublic());

            assertNotNull(encrypted);
            assertTrue(encrypted.length > 0);
            assertNotEquals(data, encrypted);
        }
    }

    public static class FilterOfClientTests {

        private FilterOfClient filter;

        @Before
        public void setUp() {
            filter = new FilterOfClient();
        }

        @Test
        public void testMatchesWithOnlyLevelFilter() {
            filter.addLevelFilter("ERROR");

            AlertMessage errorMsg = new AlertMessage("2024-12-13 10:00:00", "ERROR", "DB", "Test");
            AlertMessage criticalMsg = new AlertMessage("2024-12-13 10:00:00", "CRITICAL", "DB", "Test");

            assertTrue("ERROR message should match", filter.matches(errorMsg));
            assertFalse("CRITICAL message should NOT match", filter.matches(criticalMsg));
        }

        @Test
        public void testMatchesWithOnlySectionFilter() {
            filter.addSectionFilter("DB");

            AlertMessage dbMsg = new AlertMessage("2024-12-13 10:00:00", "ERROR", "DB", "Test");
            AlertMessage sysMsg = new AlertMessage("2024-12-13 10:00:00", "ERROR", "SYS", "Test");

            assertTrue("DB message should match", filter.matches(dbMsg));
            assertFalse("SYS message should NOT match", filter.matches(sysMsg));
        }

        @Test
        public void testMatchesWithBothFilters() {
            filter.addLevelFilter("ERROR");
            filter.addSectionFilter("DB");

            AlertMessage bothMatch = new AlertMessage("2024-12-13 10:00:00", "ERROR", "DB", "Test");
            AlertMessage levelMismatch = new AlertMessage("2024-12-13 10:00:00", "CRITICAL", "DB", "Test");
            AlertMessage sectionMismatch = new AlertMessage("2024-12-13 10:00:00", "ERROR", "SYS", "Test");

            assertTrue("Both filters match", filter.matches(bothMatch));
            assertFalse("Level doesn't match", filter.matches(levelMismatch));
            assertFalse("Section doesn't match", filter.matches(sectionMismatch));
        }
    }

    public static class AlertMessageTests {
        @Test
        public void testToString() {
            AlertMessage msg = new AlertMessage("2024-12-13 10:00:00", "ERROR", "DB", "Test message");

            String expected = "AlertMessage{timestamp='2024-12-13 10:00:00', level='ERROR', section='DB', message='Test message'}";
            assertEquals(expected, msg.toString());
        }

    }

}

