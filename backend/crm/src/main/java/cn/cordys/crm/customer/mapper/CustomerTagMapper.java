package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.dto.request.CustomerTagPageRequest;
import cn.cordys.crm.customer.dto.response.CustomerTagListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 客户标签 Mapper
 *
 * @author jianxing
 * @date 2025-01-30 16:24:22
 */
@Mapper
public interface CustomerTagMapper {
    List<CustomerTagListResponse> selectCustomerTagList(@Param("request") CustomerTagPageRequest request);
    
    CustomerTag selectCustomerTagById(@Param("id") String id);
    
    int insertCustomerTag(@Param("customerTag") CustomerTag customerTag);
    
    int updateCustomerTag(@Param("customerTag") CustomerTag customerTag);
    
    int deleteCustomerTagById(@Param("id") String id);
}