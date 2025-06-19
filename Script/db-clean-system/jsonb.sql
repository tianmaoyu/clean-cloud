


SELECT
    name,
    details->> 'brand' brand
FROM
    products
WHERE
    details['brand'] = '"Dell"'::jsonb;


SELECT
    name,
    details->> 'brand' brand
FROM
    products
WHERE
    details->>'brand' = 'Dell';

-- 多种写法
SELECT * FROM products WHERE details @> '{"brand": "Dell"}'::jsonb;
SELECT * FROM products WHERE details @> '{"brand": "Dell"}';

SELECT * FROM products WHERE details->>'brand' = 'Dell';

SELECT * FROM products WHERE details['brand'] = '"Dell"'::jsonb;

-- 添加一个属性
UPDATE products
SET details = details || '{"tags": ["electronics", "apple", "mobile"]}'::jsonb
where 1=1

