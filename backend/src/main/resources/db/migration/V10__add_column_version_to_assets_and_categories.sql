alter table assets
    add version BIGINT DEFAULT 0;

alter table categories
    add version BIGINT DEFAULT 0;