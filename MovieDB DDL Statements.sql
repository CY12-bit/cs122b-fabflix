DROP DATABASE IF EXISTS moviedb;
CREATE DATABASE moviedb;
USE moviedb;

CREATE TABLE movies (
	id VARCHAR(10),
    title VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    director VARCHAR(100) NOT NULL,
    PRIMARY KEY(id)
);
CREATE TABLE stars (
	id VARCHAR(10),
	name VARCHAR(100) NOT NULL,
	birthYear INTEGER,
	PRIMARY KEY(id)
);
CREATE TABLE stars_in_movies (
	starId VARCHAR(10),
    movieId VARCHAR(10),
    FOREIGN KEY(starId) REFERENCES stars(id),
    FOREIGN KEY(movieId) REFERENCES movies(id)
);
CREATE TABLE genres (
	id INTEGER AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL,
    PRIMARY KEY(id)
);
CREATE TABLE genres_in_movies (
	genreId INTEGER,
    movieId VARCHAR(10),
    FOREIGN KEY(genreId) REFERENCES genres(id),
    FOREIGN KEY(movieId) REFERENCES movies(id)
);
CREATE TABLE creditcards (
	id VARCHAR(20),
    firstName VARCHAR(50),
    lastName VARCHAR(50),
    expiration date NOT NULL,
    PRIMARY KEY(id)
);
CREATE TABLE customers (
	id INTEGER AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    ccld VARCHAR(50),
    address VARCHAR(200) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(ccld) REFERENCES creditcards(id)
);
CREATE TABLE sales (
	id INTEGER AUTO_INCREMENT,
    customerId INTEGER,
    movieId VARCHAR(10),
    saleDate date NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(customerId) REFERENCES customers(id),
    FOREIGN KEY(movieId) REFERENCES movies(id)
);
CREATE TABLE ratings (
	movieId VARCHAR(10),
    rating FLOAT NOT NULL,
    numVotes INTEGER NOT NULL,
    FOREIGN KEY(movieId) REFERENCES movies(id)
)