create table users (
  username varchar(255) primary key,
  password varchar(255),
  email varchar(255) UNIQUE,
  bio text,
  image varchar(511)
);
