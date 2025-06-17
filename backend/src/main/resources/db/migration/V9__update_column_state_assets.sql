alter table assets
    alter COLUMN state TYPE VARCHAR(50) USING (state::VARCHAR(50));