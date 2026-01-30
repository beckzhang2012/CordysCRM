-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

-- 创建客户标签表
CREATE TABLE crm_customer_tag
(
    `id`              VARCHAR(32)  NOT NULL COMMENT '标签ID',
    `name`            VARCHAR(100) NOT NULL COMMENT '标签名称',
    `color`           VARCHAR(20)  DEFAULT '#1890ff' COMMENT '标签颜色',
    `description`     VARCHAR(500) COMMENT '标签描述',
    `organization_id`  VARCHAR(32)  NOT NULL COMMENT '组织ID',
    `create_by`       VARCHAR(32)  COMMENT '创建人',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name_org (name, organization_id)
) COMMENT = '客户标签'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_organization_id ON crm_customer_tag (organization_id ASC);

-- 创建客户标签关联表
CREATE TABLE crm_customer_tag_relation
(
    `id`              VARCHAR(32)  NOT NULL COMMENT '关联ID',
    `customer_id`     VARCHAR(32)  NOT NULL COMMENT '客户ID',
    `tag_id`          VARCHAR(32)  NOT NULL COMMENT '标签ID',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织ID',
    `create_by`       VARCHAR(32)  COMMENT '创建人',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_tag (customer_id, tag_id)
) COMMENT = '客户标签关联'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_customer_id ON crm_customer_tag_relation (customer_id ASC);
CREATE INDEX idx_tag_id ON crm_customer_tag_relation (tag_id ASC);
CREATE INDEX idx_organization_id ON crm_customer_tag_relation (organization_id ASC);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;