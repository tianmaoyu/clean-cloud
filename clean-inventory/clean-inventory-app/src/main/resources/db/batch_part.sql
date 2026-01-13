create table public.batch_part
(
    batch_id        bigserial,
    material_id     bigint                                                   not null,
    batch_code      varchar(50)                                              not null,
    production_date date,
    expiration_date date,
    supplier_id     bigint,
    supplier_batch  varchar(50),
    quality_status  varchar(20),
    notes           text,
    created_at      timestamp with time zone default now(),
    attribute_json  jsonb,
    category_id     integer                   not null,
    constraint batch_part_pk1
        primary key (batch_id, category_id)
) partition by hash (category_id);

alter table public.batch_part
    owner to postgres;

CREATE TABLE batch_part_0 PARTITION OF batch_part FOR VALUES WITH (modulus 8, remainder 0);
CREATE TABLE batch_part_1 PARTITION OF batch_part FOR VALUES WITH (modulus 8, remainder 1);
CREATE TABLE batch_part_2 PARTITION OF batch_part FOR VALUES WITH (modulus 8, remainder 2);
CREATE TABLE batch_part_3 PARTITION OF batch_part FOR VALUES WITH (modulus 8, remainder 3);
CREATE TABLE batch_part_4 PARTITION OF batch_part FOR VALUES WITH (modulus 8, remainder 4);
CREATE TABLE batch_part_5 PARTITION OF batch_part FOR VALUES WITH (modulus 8, remainder 5);
CREATE TABLE batch_part_6 PARTITION OF batch_part FOR VALUES WITH (modulus 8, remainder 6);
CREATE TABLE batch_part_7 PARTITION OF batch_part FOR VALUES WITH (modulus 8, remainder 7);


-- insert into batch_part SELECT * from batch_part;
select product_part.tableoid::regclass,product_part.tableoid::regclass, * from product_part
              left join batch_part on product_part.category_id = batch_part.category_id

