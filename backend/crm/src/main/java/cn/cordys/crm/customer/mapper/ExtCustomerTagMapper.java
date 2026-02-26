package cn.cordys.crm.customer.mapper;

import cn.cordys.crm.customer.domain.CustomerTag;
import cn.cordys.crm.customer.domain.CustomerTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExtCustomerTagMapper {

    void insertTag(CustomerTag tag);

    void updateTag(CustomerTag tag);

    void deleteTag(@Param("id") String id);

    CustomerTag selectTagById(@Param("id") String id);

    List<CustomerTag> selectTagList(@Param("organizationId") String organizationId, @Param("name") String name);

    void insertRelation(CustomerTagRelation relation);

    void deleteRelationByTagId(@Param("tagId") String tagId);

    void deleteRelationByCustomerIdAndTagId(@Param("customerId") String customerId, @Param("tagId") String tagId);

    void deleteRelationByCustomerIdAndTagIds(@Param("customerId") String customerId, @Param("tagIds") List<String> tagIds);

    List<CustomerTagRelation> selectRelationsByCustomerId(@Param("customerId") String customerId);

    List<CustomerTagRelation> selectRelationsByTagId(@Param("tagId") String tagId);

    List<CustomerTag> selectTagsByCustomerId(@Param("customerId") String customerId);
}
