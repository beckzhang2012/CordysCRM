package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.dto.response.CustomerTagResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtCustomerTagMapper {

    List<CustomerTagResponse> list(@Param("keyword") String keyword, @Param("orgId") String orgId);

    List<CustomerTag> selectByCustomerId(@Param("customerId") String customerId, @Param("orgId") String orgId);

    int countByName(@Param("name") String name, @Param("orgId") String orgId, @Param("excludeId") String excludeId);

    void deleteRelationByCustomerId(@Param("customerId") String customerId, @Param("orgId") String orgId);

    void deleteRelationByTagId(@Param("tagId") String tagId, @Param("orgId") String orgId);

    int countCustomerByTagId(@Param("tagId") String tagId, @Param("orgId") String orgId);

    List<String> selectCustomerIdsByTagIds(@Param("tagIds") List<String> tagIds, @Param("orgId") String orgId);
}
