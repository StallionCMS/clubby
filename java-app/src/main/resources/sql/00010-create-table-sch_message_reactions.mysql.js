db.execute('''
CREATE TABLE IF NOT EXISTS `sch_message_reactions` (
`id` bigint(20) unsigned NOT NULL,
    `displayname`  varchar(255)  NULL ,
    `userid`  bigint(20)  NULL ,
    `createdat`  datetime  NULL ,
    `messageid`  bigint(20)  NULL ,
    `emoji`  varchar(255)  NULL ,
    `recipientuserid`  bigint(20)  NULL ,
    `deleted`  bit(1)  NULL ,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `row_updated_at_key` (`row_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 
''');