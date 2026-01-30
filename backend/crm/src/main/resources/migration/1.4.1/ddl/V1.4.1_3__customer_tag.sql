-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE IF NOT EXISTS customer_tag
(
    `id`              VARCHAR(32)  NOT NULL COMMENT 'id',
    `name`            VARCHAR(255) NOT NULL COMMENT '标签名称',
    `color`           VARCHAR(50) COMMENT '标签颜色',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32)  NOT NULL COMMENT '更新人',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织id',
    PRIMARY KEY (id)
) COMMENT = '客户标签'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_organization_id ON customer_tag (organization_id ASC);
CREATE INDEX idx_name ON customer_tag (name ASC);

CREATE TABLE IF NOT EXISTS customer_tag_rel
(
    `id`              VARCHAR(32) NOT NULL COMMENT 'id',
    `customer_id`     VARCHAR(32) NOT NULL COMMENT '客户id',
    `tag_id`          VARCHAR(32) NOT NULL COMMENT '标签id',
    `create_time`     BIGINT      NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT      NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32) NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32) NOT NULL COMMENT '更新人',
    `organization_id` VARCHAR(32) NOT NULL COMMENT '组织id',
    PRIMARY KEY (id)
) COMMENT = '客户标签关联'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_customer_id ON customer_tag_rel (customer_id ASC);
CREATE INDEX idx_tag_id ON customer_tag_rel (tag_id ASC);
CREATE INDEX idx_organization_id ON customer_tag_rel (organization_id ASC);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
