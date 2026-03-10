package com.grass.grassaiagent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TimeInfoToolTest {

    private TimeInfoTool tool;

    @BeforeEach
    public void setUp() {
        tool = new TimeInfoTool();
    }

    @Test
    public void testGetCurrentDateTime() {
        String result = tool.getCurrentDateTime();
        assertNotNull(result);
        assertTrue(result.contains("Asia/Shanghai"));
        assertTrue(result.contains("Date:"));
        assertTrue(result.contains("Time:"));
        assertTrue(result.contains("Timestamp:"));
        System.out.println(result);
    }

    @Test
    public void testGetTimeInTimezoneNewYork() {
        String result = tool.getTimeInTimezone("America/New_York");
        assertNotNull(result);
        assertTrue(result.contains("America/New_York"));
        System.out.println(result);
    }

    @Test
    public void testGetTimeInTimezoneTokyo() {
        String result = tool.getTimeInTimezone("Asia/Tokyo");
        assertNotNull(result);
        assertTrue(result.contains("Asia/Tokyo"));
        System.out.println(result);
    }

    @Test
    public void testGetTimeInTimezoneInvalid() {
        String result = tool.getTimeInTimezone("Invalid/Zone");
        assertNotNull(result);
        assertTrue(result.contains("Error"));
        System.out.println(result);
    }

    @Test
    public void testDaysBetween() {
        String result = tool.daysBetween("2026-01-01", "2026-03-10");
        assertNotNull(result);
        assertTrue(result.contains("68 days"));
        System.out.println(result);
    }

    @Test
    public void testDaysBetweenReverse() {
        String result = tool.daysBetween("2026-12-31", "2026-01-01");
        assertNotNull(result);
        assertTrue(result.contains("-"));
        System.out.println(result);
    }

    @Test
    public void testDaysBetweenInvalidFormat() {
        String result = tool.daysBetween("2026/01/01", "2026-03-10");
        assertNotNull(result);
        assertTrue(result.contains("Error"));
        System.out.println(result);
    }
}
