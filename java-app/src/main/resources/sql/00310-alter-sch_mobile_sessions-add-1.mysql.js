db.execute('''
ALTER TABLE `sch_mobile_sessions` 
    ADD COLUMN `deviceid`  varchar(255)  NOT NULL DEFAULT '';
''');