db.execute('''
ALTER TABLE `sch_user_profiles` 
    ADD COLUMN `notifywhenmentioned`  bit(1)  NOT NULL DEFAULT 1;
''');
