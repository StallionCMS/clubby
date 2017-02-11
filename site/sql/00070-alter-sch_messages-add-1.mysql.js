db.execute('''
ALTER TABLE `sch_messages` 
    ADD COLUMN `messagejson`  longtext  NULL;
''');