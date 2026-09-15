ALTER TABLE if exists prompt_version
  ADD column if not exists last_modified_by  varchar(255);

ALTER TABLE if exists prompt_version
  ADD column if not exists last_modified_at TIMESTAMP NOT NULL