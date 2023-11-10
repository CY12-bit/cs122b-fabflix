USE moviedb;

DROP PROCEDURE IF EXISTS add_movie;
DELIMITER $$
CREATE PROCEDURE add_movie(
    IN mTitle VARCHAR(100),
    IN mYear INTEGER,
    IN mDirector VARCHAR(100),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(32)
)
BEGIN
IF (SELECT NOT EXISTS(SELECT 1 FROM movies WHERE mTitle = title AND mYear = year AND mDirector = director)) THEN 	# If the movie does not exist
    INSERT into movies 
    SELECT CONCAT('tt',LPAD(CAST((MAX(CAST(SUBSTRING(id, 3) AS unsigned)) + 1) AS CHAR(10)),7,'0')),
    mTitle,mYear,mDirector,FLOOR(RAND()*(10-5+1)+5) FROM movies; 
    IF (SELECT NOT EXISTS(SELECT 1 FROM stars WHERE star_name = name)) THEN
        INSERT INTO stars(id,name,birthYear) 
        SELECT CONCAT('nm',LPAD(CAST((MAX(CAST(SUBSTRING(id, 3) AS unsigned)) + 1) AS CHAR(10)),7,'0')),
		star_name, NULL
		FROM stars;
    END IF;
    IF (SELECT NOT EXISTS(SELECT 1 FROM genres WHERE genre_name = name)) THEN
        INSERT INTO genres(name) VALUES (genre_name);
    END IF;
    SELECT id INTO @movie_id FROM movies WHERE title = mTitle AND year = mYear AND director = mDirector LIMIT 1;
    SELECT id INTO @star_id FROM stars WHERE name = star_name LIMIT 1;
    SELECT id INTO @genre_id FROM genres WHERE name = genre_name;
    INSERT INTO stars_in_movies(starId,movieId) VALUES (@star_id,@movie_id);
    INSERT INTO genres_in_movies(genreId, movieId) VALUES (@genre_id,@movie_id);
    SELECT @movie_id AS movie_id, @star_id AS star_id, @genre_id AS genre_id;
ELSE
	SELECT null AS movie_id, null AS star_id, null AS genre_id;
END IF;
END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS add_star;
DELIMITER $$
CREATE PROCEDURE add_star(
    IN star_name VARCHAR(100),
    IN birth_year INTEGER
)
BEGIN
    DECLARE maxId INTEGER;
    SELECT CAST(SUBSTR(id, 3) AS UNSIGNED) INTO maxId FROM stars ORDER BY CAST(SUBSTR(id, 3) AS UNSIGNED) DESC LIMIT 1;
    INSERT INTO stars(id,name,birthYear) VALUES (CONCAT('nm', maxId+1),star_name, birth_year);

    SELECT CONCAT('nm', maxId+1);
END $$
DELIMITER ;