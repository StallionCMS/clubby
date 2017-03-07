db.execute('''
ALTER TABLE `sch_channels` 
    ADD COLUMN `newusersseeoldmessages`  bit(1)  NOT NULL DEFAULT 1 ;
''');