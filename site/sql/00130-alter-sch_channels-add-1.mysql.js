db.execute('''
ALTER TABLE `sch_channels` 
    ADD COLUMN `uniquehash`  varchar(255)  NULL;
''');