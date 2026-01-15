package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.CustomerTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExtCustomerTagMapper {

    List<String> getTagsByCustomerId(@Param("customerId") String customerId);

    void batchInsert(@Param("tags") List<CustomerTag> tags);

    void deleteByCustomerId(@Param("customerId") String customerId);

    void deleteByCustomerIdAndTags(@Param("customerId") String customerId, @Param("tags") List<String> tags);

    List<String> searchTags(@Param("organizationId") String organizationId, @Param("keyword") String keyword);

    List<String> getCustomerIdsByTags(@Param("tags") List<String> tags, @Param("organizationId") String organizationId);

    List<String> getAllTagsByOrganizationId(@Param("organizationId") String organizationId);
}
