package cn.cordys.crm.customer.mapper;

import cn.cordys.common.dto.OptionDTO;
import cn.cordys.crm.customer.domain.CustomerTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtCustomerTagMapper {

    List<CustomerTag> list(@Param("keyword") String keyword, @Param("orgId") String orgId);

    void batchInsertRel(@Param("customerId") String customerId, @Param("tagIds") List<String> tagIds, @Param("orgId") String orgId);

    void deleteRelByCustomerId(@Param("customerId") String customerId);

    void deleteRelByCustomerIdAndTagIds(@Param("customerId") String customerId, @Param("tagIds") List<String> tagIds);

    List<String> selectTagIdsByCustomerId(@Param("customerId") String customerId);

    List<CustomerTag> selectTagsByCustomerId(@Param("customerId") String customerId);

    List<OptionDTO> getTagOptions(@Param("keyword") String keyword, @Param("orgId") String orgId);

    boolean checkNameExists(@Param("name") String name, @Param("orgId") String orgId, @Param("id") String id);

    List<String> selectCustomerIdsByTagIds(@Param("tagIds") List<String> tagIds, @Param("orgId") String orgId);
}
