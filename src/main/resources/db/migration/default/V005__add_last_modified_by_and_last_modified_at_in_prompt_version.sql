ALTER TABLE if exists prompt_version
  ADD column if not exists last_modified_by  varchar(255);

ALTER TABLE if exists prompt_version
  ADD column if not exists last_modified_at TIMESTAMP;

UPDATE prompt_version
 set last_modified_at = created_date WHERE last_modified_at IS NULL;
