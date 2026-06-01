package com.amanoteam.nz.library;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class QueryTest {

    @Test
    void createAndFree() {
        try (Query q = new Query()) {
            assertNotNull(q);
        }
    }

    @Test
    void addStringAndRetrieve() {
        try (Query q = new Query()) {
            q.add("name", "value");
            assertEquals("value", q.getString("name"));
        }
    }

    @Test
    void updateExistingString() {
        try (Query q = new Query()) {
            q.add("key", "old");
            q.add("key", "new");
            assertEquals("new", q.getString("key"));
        }
    }

    @Test
    void addIntAndRetrieve() {
        try (Query q = new Query()) {
            q.add("count", 42L);
            assertEquals(42L, q.getInt("count"));
        }
    }

    @Test
    void addUintAndRetrieve() {
        try (Query q = new Query()) {
            q.addUint("size", 100L);
            assertEquals(100L, q.getUint("size"));
        }
    }

    @Test
    void addFloatAndRetrieve() {
        try (Query q = new Query()) {
            q.add("pi", 3.14);
            assertEquals(3.14, q.getFloat("pi"), 1e-10);
        }
    }

    @Test
    void boolTrueValues() {
        try (Query q = new Query()) {
            q.add("v1", "true");
            q.add("v2", "TRUE");
            q.add("v3", "True");
            q.add("v4", "1");
            q.add("v5", "yes");
            q.add("v6", "YES");
            assertEquals(true, q.getBool("v1"));
            assertEquals(true, q.getBool("v2"));
            assertEquals(true, q.getBool("v3"));
            assertEquals(true, q.getBool("v4"));
            assertEquals(true, q.getBool("v5"));
            assertEquals(true, q.getBool("v6"));
        }
    }

    @Test
    void boolFalseValues() {
        try (Query q = new Query()) {
            q.add("v1", "false");
            q.add("v2", "FALSE");
            q.add("v3", "False");
            q.add("v4", "0");
            q.add("v5", "no");
            assertEquals(false, q.getBool("v1"));
            assertEquals(false, q.getBool("v2"));
            assertEquals(false, q.getBool("v3"));
            assertEquals(false, q.getBool("v4"));
            assertEquals(false, q.getBool("v5"));
        }
    }

    @Test
    void boolNonexistentReturnsNull() {
        try (Query q = new Query()) {
            assertNull(q.getBool("nonexistent"));
        }
    }

    @Test
    void loadFromString() {
        try (Query q = new Query()) {
            q.loadString("key1=val1&key2=val2");
            assertEquals("val1", q.getString("key1"));
            assertEquals("val2", q.getString("key2"));
        }
    }

    @Test
    void getItemByIndex() {
        try (Query q = new Query()) {
            q.add("a", "1");
            q.add("b", "2");

            QueryParam item0 = q.getItem(0);
            assertNotNull(item0);
            assertEquals("1", item0.getString());

            QueryParam item1 = q.getItem(1);
            assertNotNull(item1);
            assertEquals("2", item1.getString());
        }
    }

    @Test
    void getItemOutOfBoundsReturnsNull() {
        try (Query q = new Query()) {
            assertNull(q.getItem(0));
            assertNull(q.getItem(99));
        }
    }

    @Test
    void dumpString() {
        try (Query q = new Query()) {
            q.add("key", "value");
            String dumped = q.dumpString();
            assertTrue(dumped.contains("key=value"));
        }
    }

    @Test
    void loadEnviron() {
        try (Query q = new Query()) {
            q.loadEnviron();
            assertNotNull(q.getString("PATH"));
        }
    }

    @Test
    void loadFileFailsOnNonexistentFile() {
        try (Query q = new Query()) {
            assertThrows(RuntimeException.class, () ->
                q.loadFile("/nonexistent/file.conf"));
        }
    }

    @Test
    void nonexistentKeyReturnsNull() {
        try (Query q = new Query()) {
            assertNull(q.getString("nonexistent"));
        }
    }

    @Test
    void chainedAdds() {
        try (Query q = new Query()) {
            q.add("a", "1").add("b", "2").add("c", "3");
            assertEquals("1", q.getString("a"));
            assertEquals("2", q.getString("b"));
            assertEquals("3", q.getString("c"));
        }
    }

    @Test
    void closedQueryThrowsOnUse() {
        Query q = new Query();
        q.close();
        assertThrows(IllegalStateException.class, () -> q.add("k", "v"));
        assertThrows(IllegalStateException.class, () -> q.getString("k"));
    }

    @Test
    void doubleCloseDoesNotThrow() {
        Query q = new Query();
        q.close();
        q.close();
    }

    @Test
    void customSeparator() {
        try (Query q = new Query(';', ":")) {
            q.add("a", "1").add("b", "2");
            String dumped = q.dumpString();
            assertTrue(dumped.contains("a:1") && dumped.contains("b:2"));
        }
    }

    @Test
    void queryParamFromItem() {
        try (Query q = new Query()) {
            q.add("key", "42");
            QueryParam param = q.getItem(0);
            assertNotNull(param);
            assertEquals("42", param.getString());
            assertEquals(42, param.getInt());
            assertEquals(42.0, param.getFloat(), 1e-10);
        }
    }

    @Test
    void dumpFile() throws Exception {
        java.nio.file.Path tmp = java.nio.file.Files.createTempFile("query", ".txt");
        try (Query q = new Query()) {
            q.add("a", "1");
            q.dumpFile(tmp.toString());
            String content = new String(java.nio.file.Files.readAllBytes(tmp));
            assertTrue(content.contains("a=1"));
        } finally {
            java.nio.file.Files.deleteIfExists(tmp);
        }
    }
}
