BEGIN;

CREATE TABLE IF NOT EXISTS Users
(
    id
    SERIAL
    PRIMARY
    KEY,
    login
    TEXT
    UNIQUE
    NOT
    NULL,
    password
    TEXT
    NOT
    NULL
);

DO
$$
BEGIN
CREATE TYPE FURNISHES AS ENUM(
	'NONE',
	'DESIGNER',
	'FINE',
	'LITTLE'
);
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS Coordinates
(
    id
    SERIAL
    PRIMARY
    KEY,
    x
    INT,
    y
    NUMERIC
);

CREATE TABLE IF NOT EXISTS Houses
(
    id
    SERIAL
    PRIMARY
    KEY,
    name
    TEXT,
    year
    INT,
    numberOfFloors
    INT,
    numberOfLifts
    INT
);

CREATE TABLE IF NOT EXISTS Flats
(
    id
    SERIAL
    PRIMARY
    KEY,
    owner_id
    INT
    REFERENCES
    Users
(
    id
),
    name TEXT,
    coordinates_id INT REFERENCES Coordinates
(
    id
) ON DELETE CASCADE,
    creationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    area INT,
    numberOfRooms INT,
    timeToMetroOnFoot NUMERIC,
    timeToMetroByTransport NUMERIC,
    furnish FURNISHES,
    house_id INT REFERENCES Houses
(
    id
)
  ON DELETE CASCADE
    );

END;