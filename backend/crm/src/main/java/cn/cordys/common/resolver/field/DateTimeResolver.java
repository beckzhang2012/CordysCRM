package cn.cordys.common.resolver.field;

import cn.cordys.common.util.TimeUtils;
import cn.cordys.crm.system.dto.field.DateTimeField;
import org.apache.commons.lang3.Strings;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

/**
 * @author jianxing
 */
public class DateTimeResolver extends AbstractModuleFieldResolver<DateTimeField> {

    public static final String DATE = "date";
    public static final String DATETIME = "datetime";
    public static final String MONTH = "month";

    @Override
    public void validate(DateTimeField dateTimeField, Object value) {
        validateRequired(dateTimeField, value);

        if (value != null) {
            validateLong(dateTimeField.getName(), value);
        }
    }

    protected void validateLong(String name, Object value) {
        if (value != null && !(value instanceof Long)) {
            throwValidateException(name);
        }
    }

    @Override
    public Object convertToValue(DateTimeField dateTimeField, String value) {
        if (Strings.isBlank(value)) {
            return null;
        }
        return parse2Long(value);
    }

    @Override
    public Object transformToValue(DateTimeField dateTimeField, String value) {
        if (Strings.isBlank(value)) {
            return null;
        }
        try {
            Long timestamp = Long.valueOf(value);
            if (Strings.CI.equals(dateTimeField.getDateType(), DATE)) {
                return TimeUtils.getDataStr(timestamp);
            }
            if (Strings.CI.equals(dateTimeField.getDateType(), DATETIME)) {
                return TimeUtils.getDataTimeStr(timestamp);
            }
            if (Strings.CI.equals(dateTimeField.getDateType(), MONTH)) {
                return TimeUtils.getMonthStr(timestamp);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return value;
    }

    @Override
    public Object textToValue(DateTimeField field, String text) {
        if (Strings.isBlank(text)) {
            return null;
        }
        
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-M-d H:m:s"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/d H:m:s"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-M-d H:m"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/d H:m"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-M-d"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/d"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-M"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy/M"))
                .toFormatter();

        try {
            TemporalAccessor parsed = formatter.parseBest(text,
                    LocalDateTime::from,
                    LocalDate::from,
                    YearMonth::from);

            Instant instant = switch (parsed) {
                case LocalDateTime localDateTime -> localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                case LocalDate localDate -> localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                case YearMonth yearMonth -> yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                default -> throw new DateTimeParseException("无法解析日期时间: " + text, text, 0);
            };

            return instant.toEpochMilli();
        } catch (DateTimeParseException e) {
            throwValidateException(field.getName(), "日期格式不正确");
            return null;
        }
    }
}
