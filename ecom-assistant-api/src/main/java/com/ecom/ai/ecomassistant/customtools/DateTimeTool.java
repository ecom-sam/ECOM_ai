package com.ecom.ai.ecomassistant.customtools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeTool implements ChatToolMarker {
    @Tool(description = "Get the current date and time in the user's timezone")
    public String getCurrentDateTime() {
        ZoneId zone = LocaleContextHolder.getTimeZone().toZoneId();
        return LocalDateTime.now(zone).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + zone;
    }

    @Tool(description = "Get the current date in the user's timezone")
    public String getCurrentDate() {
        ZoneId zone = LocaleContextHolder.getTimeZone().toZoneId();
        return LocalDate.now(zone).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Tool(description = "Get the current time in the user's timezone")
    public String getCurrentTime() {
        ZoneId zone = LocaleContextHolder.getTimeZone().toZoneId();
        return LocalTime.now(zone).format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    @Tool(description = "Convert a date-time string from one timezone to another. Input format: yyyy-MM-ddTHH:mm:ss")
    public String convertTimezone(String dateTimeStr, String fromZone, String toZone) {
        LocalDateTime ldt = LocalDateTime.parse(dateTimeStr);
        ZonedDateTime fromZoned = ldt.atZone(ZoneId.of(fromZone));
        ZonedDateTime toZoned = fromZoned.withZoneSameInstant(ZoneId.of(toZone));
        return toZoned.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + toZone;
    }

    @Tool(description = "Calculate days between two dates (format yyyy-MM-dd)")
    public long daysBetween(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return Duration.between(start.atStartOfDay(), end.atStartOfDay()).toDays();
    }

    @Tool(description = "Get the day of week for a given date (format yyyy-MM-dd)")
    public String dayOfWeek(String date) {
        LocalDate ld = LocalDate.parse(date);
        return ld.getDayOfWeek().toString();
    }
}
