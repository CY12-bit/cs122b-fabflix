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
	starId VARCHAR(10) NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    FOREIGN KEY(starId) REFERENCES stars(id),
    FOREIGN KEY(movieId) REFERENCES movies(id)
);
CREATE TABLE genres (
	id INTEGER AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL,
    PRIMARY KEY(id)
);
CREATE TABLE genres_in_movies (
	genreId INTEGER NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    FOREIGN KEY(genreId) REFERENCES genres(id),
    FOREIGN KEY(movieId) REFERENCES movies(id)
);
CREATE TABLE creditcards (
	id VARCHAR(20),
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    expiration date NOT NULL,
    PRIMARY KEY(id)
);
CREATE TABLE customers (
	id INTEGER AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    ccld VARCHAR(20) NOT NULL,
    address VARCHAR(200) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(ccld) REFERENCES creditcards(id)
);
CREATE TABLE sales (
	id INTEGER AUTO_INCREMENT,
    customerId INTEGER NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    saleDate date NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(customerId) REFERENCES customers(id),
    FOREIGN KEY(movieId) REFERENCES movies(id)
);
CREATE TABLE ratings (
	movieId VARCHAR(10) NOT NULL,
    rating FLOAT NOT NULL,
    numVotes INTEGER NOT NULL,
    FOREIGN KEY(movieId) REFERENCES movies(id)
)