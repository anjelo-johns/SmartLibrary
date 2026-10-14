package com.smartlibrary.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashProducesThreePartFormat() {
        String hash = PasswordUtil.hash("MyPassword1!");
        String[] parts = hash.split(":");
        assertEquals(3, parts.length, "Hash must have format iterations:salt:hash");
        assertEquals("65536", parts[0], "Iteration count must be 65536");
    }

    @Test
    void verifySucceedsForCorrectPassword() {
        String password = "Correct@Horse99";
        String hash = PasswordUtil.hash(password);
        assertTrue(PasswordUtil.verify(password, hash));
    }

    @Test
    void verifyFailsForWrongPassword() {
        String hash = PasswordUtil.hash("RightPassword1!");
        assertFalse(PasswordUtil.verify("WrongPassword1!", hash));
    }

    @Test
    void eachHashHasUniqueSalt() {
        String password = "SamePassword1!";
        String hash1 = PasswordUtil.hash(password);
        String hash2 = PasswordUtil.hash(password);
        assertNotEquals(hash1, hash2, "Two hashes of the same password must differ");
        assertTrue(PasswordUtil.verify(password, hash1));
        assertTrue(PasswordUtil.verify(password, hash2));
    }

    @Test
    void verifyReturnsFalseForNullInputs() {
        assertFalse(PasswordUtil.verify(null, "65536:abc:def"));
        assertFalse(PasswordUtil.verify("password", null));
        assertFalse(PasswordUtil.verify(null, null));
    }

    @Test
    void verifyReturnsFalseForMalformedHash() {
        assertFalse(PasswordUtil.verify("password", "not:a:valid:hash:at:all"));
        assertFalse(PasswordUtil.verify("password", "twocolons"));
        assertFalse(PasswordUtil.verify("password", ""));
    }
}