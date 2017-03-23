db.execute('''
ALTER TABLE `sch_user_profiles` 
    ADD COLUMN `avatarurl`  varchar(255)  NULL;
''');