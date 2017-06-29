db.execute('''
CREATE TABLE IF NOT EXISTS `sch_mobile_sessions` (
`id` bigint(20) unsigned NOT NULL,
    `location`  varchar(255)  NOT NULL  DEFAULT '' ,
    `userid`  bigint(20)  NULL ,
    `devicename`  varchar(255)  NOT NULL  DEFAULT '' ,
    `ipaddress`  varchar(255)  NOT NULL  DEFAULT '' ,
    `lastsigninat`  datetime  NULL ,
    `deviceoperatingsystem`  varchar(30)  NOT NULL ,
    `registrationtoken`  varchar(255)  NOT NULL  DEFAULT '' ,
    `sessionkey`  varchar(255)  NOT NULL  DEFAULT '' ,
    `passphraseencryptionsecret`  varchar(255)  NOT NULL  DEFAULT '' ,
    `deleted`  bit(1)  NULL ,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `row_updated_at_key` (`row_updated_at`),
  UNIQUE KEY `sessionkey_key` (`sessionkey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 
''');