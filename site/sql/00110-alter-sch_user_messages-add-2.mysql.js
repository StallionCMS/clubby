db.execute('''
ALTER TABLE `sch_user_messages` 
    ADD COLUMN `passwordvectorhex`  varchar(255)  NULL,
    ADD COLUMN `encryptedpasswordhex`  text;
''');
