-- 客户标签表
CREATE TABLE IF NOT EXISTS `customer_tag` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(20) DEFAULT '#1677FF' COMMENT '标签颜色',
    `organization_id` VARCHAR(50) NOT NULL COMMENT '组织ID',
    `create_user` VARCHAR(50) NOT NULL COMMENT '创建人',
    `create_time` BIGINT NOT NULL COMMENT '创建时间',
    `update_user` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time` BIGINT DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_org_id` (`organization_id`),
    KEY `idx_name` (`name`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户标签表';

-- 客户标签关联表
CREATE TABLE IF NOT EXISTS `customer_tag_relation` (
    `id` VARCHAR(50) NOT NULL COMMENT '主键ID',
    `customer_id` VARCHAR(50) NOT NULL COMMENT '客户ID',
    `tag_id` VARCHAR(50) NOT NULL COMMENT '标签ID',
    `organization_id` VARCHAR(50) NOT NULL COMMENT '组织ID',
    `create_user` VARCHAR(50) NOT NULL COMMENT '创建人',
    `create_time` BIGINT NOT NULL COMMENT '创建时间',
    `update_user` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time` BIGINT DEFAULT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_tag_id` (`tag_id`),
    KEY `idx_org_id` (`organization_id`),
    KEY `idx_deleted` (`deleted`),
    UNIQUE KEY `uk_customer_tag` (`customer_id`, `tag_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户标签关联表';
