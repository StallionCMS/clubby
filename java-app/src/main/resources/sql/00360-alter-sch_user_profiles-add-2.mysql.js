db.execute('''
ALTER TABLE `sch_user_profiles` 
    ADD COLUMN `mobilenotifypreference`  varchar(30)  NOT NULL DEFAULT 'VIBRATE',
    ADD COLUMN `desktopnotifypreference`  varchar(30)  NOT NULL DEFAULT 'SILENT';
''');