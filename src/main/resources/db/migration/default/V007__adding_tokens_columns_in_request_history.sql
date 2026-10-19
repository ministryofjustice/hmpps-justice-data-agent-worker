ALTER TABLE if exists request_history
    ADD column if not exists  prompt_tokens BIGINT;

ALTER TABLE if exists request_history
    ADD column if not exists  completion_tokens BIGINT;

ALTER TABLE if exists request_history
    ADD column if not exists  total_tokens BIGINT;
