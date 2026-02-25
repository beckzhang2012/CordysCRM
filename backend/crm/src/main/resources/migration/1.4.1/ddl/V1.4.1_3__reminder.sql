-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE reminder
(
    `id`               VARCHAR(32)  NOT NULL COMMENT 'id',
    `business_type`    VARCHAR(50)  NOT NULL COMMENT '业务类型：CUSTOMER-客户, OPPORTUNITY-商机, CLUE-线索',
    `business_id`      VARCHAR(32)  NOT NULL COMMENT '业务ID',
    `remind_time`      BIGINT       NOT NULL COMMENT '提醒时间戳',
    `content`          TEXT         COMMENT '提醒内容',
    `status`           VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待提醒, COMPLETED-已完成, CANCELLED-已取消',
    `organization_id`  VARCHAR(32)  NOT NULL COMMENT '组织id',
    `owner`            VARCHAR(32)  COMMENT '负责人',
    `create_time`      BIGINT       NOT NULL COMMENT '创建时间',
    `update_time`      BIGINT       NOT NULL COMMENT '更新时间',
    `create_user`      VARCHAR(32)  NOT NULL COMMENT '创建人',
    `update_user`      VARCHAR(32)  NOT NULL COMMENT '更新人',
    PRIMARY KEY (id)
) COMMENT = '提醒'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_organization_id ON reminder (organization_id ASC);
CREATE INDEX idx_owner ON reminder (owner ASC);
CREATE INDEX idx_remind_time ON reminder (remind_time ASC);
CREATE INDEX idx_status ON reminder (status ASC);
CREATE INDEX idx_business ON reminder (business_type ASC, business_id ASC);

-- set innodb lock wait timeout to default
SET SESSION innodb_lock_wait_timeout = DEFAULT;
