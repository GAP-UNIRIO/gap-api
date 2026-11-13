ALTER TABLE orders ADD COLUMN status varchar(30);

UPDATE orders SET status = 'EM_ANALISE' WHERE status IS NULL;

ALTER TABLE orders ALTER COLUMN status SET NOT NULL;
