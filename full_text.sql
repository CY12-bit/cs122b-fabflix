USE moviedb;

ALTER TABLE movies ADD FULLTEXT(title);