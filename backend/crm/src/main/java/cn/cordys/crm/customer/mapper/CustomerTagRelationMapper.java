package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.CustomerTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CustomerTagRelationMapper {
    List<CustomerTagRelation> selectCustomerTagRelationsByCustomerId(@Param("customerId") String customerId);
    
    List<String> selectCustomerIdsByTagId(@Param("tagId") String tagId);
    
    int insertCustomerTagRelation(@Param("customerTagRelation") CustomerTagRelation customerTagRelation);
    
    int deleteCustomerTagRelation(@Param("customerId") String customerId, @Param("tagId") String tagId);
    
    int deleteCustomerTagRelationsByCustomerId(@Param("customerId") String customerId);
}