db.execute('''
ALTER TABLE `sch_channels` 
    ADD COLUMN `encrypted`  bit(1)  NOT NULL DEFAULT 0;
''');
