db.execute('''
ALTER TABLE `sch_channels` 
    ADD COLUMN `directmessageuserids`  longtext  NULL;
''');