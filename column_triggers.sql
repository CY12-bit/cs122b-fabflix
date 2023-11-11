USE moviedb;

ALTER TABLE movies ADD COLUMN price FLOAT DEFAULT NULL;
ALTER TABLE sales ADD COLUMN quantity INT DEFAULT 1;

UPDATE movies
SET price = FLOOR(RAND()*(10-5+1)+5)
WHERE price IS NULL AND id LIKE 'tt%';

UPDATE sales
SET quantity = 1
WHERE quantity IS NULL AND id >= 1;

DROP TRIGGER IF EXISTS `insertRandomPrice`;
DELIMITER $$
CREATE TRIGGER insertRandomPrice BEFORE
INSERT ON movies
FOR EACH ROW
	BEGIN
    IF NEW.price IS NULL
    THEN SET NEW.price = FLOOR(RAND()*(10-5+1)+5);
    END IF;
END $$
DELIMITER ;
