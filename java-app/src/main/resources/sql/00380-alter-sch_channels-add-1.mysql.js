db.execute('''
ALTER TABLE `sch_channels` 
    ADD COLUMN `wikistyle`  bit(1)  NOT NULL default 0;
''');
