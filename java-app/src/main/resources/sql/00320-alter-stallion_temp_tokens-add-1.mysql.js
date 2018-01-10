db.execute('''
ALTER TABLE `stallion_temp_tokens` 
    ADD COLUMN `deleted`  bit(1)  NULL;
''');