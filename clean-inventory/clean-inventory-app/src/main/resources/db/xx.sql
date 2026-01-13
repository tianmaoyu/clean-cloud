select *
from batch_part
         left join product_part on
    product_part.category_id = batch_part.category_id
where batch_part.category_id = 938;

CREATE INDEX idx_batch_part_category_id ON batch_part(category_id);


explain(ANALYZE, BUFFERS) select count(1)
from batch
where category_id = 938;

explain(ANALYZE, BUFFERS)  select count(1)
    from batch_part
    where batch_part.category_id = 938;


--update batch set category_id=b. category_id from batch_part b where b.batch_id = batch.batch_id;
CREATE EXTENSION pg_stat_statements;
select * from pg_stat_statements;

explain(ANALYZE, BUFFERS) select count(*) from batch  inner join product on
    product.category_id = batch.category_id
where batch.category_id = 938;

explain(ANALYZE, BUFFERS) select count(*) from batch_part inner join product_part  on
    product_part .category_id = batch_part .category_id
where batch_part .category_id = 938;
