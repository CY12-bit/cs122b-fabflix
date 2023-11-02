USE moviedb;

DROP PROCEDURE IF EXISTS add_movie;
DELIMITER $$
CREATE PROCEDURE add_movie(
    IN mTitle VARCHAR(100),
    IN mYear INTEGER,
    IN mDirector VARCHAR(100),
    IN price FLOAT,
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(32)
)
BEGIN
IF (mTitle,mYear,mDirector) NOT IN (SELECT title,year,director FROM movies) THEN 	# If the movie does not exist
    INSERT into movies 
    SELECT CONCAT('tt',LPAD(CAST((MAX(CAST(SUBSTRING(id, 3) AS unsigned)) + 1) AS CHAR(10)),7,'0')),
    mTitle,mYear,mDirector,price FROM movies; 
    IF star_name NOT IN (SELECT DISTINCT name FROM stars) THEN
        INSERT INTO stars(id,name,birthYear) 
        SELECT CONCAT('nm',LPAD(CAST((MAX(CAST(SUBSTRING(id, 3) AS unsigned)) + 1) AS CHAR(10)),7,'0')),
		star_name, NULL
		FROM stars;
    END IF;
    IF genre_name NOT IN (SELECT DISTINCT name FROM genres) THEN
        INSERT INTO genres(name) VALUES (genre_name);
    END IF;
    SELECT id INTO @movie_id FROM movies WHERE title = mTitle AND year = mYear AND director = mDirector;
    SELECT id INTO @star_id FROM stars WHERE name = star_name;
    SELECT id INTO @genre_id FROM genres WHERE name = genre_name;
    INSERT INTO stars_in_movies(starId,movieId) VALUES (@star_id,@movie_id);
    INSERT INTO genres_in_movies(genreId, movieId) VALUES (@genre_id,@movie_id);
	SELECT @movie_id, @star_id, @genre_id;
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
    IF (star_name, birth_year) NOT IN (SELECT name, birthYear FROM stars) THEN
        INSERT INTO stars(id,name,birthYear)
        SELECT CONCAT('nm',LPAD(CAST((MAX(CAST(SUBSTRING(id, 3) AS unsigned)) + 1) AS CHAR(10)),7,'0')),
               star_name, birth_year
        FROM stars;

        SELECT id AS star_id FROM stars WHERE name = star_name AND birthYear = birth_year;
    END IF;
END $$
DELIMITER ;