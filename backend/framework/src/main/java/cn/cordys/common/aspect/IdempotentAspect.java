package cn.cordys.common.aspect;

import cn.cordys.common.annotation.Idempotent;
import cn.cordys.common.exception.GenericException;
import cn.cordys.common.response.result.CrmHttpResultCode;
import cn.cordys.security.SessionUtils;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性校验切面
 *
 * @author cordys
 * @date 2025-02-03
 */
@Aspect
@Component
public class IdempotentAspect {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(cn.cordys.common.annotation.Idempotent)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        String key = generateKey(point, idempotent, signature);

        // 尝试设置锁，如果key已存在则返回false
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, "1", idempotent.expireTime(), TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(locked)) {
            throw new GenericException(CrmHttpResultCode.REPEAT_REQUEST.getCode(), idempotent.message());
        }

        try {
            return point.proceed();
        } catch (Exception e) {
            // 发生异常时删除锁，允许重试
            redisTemplate.delete(key);
            throw e;
        }
    }

    /**
     * 生成幂等key
     */
    private String generateKey(ProceedingJoinPoint point, Idempotent idempotent, MethodSignature signature) {
        StringBuilder keyBuilder = new StringBuilder(idempotent.prefix());
        keyBuilder.append(":").append(SessionUtils.getUserId());

        // 如果指定了表达式，则使用表达式生成key
        if (!idempotent.keyExpression().isEmpty()) {
            StandardEvaluationContext context = new StandardEvaluationContext();
            Object[] args = point.getArgs();
            String[] paramNames = signature.getParameterNames();

            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            String exprValue = parser.parseExpression(idempotent.keyExpression()).getValue(context, String.class);
            keyBuilder.append(":").append(exprValue);
        } else {
            // 默认使用请求参数的哈希值
            int hashCode = Arrays.deepHashCode(point.getArgs());
            keyBuilder.append(":").append(hashCode);
        }

        return keyBuilder.toString();
    }
}
