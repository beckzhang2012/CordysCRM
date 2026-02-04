package cn.cordys.common.annotation;

import java.lang.annotation.*;

/**
 * 幂等性校验注解
 * 用于防止重复提交
 *
 * @author cordys
 * @date 2025-02-03
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等key的前缀
     */
    String prefix() default "idempotent";

    /**
     * 幂等key的生成表达式，支持SpEL表达式
     * 默认使用用户ID + 请求参数哈希
     */
    String keyExpression() default "";

    /**
     * 锁的过期时间（秒）
     * 默认5秒
     */
    int expireTime() default 5;

    /**
     * 提示信息
     */
    String message() default "请求正在处理中，请勿重复提交";
}
