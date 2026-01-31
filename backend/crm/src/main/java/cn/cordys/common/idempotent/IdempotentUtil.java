package cn.cordys.common.idempotent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 幂等性校验工具类
 * 
 * @author jianxing
 * @date 2025-02-08 16:24:22
 */
@Component
public class IdempotentUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 幂等性校验
     * 
     * @param request 请求对象
     * @param expireTime 过期时间（秒）
     * @return true表示通过校验，false表示重复请求
     */
    public boolean checkIdempotent(HttpServletRequest request, long expireTime) {
        String key = buildIdempotentKey(request);
        
        // 尝试设置键值对，如果键已存在则返回false
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "1", expireTime, TimeUnit.SECONDS);
        
        return Boolean.TRUE.equals(result);
    }

    /**
     * 构建幂等性校验的键
     * 
     * @param request 请求对象
     * @return 幂等性校验的键
     */
    private String buildIdempotentKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String userId = request.getHeader("userId");
        
        // 如果没有userId，则使用IP地址
        if (!StringUtils.hasText(userId)) {
            userId = request.getRemoteAddr();
        }
        
        // 使用请求URI、方法和用户ID构建唯一键
        return "idempotent:" + method + ":" + uri + ":" + userId;
    }

    /**
     * 删除幂等性校验的键
     * 
     * @param request 请求对象
     */
    public void removeIdempotentKey(HttpServletRequest request) {
        String key = buildIdempotentKey(request);
        redisTemplate.delete(key);
    }
}