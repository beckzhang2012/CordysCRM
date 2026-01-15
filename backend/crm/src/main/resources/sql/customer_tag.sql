-- 客户标签表
CREATE TABLE IF NOT EXISTS `customer_tag` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '标签名称',
  `color` varchar(20) DEFAULT '#18a058' COMMENT '标签颜色',
  `organization_id` varchar(32) NOT NULL COMMENT '组织ID',
  `create_user` varchar(32) DEFAULT NULL COMMENT '创建人',
  `usage_count` int(11) DEFAULT '0' COMMENT '使用次数',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_organization_id` (`organization_id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签表';

-- 客户标签关联表
CREATE TABLE IF NOT EXISTS `customer_tag_relation` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `customer_id` varchar(32) NOT NULL COMMENT '客户ID',
  `tag_id` varchar(32) NOT NULL COMMENT '标签ID',
  `organization_id` varchar(32) NOT NULL COMMENT '组织ID',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_tag_id` (`tag_id`),
  KEY `idx_organization_id` (`organization_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签关联表';
