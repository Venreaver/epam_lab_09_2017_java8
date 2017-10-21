package com.epam;

import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @see <a href="https://habrahabr.ru/post/274811/">Java и время, часть 1</a>
 * @see <a href="https://habrahabr.ru/post/274905/">Java и время, часть 2</a>
 */
class DateExampleTest {

    @Test
    void testSunday() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2017, Calendar.OCTOBER, 20, 12, 30, 0);

        assertEquals(Calendar.FRIDAY, calendar.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    void getDay() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2017, Calendar.OCTOBER, 20, 12, 30, 0);

        assertEquals(6, calendar.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    void getMonth() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2017, Calendar.OCTOBER, 20, 12, 30, 0);

        assertEquals(Calendar.OCTOBER, calendar.get(Calendar.MONTH));
    }

    @Test
    void setMillis() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2017, Calendar.OCTOBER, 20, 12, 30, 0);
        calendar.setTimeInMillis(calendar.getTimeInMillis() / 1000 * 1000);

        assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    void testFormat() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("MSK");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(tz);

        Date moment = dateFormat.parse("2005-03-27 01:30:00");
        assertEquals("2005-03-27 01:30:00", dateFormat.format(moment));
    }

    @Test
    void testTimeZones() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);

        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        Date momentMSK = dateFormat.parse("2016-01-01 16:30:00");

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date momentUTC = dateFormat.parse("2016-01-01 13:30:00");

        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        Date momentCET = dateFormat.parse("2016-01-01 14:30:00");

        assertTrue(momentCET.equals(momentMSK));
        assertTrue(momentMSK.equals(momentUTC));
    }

    @Test
    void testTzMoscow() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");

        System.out.println(tz.getRawOffset());
        System.out.println(tz.getOffset(System.currentTimeMillis()));
        System.out.println(tz.useDaylightTime());

        System.out.println(tz.getDisplayName(false, TimeZone.LONG, Locale.ENGLISH));
        System.out.println(tz.getDisplayName(false, TimeZone.SHORT, Locale.ENGLISH));
        System.out.println(tz.getDisplayName(true, TimeZone.LONG, Locale.ENGLISH));
        System.out.println(tz.getDisplayName(true, TimeZone.SHORT, Locale.ENGLISH));

        System.out.println(tz.getDisplayName(false, TimeZone.LONG, Locale.FRENCH));
        System.out.println(tz.getDisplayName(false, TimeZone.SHORT, Locale.FRENCH));
        System.out.println(tz.getDisplayName(true, TimeZone.LONG, Locale.FRENCH));
        System.out.println(tz.getDisplayName(true, TimeZone.SHORT, Locale.FRENCH));
    }


    @Test
    void testTzParis() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("Europe/Paris");

        System.out.println(tz.getRawOffset());
        System.out.println(tz.getOffset(System.currentTimeMillis()));
        System.out.println(tz.useDaylightTime());

        System.out.println(tz.getDisplayName(false, TimeZone.LONG, Locale.ENGLISH));
        System.out.println(tz.getDisplayName(false, TimeZone.SHORT, Locale.ENGLISH));
        System.out.println(tz.getDisplayName(true, TimeZone.LONG, Locale.ENGLISH));
        System.out.println(tz.getDisplayName(true, TimeZone.SHORT, Locale.ENGLISH));

        System.out.println(tz.getDisplayName(false, TimeZone.LONG, Locale.FRENCH));
        System.out.println(tz.getDisplayName(false, TimeZone.SHORT, Locale.FRENCH));
        System.out.println(tz.getDisplayName(true, TimeZone.LONG, Locale.FRENCH));
        System.out.println(tz.getDisplayName(true, TimeZone.SHORT, Locale.FRENCH));
    }

    @Test
    void testGmt5() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("GMT+5");

        System.out.println(tz.getRawOffset());
        System.out.println(tz.getOffset(System.currentTimeMillis()));
        System.out.println(tz.useDaylightTime());

        System.out.println(tz.getDisplayName(false, TimeZone.LONG, Locale.ENGLISH));
        System.out.println(tz.getDisplayName(false, TimeZone.SHORT, Locale.ENGLISH));
        System.out.println(tz.getDisplayName(true, TimeZone.LONG, Locale.ENGLISH));
        System.out.println(tz.getDisplayName(true, TimeZone.SHORT, Locale.ENGLISH));

        System.out.println(tz.getDisplayName(false, TimeZone.LONG, Locale.FRENCH));
        System.out.println(tz.getDisplayName(false, TimeZone.SHORT, Locale.FRENCH));
        System.out.println(tz.getDisplayName(true, TimeZone.LONG, Locale.FRENCH));
        System.out.println(tz.getDisplayName(true, TimeZone.SHORT, Locale.FRENCH));
    }

    @Test
    void testMissing() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(tz);

        Date moment = dateFormat.parse("2005-03-27 02:30:00");
        System.out.println(moment);
    }

    @Test
    void testSummerDay() {
        TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");

        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        calendar.setTimeZone(tz);
        calendar.set(2005, Calendar.MARCH, 27, 0, 0, 0);

        Date time1 = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, +1);

        Date time2 = calendar.getTime();

        System.out.println(TimeUnit.MILLISECONDS.toHours(time2.getTime() - time1.getTime()));
    }

    @Test
    void testInstantFields() throws Exception {
        Instant instant = Clock.systemDefaultZone().instant();

        System.out.println(instant.getEpochSecond());
        System.out.println(instant.getNano());

        System.out.println(instant.toEpochMilli());
    }

    @Test
    void testLocalDateTime() throws Exception {
        ZonedDateTime zdt1 = ZonedDateTime.of(2015, 1, 10, 15, 0, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime zdt2 = ZonedDateTime.of(2015, 1, 10, 14, 0, 0, 0, ZoneId.of("Europe/London"));
        assertEquals(-1, zdt1.compareTo(zdt2));

        LocalDateTime ldt1 = zdt1.toLocalDateTime();
        LocalDateTime ldt2 = zdt2.toLocalDateTime();
        assertEquals(+1, ldt1.compareTo(ldt2));
    }

    @Test
    void testZoned1() throws Exception {
        LocalDateTime ldt = LocalDateTime.of(2015, 1, 10, 0, 0, 0, 0);
        // java.time.DateTimeException: Unable to obtain ZonedDateTime from TemporalAccessor: 2015-01-10T00:00 of type java.time.LocalDateTime
        assertThrows(DateTimeException.class, () -> ZonedDateTime.from(ldt));
    }

    @Test
    void testZoned2() throws Exception {
        LocalDateTime ldt = LocalDateTime.of(2015, 1, 10, 0, 0, 0, 0);
        ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.of("Europe/Moscow"));
    }

    @Test
    void testLenient2() throws Exception {
        // java.time.DateTimeException: Invalid date 'February 29' as '2005' is not a leap year

        assertThrows(DateTimeException.class, () -> ZonedDateTime.of(2005, 2, 29, 2, 30, 0, 0, ZoneId.of("Europe/Moscow")));
    }

    @Test
    void testDuration() throws Exception {
        Period period = Period.of(0, 0, 1);
        Duration duration = Duration.of(1, ChronoUnit.DAYS);

        System.currentTimeMillis();
        System.nanoTime();

        ZonedDateTime zdt1 = ZonedDateTime.of(2005, 10, 30, 0, 0, 0, 0, ZoneId.of("Europe/Moscow"));

        ZonedDateTime ztd2 = zdt1.plus(period);
        assertEquals(ZonedDateTime.of(2005, 10, 31, 0, 0, 0, 0, ZoneId.of("Europe/Moscow")), ztd2);

        ZonedDateTime ztd3 = zdt1.plus(duration);
        assertEquals(ZonedDateTime.of(2005, 10, 30, 23, 0, 0, 0, ZoneId.of("Europe/Moscow")), ztd3);
    }

    @Test
    void testDateTimeFormatter() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:dd z", Locale.ENGLISH);

        ZonedDateTime zdt1 = ZonedDateTime.of(2005, 10, 30, 0, 0, 0, 0, ZoneId.of("Europe/Moscow"));

        String text = zdt1.format(formatter);
        System.out.println(text);

        TemporalAccessor ta = formatter.parse(text); // java.time.format.Parsed
        ZonedDateTime zdt2 = ZonedDateTime.from(ta);

        assertEquals(zdt1, zdt2);
    }
}