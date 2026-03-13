package com.grass.grassaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * @description: 时间信息工具，获取当前日期时间及时区转换
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10
 */
public class TimeInfoTool {

    @Tool(description = "Get the current date and time information including year, month, day, weekday, and time. Use this whenever you need to know the current date or time.")
    public String getCurrentDateTime() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss (EEEE)", Locale.CHINESE);
        return "Current Date & Time (Asia/Shanghai):\n" +
                "  Full: " + now.format(fullFormatter) + "\n" +
                "  Date: " + now.toLocalDate() + "\n" +
                "  Time: " + now.toLocalTime().truncatedTo(ChronoUnit.SECONDS) + "\n" +
                "  Timezone: " + now.getZone() + "\n" +
                "  Timestamp: " + now.toInstant().toEpochMilli();
    }

    @Tool(description = "Get the current time in a specific timezone, e.g. 'America/New_York', 'Europe/London', 'Asia/Tokyo'")
    public String getTimeInTimezone(
            @ToolParam(description = "Timezone ID, e.g. 'America/New_York', 'Europe/London', 'Asia/Tokyo'") String timezoneId) {
        try {
            ZoneId zone = ZoneId.of(timezoneId);
            ZonedDateTime now = ZonedDateTime.now(zone);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss (EEEE)", Locale.ENGLISH);
            return "Current time in " + timezoneId + ": " + now.format(formatter);
        } catch (Exception e) {
            return "Error: Invalid timezone ID '" + timezoneId + "'. Use format like 'Asia/Shanghai', 'America/New_York'";
        }
    }

    @Tool(description = "Calculate the number of days between two dates")
    public String daysBetween(
            @ToolParam(description = "Start date in yyyy-MM-dd format") String startDate,
            @ToolParam(description = "End date in yyyy-MM-dd format") String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T00:00:00");
            long days = ChronoUnit.DAYS.between(start, end);
            return "Days between " + startDate + " and " + endDate + ": " + days + " days";
        } catch (Exception e) {
            return "Error: Please use yyyy-MM-dd format, e.g. '2026-03-10'";
        }
    }
}
