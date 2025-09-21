
select * from product where id=5000000
delete  from product where id=5000000;

with t as (
         SELECT
             id,
             category_id,
             COUNT(*) OVER (PARTITION BY id, category_id) AS cnt
         FROM product_part
)
SELECT id, category_id
from t
WHERE cnt > 1;


create table public.product_part
(
    id             bigint,
    code           varchar(50),
    name           varchar(100),
    category_id    integer,
    brand_id       integer,
    price          numeric(10, 2),
    cost           numeric(10, 2),
    stock_quantity integer,
    weight         numeric(10, 2),
    active_flag    boolean,
    created_at     timestamp with time zone,
    updated_at     timestamp with time zone,
    description    text,
    image_url      varchar(255),
    tags           varchar(255),
    attributes     jsonb,
    primary key (id,category_id)
) partition by  hash (category_id);

alter table public.product_part
    owner to postgres;

CREATE TABLE product_part_0 PARTITION OF product_part FOR VALUES WITH (modulus 8, remainder 0);
CREATE TABLE product_part_1 PARTITION OF product_part FOR VALUES WITH (modulus 8, remainder 1);
CREATE TABLE product_part_2 PARTITION OF product_part FOR VALUES WITH (modulus 8, remainder 2);
CREATE TABLE product_part_3 PARTITION OF product_part FOR VALUES WITH (modulus 8, remainder 3);
CREATE TABLE product_part_4 PARTITION OF product_part FOR VALUES WITH (modulus 8, remainder 4);
CREATE TABLE product_part_5 PARTITION OF product_part FOR VALUES WITH (modulus 8, remainder 5);
CREATE TABLE product_part_6 PARTITION OF product_part FOR VALUES WITH (modulus 8, remainder 6);
CREATE TABLE product_part_7 PARTITION OF product_part FOR VALUES WITH (modulus 8, remainder 7);

insert into  product_part select * from product


