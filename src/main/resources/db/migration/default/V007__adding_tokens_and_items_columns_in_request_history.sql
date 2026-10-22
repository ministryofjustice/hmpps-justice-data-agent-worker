ALTER TABLE if exists request_history
    ADD column if not exists  prompt_tokens INTEGER;

ALTER TABLE if exists request_history
    ADD column if not exists  completion_tokens INTEGER;

ALTER TABLE if exists request_history
    ADD column if not exists  total_tokens INTEGER;

ALTER TABLE if exists request_history
    ADD column if not exists  total_input_items INTEGER;

ALTER TABLE if exists request_history
    ADD column if not exists  total_output_items INTEGER;
