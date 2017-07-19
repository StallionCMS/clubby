db.execute('''
ALTER TABLE `stallion_uploaded_files` 
    ADD COLUMN `publiclyviewable`  bit(1)  NOT NULL DEFAULT 0 ,
    ADD COLUMN `sizebytes`  bigint(20)  NOT NULL DEFAULT '0',
    ADD COLUMN `provisional`  bit(1)  NOT NULL DEFAULT 1 ;
''');