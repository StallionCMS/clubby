db.execute('''
ALTER TABLE `sch_messages` 
    ADD COLUMN `messageencryptedjsonvector`  varchar(255)  NULL;
''');