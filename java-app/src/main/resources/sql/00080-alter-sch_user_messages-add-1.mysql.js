db.execute('''
ALTER TABLE `sch_user_messages` 
    ADD COLUMN `encryptedmessagedecryptionkey`  varchar(255)  NULL;
''');