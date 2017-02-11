db.execute('''
CREATE TABLE IF NOT EXISTS `sch_user_messages` (
`id` bigint(20) unsigned NOT NULL,
    `userid`  bigint(20)  NULL ,
    `date`  datetime  NULL ,
    `channelid`  bigint(20)  NULL ,
    `messageid`  bigint(20)  NULL ,
    `encryptedmessagedecriptionkey`  varchar(255)  NULL ,
    `mentioned`  bit(1)  NOT NULL  DEFAULT 0 ,
    `herementioned`  bit(1)  NOT NULL  DEFAULT 0 ,
    `read`  bit(1)  NOT NULL  DEFAULT 0 ,
    `deleted`  bit(1)  NULL ,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `row_updated_at_key` (`row_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 
''');