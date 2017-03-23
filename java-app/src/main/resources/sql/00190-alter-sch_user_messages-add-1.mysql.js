db.execute('''
ALTER TABLE `sch_user_messages` 
    ADD COLUMN `emailnotifysent`  bit(1)  NOT NULL DEFAULT 0;
''');
