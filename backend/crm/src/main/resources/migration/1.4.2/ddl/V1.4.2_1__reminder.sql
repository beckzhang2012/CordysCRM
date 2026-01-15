CREATE TABLE reminder
(
    `id`              VARCHAR(32)  NOT NULL COMMENT 'id',
    `customer_id`     VARCHAR(32)  NOT NULL COMMENT '客户ID',
    `customer_name`   VARCHAR(255) NOT NULL COMMENT '客户名称',
    `reminder_time`   BIGINT       NOT NULL COMMENT '提醒时间',
    `content`         VARCHAR(500) NOT NULL COMMENT '提醒内容',
    `is_read`         BIT(1)       NOT NULL DEFAULT 0 COMMENT '是否已读',
    `organization_id` VARCHAR(32)  NOT NULL COMMENT '组织ID',
    `creator_id`      VARCHAR(32)  NOT NULL COMMENT '创建人ID',
    `creator_name`    VARCHAR(255) NOT NULL COMMENT '创建人名称',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`     VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`     VARCHAR(32)  NOT NULL COMMENT '更新人',
    PRIMARY KEY (id)
) COMMENT = '提醒'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_organization_id ON reminder (organization_id ASC);
CREATE INDEX idx_creator_id ON reminder (creator_id ASC);
CREATE INDEX idx_reminder_time ON reminder (reminder_time ASC);
CREATE INDEX idx_is_read ON reminder (is_read ASC);
CREATE INDEX idx_customer_id ON reminder (customer_id ASC);
