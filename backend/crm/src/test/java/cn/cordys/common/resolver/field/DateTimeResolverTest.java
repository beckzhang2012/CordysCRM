package cn.cordys.common.resolver.field;

import cn.cordys.crm.system.dto.field.DateTimeField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeResolverTest {

    private DateTimeResolver dateTimeResolver;
    private DateTimeField dateTimeField;

    @BeforeEach
    void setUp() {
        dateTimeResolver = new DateTimeResolver();
        dateTimeField = new DateTimeField();
        dateTimeField.setName("birthday");
        dateTimeField.setDateType("date");
        dateTimeField.setRequired(false);
    }

    @Test
    @DisplayName("transformToValue - 处理null值应返回null")
    void transformToValue_WithNull_ShouldReturnNull() {
        Object result = dateTimeResolver.transformToValue(dateTimeField, null);
        assertNull(result);
    }

    @Test
    @DisplayName("transformToValue - 处理空字符串应返回null")
    void transformToValue_WithEmptyString_ShouldReturnNull() {
        Object result = dateTimeResolver.transformToValue(dateTimeField, "");
        assertNull(result);
    }

    @Test
    @DisplayName("transformToValue - 处理空白字符串应返回null")
    void transformToValue_WithBlankString_ShouldReturnNull() {
        Object result = dateTimeResolver.transformToValue(dateTimeField, "   ");
        assertNull(result);
    }

    @Test
    @DisplayName("transformToValue - 处理有效日期戳应返回格式化日期字符串")
    void transformToValue_WithValidTimestamp_ShouldReturnFormattedDate() {
        long timestamp = 1640995200000L;
        Object result = dateTimeResolver.transformToValue(dateTimeField, String.valueOf(timestamp));
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    @Test
    @DisplayName("transformToValue - 处理无效数字字符串应返回null")
    void transformToValue_WithInvalidNumber_ShouldReturnNull() {
        Object result = dateTimeResolver.transformToValue(dateTimeField, "invalid");
        assertNull(result);
    }

    @Test
    @DisplayName("convertToValue - 处理null值应返回null")
    void convertToValue_WithNull_ShouldReturnNull() {
        Object result = dateTimeResolver.convertToValue(dateTimeField, null);
        assertNull(result);
    }

    @Test
    @DisplayName("convertToValue - 处理空字符串应返回null")
    void convertToValue_WithEmptyString_ShouldReturnNull() {
        Object result = dateTimeResolver.convertToValue(dateTimeField, "");
        assertNull(result);
    }

    @Test
    @DisplayName("convertToValue - 处理有效日期戳应返回Long类型")
    void convertToValue_WithValidTimestamp_ShouldReturnLong() {
        long timestamp = 1640995200000L;
        Object result = dateTimeResolver.convertToValue(dateTimeField, String.valueOf(timestamp));
        assertNotNull(result);
        assertTrue(result instanceof Long);
        assertEquals(timestamp, result);
    }

    @Test
    @DisplayName("textToValue - 处理null值应返回null")
    void textToValue_WithNull_ShouldReturnNull() {
        Object result = dateTimeResolver.textToValue(dateTimeField, null);
        assertNull(result);
    }

    @Test
    @DisplayName("textToValue - 处理空字符串应返回null")
    void textToValue_WithEmptyString_ShouldReturnNull() {
        Object result = dateTimeResolver.textToValue(dateTimeField, "");
        assertNull(result);
    }

    @Test
    @DisplayName("textToValue - 处理有效日期文本应返回时间戳")
    void textToValue_WithValidDateText_ShouldReturnTimestamp() {
        Object result = dateTimeResolver.textToValue(dateTimeField, "2024-01-01");
        assertNotNull(result);
        assertTrue(result instanceof Long);
        assertTrue((Long) result > 0);
    }

    @Test
    @DisplayName("textToValue - 处理无效日期文本应抛出验证异常")
    void textToValue_WithInvalidDateText_ShouldThrowValidateException() {
        assertThrows(RuntimeException.class, () -> {
            dateTimeResolver.textToValue(dateTimeField, "invalid-date");
        });
    }

    @Test
    @DisplayName("validate - 非必填字段且值为null时不应抛出异常")
    void validate_NonRequiredWithNullValue_ShouldNotThrowException() {
        dateTimeField.setRequired(false);
        assertDoesNotThrow(() -> {
            dateTimeResolver.validate(dateTimeField, null);
        });
    }

    @Test
    @DisplayName("validate - 必填字段且值为null时应抛出异常")
    void validate_RequiredWithNullValue_ShouldThrowException() {
        dateTimeField.setRequired(true);
        assertThrows(RuntimeException.class, () -> {
            dateTimeResolver.validate(dateTimeField, null);
        });
    }

    @Test
    @DisplayName("validate - 有效Long值不应抛出异常")
    void validate_WithValidLongValue_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            dateTimeResolver.validate(dateTimeField, 1640995200000L);
        });
    }

    @Test
    @DisplayName("validate - 无效类型值应抛出异常")
    void validate_WithInvalidTypeValue_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> {
            dateTimeResolver.validate(dateTimeField, "not-a-long");
        });
    }
}
