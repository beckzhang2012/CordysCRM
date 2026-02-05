package cn.cordys.common.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 导出执行器解析器
 * 根据资源类型获取对应的导出执行器
 * @author song-cc-rock
 */
@Service
public class ExportExecutorResolver {

    private final Map<String, cn.cordys.common.service.ExportExecutor> executorMap = new ConcurrentHashMap<>();

    /**
     * 注册导出执行器
     * @param resourceType 资源类型
     * @param executor 导出执行器
     */
    public void register(String resourceType, cn.cordys.common.service.ExportExecutor executor) {
        executorMap.put(resourceType, executor);
    }

    /**
     * 根据资源类型解析导出执行器
     * @param resourceType 资源类型
     * @return 导出执行器
     */
    public cn.cordys.common.service.ExportExecutor resolve(String resourceType) {
        return executorMap.get(resourceType);
    }

    /**
     * 注销导出执行器
     * @param resourceType 资源类型
     */
    public void unregister(String resourceType) {
        executorMap.remove(resourceType);
    }
}
