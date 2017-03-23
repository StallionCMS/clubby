db.execute('''
ALTER TABLE `sch_user_messages` 
    ADD COLUMN `watched`  bit(1)  NULL;
''');