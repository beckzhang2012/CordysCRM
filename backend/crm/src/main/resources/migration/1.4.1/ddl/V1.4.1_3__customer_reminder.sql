CREATE TABLE IF NOT EXISTS `customer_reminder` (
  `id` varchar(64) NOT NULL COMMENT 'Primary Key ID',
  `customer_id` varchar(64) NOT NULL COMMENT 'Customer ID',
  `customer_name` varchar(255) DEFAULT NULL COMMENT 'Customer Name',
  `user_id` varchar(64) NOT NULL COMMENT 'User ID',
  `user_name` varchar(255) DEFAULT NULL COMMENT 'User Name',
  `reminder_time` datetime NOT NULL COMMENT 'Reminder Time',
  `content` varchar(500) DEFAULT NULL COMMENT 'Reminder Content',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT 'Status: PENDING, NOTIFIED',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Delete Flag',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_reminder_time` (`reminder_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Customer Reminder Table';
