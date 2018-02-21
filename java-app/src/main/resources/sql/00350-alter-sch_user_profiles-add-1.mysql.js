db.execute('''
ALTER TABLE `sch_user_profiles` 
    ADD COLUMN `avatarfileid`  bigint(20)  NULL;
''');