-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE customer_reminder
(
    `id`              VARCHAR(32)   NOT NULL COMMENT 'id',
    `customer_id`     VARCHAR(32)   NOT NULL COMMENT '客户id',
    `content`         VARCHAR(1000) NOT NULL COMMENT '提醒内容',
    `remind_time`     BIGINT        NOT NULL COMMENT '提醒时间（时间戳）',
    `owner`           VARCHAR(32)   NOT NULL COMMENT '负责人',
    `status`          VARCHAR(32)   NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待提醒, TRIGGERED-已提醒, DISMISSED-已取消',
    `organization_id` VARCHAR(32)   NOT NULL COMMENT '组织id',
    `create_time`     BIGINT        NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT        NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32)   NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32)   NOT NULL COMMENT '更新人',
    PRIMARY KEY (id)
) COMMENT = '客户跟进提醒'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_customer_id ON customer_reminder (customer_id ASC);
CREATE INDEX idx_owner ON customer_reminder (owner ASC);
CREATE INDEX idx_remind_time ON customer_reminder (remind_time ASC);
CREATE INDEX idx_status ON customer_reminder (status ASC);
CREATE INDEX idx_organization_id ON customer_reminder (organization_id ASC);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
