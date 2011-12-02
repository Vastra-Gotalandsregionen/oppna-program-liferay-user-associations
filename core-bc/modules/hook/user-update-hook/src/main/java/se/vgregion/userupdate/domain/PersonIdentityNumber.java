/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */
package se.vgregion.userupdate.domain;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Anders Asplund - Logica
 */
public final class PersonIdentityNumber {
    public enum Gender {
        /**
         * Gender types.
         */
        MALE, FEMALE;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonIdentityNumber.class);
    private static final String VALID_PERSONALNUMBER = "[\\d]{12}";
    private static final String BIRTHDAY_FORMAT = "yyyyMMdd";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(BIRTHDAY_FORMAT);

    private final String personIdentityNumber;
    private final Date birthday;
    private final int securityNumber;
    private final Gender gender;
    private static final int GENDER_NUMBER_POSITION = 10;

    /**
     * Creates new PersonIdentityNumber from a string representation of a person identity number. If
     * personIdentityNumber is not a valid person identity number it will throw an {@link IllegalArgumentException}
     *
     * @param personIdentityNumber
     *            the string to parse
     */
    public PersonIdentityNumber(String personIdentityNumber) {
        this.personIdentityNumber = personIdentityNumber;
        validate(personIdentityNumber);
        birthday = parseBirthday();
        securityNumber = parseSecurityNumber();
        gender = parseGender();
    }

    private static void validate(String personIdentityNumber) {
        Validate.notNull(personIdentityNumber);
        validatePattern(personIdentityNumber);
        validateDate(personIdentityNumber);
    }

    private static void validatePattern(String personIdentityNumber) {
        if (!personIdentityNumber.matches(VALID_PERSONALNUMBER)) {
            LOGGER.warn("Trying to parse illegal personal number format.");
            throw new IllegalArgumentException("Illegal personal number. [" + BIRTHDAY_FORMAT + "xxxx]");
        }
    }

    private static void validateDate(String personIdentityNumber) {
        try {
            DATE_FORMAT.setLenient(false);
            DATE_FORMAT.parse(personIdentityNumber.substring(0, BIRTHDAY_FORMAT.length()));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Not a valid date", e);
        }
    }

    /**
     * Validates a string to see if its a valid Swedish person identity number.
     *
     * @param personIdentityNumber
     *            the string to validate
     * @return true if personIdentityNumber is a valid Swedish person identity number. False otherwise.
     */
    public static boolean isValid(String personIdentityNumber) {
        try {
            validate(personIdentityNumber);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private Date parseBirthday() {
        try {
            return DATE_FORMAT.parse(personIdentityNumber.substring(0, BIRTHDAY_FORMAT.length()));
        } catch (ParseException e) {
            LOGGER.warn("Trying to parse illegal birthday format.");
            throw new IllegalArgumentException("Illegal birthday format. [" + BIRTHDAY_FORMAT + "]", e);
        }
    }

    private int parseSecurityNumber() {
        return Integer.parseInt(personIdentityNumber.substring(BIRTHDAY_FORMAT.length()));
    }

    private Gender parseGender() {
        if (Integer.parseInt(String.valueOf(personIdentityNumber.charAt(GENDER_NUMBER_POSITION))) % 2 == 0) {
            return Gender.FEMALE;
        } else {
            return Gender.MALE;
        }
    }

    public Date getBirthday() {
        return new Date(birthday.getTime());
    }

    public int getSecurityNumber() {
        return securityNumber;
    }

    public String getPersonIdentityNumber() {
        return personIdentityNumber;
    }

    public Gender getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return personIdentityNumber;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(personIdentityNumber).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PersonIdentityNumber)) {
            return false;
        }

        PersonIdentityNumber other = (PersonIdentityNumber) obj;
        return new EqualsBuilder().append(personIdentityNumber, other.personIdentityNumber).isEquals();
    }
}
