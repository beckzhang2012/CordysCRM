-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE customer_tag
(
    `id`              VARCHAR(32)  NOT NULL COMMENT 'id',
    `name`            VARCHAR(100) NOT NULL COMMENT '标签名称',
    `color`           VARCHAR(20)  NOT NULL COMMENT '标签颜色',
    `enable`          BIT(1)       NOT NULL DEFAULT 1 COMMENT '启用/禁用',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织id',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32)  NOT NULL COMMENT '更新人',
    PRIMARY KEY (id)
) COMMENT = '客户标签'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE UNIQUE INDEX uk_org_name ON customer_tag (organization_id, name);
CREATE INDEX idx_organization_id ON customer_tag (organization_id ASC);
CREATE INDEX idx_enable ON customer_tag (enable ASC);

CREATE TABLE customer_tag_relation
(
    `id`              VARCHAR(32) NOT NULL COMMENT 'id',
    `customer_id`     VARCHAR(32) NOT NULL COMMENT '客户id',
    `tag_id`          VARCHAR(32) NOT NULL COMMENT '标签id',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织id',
    `create_time`     BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT      NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32) NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32) NOT NULL COMMENT '更新人',
    PRIMARY KEY (id)
) COMMENT = '客户标签关联'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE UNIQUE INDEX uk_customer_tag ON customer_tag_relation (customer_id, tag_id);
CREATE INDEX idx_organization_id ON customer_tag_relation (organization_id ASC);
CREATE INDEX idx_customer_id ON customer_tag_relation (customer_id ASC);
CREATE INDEX idx_tag_id ON customer_tag_relation (tag_id ASC);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;