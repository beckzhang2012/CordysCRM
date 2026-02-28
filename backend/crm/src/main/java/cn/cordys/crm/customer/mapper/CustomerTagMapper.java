package cn.cordys.crm.customer.mapper;

import cn.cordys.mybatis.BaseMapper;
import cn.cordys.crm.customer.domain.CustomerTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户标签Mapper
 *
 * @author jianxing
 * @date 2025-01-15
 */
@Mapper
public interface CustomerTagMapper extends BaseMapper<CustomerTag> {

    List<CustomerTag> selectByCustomerId(@Param("customerId") String customerId);

    List<CustomerTag> selectByOrganizationId(@Param("organizationId") String organizationId);

    CustomerTag selectByName(@Param("name") String name, @Param("organizationId") String organizationId);

    void incrementUsageCount(@Param("tagId") String tagId);
}
