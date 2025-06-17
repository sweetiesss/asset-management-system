create TABLE asset_returns
(
    id            UUID        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE,
    updated_at    TIMESTAMP WITHOUT TIME ZONE,
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255),
    returned_date date,
    state         VARCHAR(50) NOT NULL,
    assignment_id UUID        NOT NULL,
    version       BIGINT DEFAULT 0,
    CONSTRAINT pk_asset_returns PRIMARY KEY (id)
);

alter table asset_returns
    add CONSTRAINT uc_asset_returns_assignment UNIQUE (assignment_id);

alter table asset_returns
    add CONSTRAINT FK_ASSET_RETURNS_ON_ASSIGNMENT FOREIGN KEY (assignment_id) REFERENCES assignments (id);

alter table assets
    alter COLUMN category_id SET NOT NULL;

alter table assets
    alter COLUMN location_id SET NOT NULL;