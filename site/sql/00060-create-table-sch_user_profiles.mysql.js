db.execute('''
CREATE TABLE IF NOT EXISTS `sch_user_profiles` (
`id` bigint(20) unsigned NOT NULL,
    `email`  varchar(255)  NULL ,
    `userid`  bigint(20)  NULL ,
    `publickeyhex`  varchar(255)  NULL ,
    `encryptedprivatekeyhex`  varchar(255)  NULL ,
    `encryptedprivatekeyinitializationvectorhex`  varchar(255)  NULL ,
    `aboutme`  varchar(255)  NULL ,
    `website`  varchar(255)  NULL ,
    `emailmewhenmentioned`  bit(1)  NOT NULL  DEFAULT 1 ,
    `deleted`  bit(1)  NULL ,
  `row_updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `row_updated_at_key` (`row_updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 
''');