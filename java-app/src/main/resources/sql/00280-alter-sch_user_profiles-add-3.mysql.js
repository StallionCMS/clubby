db.execute('''
ALTER TABLE `sch_user_profiles` 
    ADD COLUMN `publickeyjwkjson`  longtext,
    ADD COLUMN `privatekeyjwkencryptedhex`  longtext,
    ADD COLUMN `privatekeyvectorhex`  varchar(255)  NULL;
''');
