db.execute('''
ALTER TABLE `sch_user_profiles` 
    ADD COLUMN `contactinfo`  varchar(255)  NULL;
''');