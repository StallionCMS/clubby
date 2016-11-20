db.execute('''
CREATE TABLE IF NOT EXISTS `sch_user_profiles` (
`id` bigint(20) unsigned NOT NULL,
    `publickey`  varchar(255)  NULL ,
    `encryptedprivatekey`  varchar(255)  NULL ,
    `userid`  bigint(20)  NULL ,
    `deleted`  bit(1)  NULL ,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `row_updated_at_key` (`row_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 
''');