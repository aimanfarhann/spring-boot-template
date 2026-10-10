CREATE TABLE product
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL UNIQUE,
    description VARCHAR(1000),
    price       NUMERIC(12, 2) NOT NULL CHECK (price >= 0)
);
