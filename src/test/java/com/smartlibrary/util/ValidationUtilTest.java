package com.smartlibrary.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test void validName()              { assertNull(ValidationUtil.validateName("Alice Johnson")); }
    @Test void nullName()               { assertNotNull(ValidationUtil.validateName(null)); }
    @Test void emptyName()              { assertNotNull(ValidationUtil.validateName("")); }
    @Test void singleCharName()         { assertNotNull(ValidationUtil.validateName("A")); }

    @Test void validRegisterNumber()    { assertNull(ValidationUtil.validateRegisterNumber("STU001")); }
    @Test void nullRegisterNumber()     { assertNotNull(ValidationUtil.validateRegisterNumber(null)); }
    @Test void emptyRegisterNumber()    { assertNotNull(ValidationUtil.validateRegisterNumber("")); }
    @Test void tooShortRegisterNumber() { assertNotNull(ValidationUtil.validateRegisterNumber("AB")); }

    @Test void validEmail()             { assertNull(ValidationUtil.validateEmail("user@example.com")); }
    @Test void invalidEmailNoAt()       { assertNotNull(ValidationUtil.validateEmail("userexample.com")); }
    @Test void nullEmail()              { assertNotNull(ValidationUtil.validateEmail(null)); }
    @Test void emptyEmail()             { assertNotNull(ValidationUtil.validateEmail("")); }

    @Test void validPhone()             { assertNull(ValidationUtil.validatePhone("9876543210")); }
    @Test void nullPhone()              { assertNotNull(ValidationUtil.validatePhone(null)); }
    @Test void emptyPhone()             { assertNotNull(ValidationUtil.validatePhone("")); }
    @Test void shortPhone()             { assertNotNull(ValidationUtil.validatePhone("12345")); }

    @Test void validPassword()          { assertNull(ValidationUtil.validatePassword("Secure@123")); }
    @Test void passwordTooShort()       { assertNotNull(ValidationUtil.validatePassword("Ab1!")); }
    @Test void passwordNoLetter()       { assertNotNull(ValidationUtil.validatePassword("12345678!")); }
    @Test void passwordNoDigit()        { assertNotNull(ValidationUtil.validatePassword("SecurePass!")); }
    @Test void passwordNoSpecial()      { assertNotNull(ValidationUtil.validatePassword("SecurePass1")); }
    @Test void nullPassword()           { assertNotNull(ValidationUtil.validatePassword(null)); }

    @Test void passwordsMatch()         { assertNull(ValidationUtil.validateConfirmPassword("Pass@123", "Pass@123")); }
    @Test void passwordsMismatch()      { assertNotNull(ValidationUtil.validateConfirmPassword("Pass@123", "Different@1")); }
    @Test void confirmBlank()           { assertNotNull(ValidationUtil.validateConfirmPassword("Pass@123", "")); }

    @Test void validIsbn()              { assertNull(ValidationUtil.validateIsbn("9780134685991")); }
    @Test void nullIsbn()               { assertNull(ValidationUtil.validateIsbn(null)); }
    @Test void emptyIsbn()              { assertNull(ValidationUtil.validateIsbn("")); }
    @Test void invalidIsbn()            { assertNotNull(ValidationUtil.validateIsbn("!!!")); }
}