db.execute('''
ALTER TABLE `sch_user_messages` 
    ADD COLUMN `mobilenotifypending`  bit(1)  NOT NULL DEFAULT 0 ;
''');