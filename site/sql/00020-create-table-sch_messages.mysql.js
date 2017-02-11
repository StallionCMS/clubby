db.execute('''
CREATE TABLE IF NOT EXISTS `sch_messages` (
`id` bigint(20) unsigned NOT NULL,
    `createdat`  datetime  NULL ,
    `updatedat`  datetime  NULL ,
    `channelid`  bigint(20)  NOT NULL  DEFAULT '0' ,
    `purgeat`  datetime  NULL ,
    `expiresat`  datetime  NULL ,
    `deletedat`  bigint(20)  NOT NULL  DEFAULT '0' ,
    `fromuserid`  bigint(20)  NOT NULL  DEFAULT '0' ,
    `fromusername`  varchar(255)  NULL ,
    `messageencryptedjson`  varchar(255)  NULL ,
    `parentmessageid`  bigint(20)  NOT NULL  DEFAULT '0' ,
    `edited`  bit(1)  NOT NULL  DEFAULT 0 ,
    `editedat`  datetime  NULL ,
    `deleted`  bit(1)  NULL ,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `row_updated_at_key` (`row_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 
''');