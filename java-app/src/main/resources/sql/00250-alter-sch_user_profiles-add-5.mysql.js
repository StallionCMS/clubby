db.execute('''
ALTER TABLE `sch_user_profiles` 
    ADD COLUMN `googleauthenticatorkey`  varchar(255)  NULL,
    ADD COLUMN `googleauthenticatorscratchcodes`  longtext  NULL,
    ADD COLUMN `twofactorenabled`  bit(1)  NULL,
    ADD COLUMN `twofactorcookiesecret`  varchar(255)  NULL,
    ADD COLUMN `twofactorsessions`  longtext  NULL;
''');