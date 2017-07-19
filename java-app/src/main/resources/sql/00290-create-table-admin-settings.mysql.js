db.execute('''
CREATE TABLE IF NOT EXISTS `sch_admin_settings` (
    `name`  varchar(255)  NOT NULL,
    `value` longtext,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 
''');
