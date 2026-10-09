ALTER TABLE if exists prompt
    ALTER column  created_by  TYPE varchar(255);

ALTER TABLE if exists prompt_version
    ALTER column  created_by  TYPE varchar(255);


UPDATE prompt_version
 set last_modified_by = created_by WHERE last_modified_by IS NULL;
