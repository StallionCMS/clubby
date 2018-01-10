db.execute('''
ALTER TABLE `sch_user_profiles` 
    ADD COLUMN `passwordfourcharactershashed`  varchar(255)  NULL,
    ADD COLUMN `passwordsalt`  varchar(255)  NULL;
''');