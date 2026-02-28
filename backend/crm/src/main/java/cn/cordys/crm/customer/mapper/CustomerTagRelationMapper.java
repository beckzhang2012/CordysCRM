package cn.cordys.crm.customer.mapper;

import cn.cordys.mybatis.BaseMapper;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户标签关联Mapper
 *
 * @author jianxing
 * @date 2025-01-15
 */
@Mapper
public interface CustomerTagRelationMapper extends BaseMapper<CustomerTagRelation> {

    List<String> selectTagIdsByCustomerId(@Param("customerId") String customerId);

    List<String> selectCustomerIdsByTagId(@Param("tagId") String tagId);

    void deleteByCustomerId(@Param("customerId") String customerId);

    void deleteByTagId(@Param("tagId") String tagId);

    void deleteByCustomerIdAndTagId(@Param("customerId") String customerId, @Param("tagId") String tagId);
}
