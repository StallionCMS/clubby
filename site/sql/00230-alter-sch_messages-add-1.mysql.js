db.execute('''
ALTER TABLE `sch_messages` 
    ADD COLUMN `threadid`  bigint(20)  NOT NULL DEFAULT '0';
''');