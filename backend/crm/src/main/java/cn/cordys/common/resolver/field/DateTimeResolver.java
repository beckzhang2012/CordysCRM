package cn.cordys.common.resolver.field;

import cn.cordys.common.util.TimeUtils;
import cn.cordys.crm.system.dto.field.DateTimeField;
import org.apache.commons.lang3.StringUtils;
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

    private static final DateTimeFormatter TEXT_TO_VALUE_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-M-d H:m:s"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/d H:m:s"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-M-d H:m"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/d H:m"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-M-d"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/d"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-M"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/M"))
            .toFormatter();

    @Override
    public void validate(DateTimeField dateTimeField, Object value) {
        validateRequired(dateTimeField, value);

        validateLong(dateTimeField.getName(), value);
    }

    protected void validateLong(String name, Object value) {
        if (value != null && !(value instanceof Long)) {
            throwValidateException(name);
        }
    }

    @Override
    public Object convertToValue(DateTimeField dateTimeField, String value) {
        if (value == null || value.isBlank() || Strings.CI.equals(value, "null")) {
            return null;
        }
        return parse2Long(value);
    }

    @Override
    public Object transformToValue(DateTimeField dateTimeField, String value) {
        if (value == null || value.isBlank() || Strings.CI.equals(value, "null")) {
            return StringUtils.EMPTY;
        }
        if (Strings.CI.equals(dateTimeField.getDateType(), DATE)) {
            return TimeUtils.getDataStr(Long.valueOf(value));
        }
        if (Strings.CI.equals(dateTimeField.getDateType(), DATETIME)) {
            return TimeUtils.getDataTimeStr(Long.valueOf(value));
        }
        if (Strings.CI.equals(dateTimeField.getDateType(), MONTH)) {
            return TimeUtils.getMonthStr(Long.valueOf(value));
        }
        return value;
    }

    @Override
    public Object textToValue(DateTimeField field, String text) {
        if (StringUtils.isBlank(text) || Strings.CI.equals(text, "null")) {
            return null;
        }

        TemporalAccessor parsed = TEXT_TO_VALUE_FORMATTER.parseBest(text,
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
    }
}
