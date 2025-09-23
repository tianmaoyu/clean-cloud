

select
    product_part.tableoid::regclass,
        product_part.tableoid::regclass,
        *
from
    batch_part
        left join product_part on
            product_part.category_id = batch_part.category_id
where batch_part.category_id= 938



select count(1)  from  batch_part where batch_part.category_id= 938