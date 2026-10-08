ALTER TABLE if exists prompt_version
  ADD column if not exists batch_array_name varchar(255);

ALTER TABLE if exists prompt_version
  ADD column if not exists batch_size INTEGER;
