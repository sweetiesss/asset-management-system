create TABLE staff_code_count
(
    id         VARCHAR(255)      NOT NULL,
    last_value INTEGER DEFAULT 0 NOT NULL,
    version    BIGINT  DEFAULT 0 NOT NULL,
    CONSTRAINT pk_staff_code_count PRIMARY KEY (id)
);

insert into staff_code_count (id) values ('SD');