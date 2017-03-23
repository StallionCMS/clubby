db.execute('''
ALTER TABLE `sch_channel_members` 
    ADD COLUMN `hidden`  bit(1)  NOT NULL DEFAULT 0;
''');
