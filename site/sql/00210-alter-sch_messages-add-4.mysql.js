db.execute('''
ALTER TABLE `sch_messages` 
    ADD COLUMN `pinned`  bit(1)  NOT NULL DEFAULT 0 ,
    ADD COLUMN `title`  varchar(255)  NULL,
    ADD COLUMN `wiki`  bit(1)  NOT NULL DEFAULT 0 ,
    ADD COLUMN `threadupdatedat`  datetime  NULL;
''');