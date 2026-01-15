-- set innodb lock wait timeout
SET SESSION innodb_lock_wait_timeout = 7200;

CREATE TABLE reminder
(
    `id`              VARCHAR(32)  NOT NULL COMMENT 'id',
    `source_id`       VARCHAR(32)  NOT NULL COMMENT '源ID（如客户ID、线索ID等）',
    `source_name`     VARCHAR(255) COMMENT '源名称（如客户名称、线索名称等）',
    `remind_time`     DATETIME     NOT NULL COMMENT '提醒时间',
    `remind_content`  TEXT COMMENT '提醒内容',
    `is_read`         BIT(1)       NOT NULL DEFAULT 0 COMMENT '是否已读',
    `created_at`      DATETIME     NOT NULL COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
) COMMENT = '提醒'
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci;

CREATE INDEX idx_source_id ON reminder (source_id ASC);
CREATE INDEX idx_remind_time ON reminder (remind_time ASC);
CREATE INDEX idx_is_read ON reminder (is_read ASC);
