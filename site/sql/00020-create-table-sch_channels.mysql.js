db.execute('''
CREATE TABLE IF NOT EXISTS `sch_channels` (
`id` bigint(20) unsigned NOT NULL,
    `name`  varchar(255)  NULL ,
    `channeltype`  varchar(30)  NULL ,
    `deleted`  bit(1)  NULL ,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `row_updated_at_key` (`row_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 
''');