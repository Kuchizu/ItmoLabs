CREATE TABLE Types(
	id SERIAL PRIMARY KEY,
	type TEXT,
	ecosystem TEXT
);

CREATE TABLE Locations(
	id SERIAL PRIMARY KEY,
	title TEXT,
	type INT REFERENCES Types,
	size INT
);

CREATE TABLE Ways(
	id SERIAL PRIMARY KEY,
	from_ INT REFERENCES Locations,
	to_ INT REFERENCES Locations
);

CREATE TABLE Persons(
	id SERIAL PRIMARY KEY,
	name TEXT NOT NULL,
	surname TEXT NOT NULL,
	sex BOOL NOT NULL,
	location INT NOT NULL REFERENCES Locations,
	way INT REFERENCES Ways
);

CREATE TABLE Animals(
	id SERIAL PRIMARY KEY,
	name TEXT,
	type TEXT NOT NULL,
	location INT NOT NULL REFERENCES Locations,
	way INT REFERENCES Ways on delete cascade
);

CREATE TABLE Cars(
	id SERIAL PRIMARY KEY,
	model TEXT,
	owner INT REFERENCES Persons,
	way INT REFERENCES Ways,
	pass1 INT REFERENCES Persons check(pass1 <> pass2 and pass1 <> pass3),
	pass2 INT REFERENCES Persons check(pass2 <> pass1 and pass2 <> pass3),
	pass3 INT REFERENCES Persons check(pass3 <> pass1 and pass3 <> pass2)
);

CREATE TABLE Lycenses(
	id SERIAL PRIMARY KEY,
	person_id INT REFERENCES Persons,
	car INT REFERENCES Cars
);

INSERT INTO Types(type, ecosystem) VALUES('Морской', 53);
INSERT INTO Types(type, ecosystem) VALUES('Речной', 31);
INSERT INTO Types(type, ecosystem) VALUES('Скалистый', 22);
INSERT INTO Types(type, ecosystem) VALUES('Земляной', 78);
INSERT INTO Types(type, ecosystem) VALUES('Каменный', 33);

INSERT INTO Locations(title, type, size) VALUES('Берег', 1, 512);
INSERT INTO Locations(title, type, size) VALUES('Дом', 4, 100);
INSERT INTO Locations(title, type, size) VALUES('Лес', 3, 10000);

INSERT INTO Ways(from_, to_) VALUES(1, 2);
INSERT INTO Ways(from_, to_) VALUES(2, 1);
INSERT INTO Ways(from_, to_) VALUES(1, 3);
INSERT INTO Ways(from_, to_) VALUES(3, 1);
INSERT INTO Ways(from_, to_) VALUES(2, 3);
INSERT INTO Ways(from_, to_) VALUES(3, 2);

INSERT INTO Persons(name, surname, sex, location) VALUES('Тина', 'Боумен', FALSE, 2);
INSERT INTO Persons(name, surname, sex, location, way) VALUES('Гутьеррес', 'Koftagad', TRUE, 1, 1);
INSERT INTO Persons(name, surname, sex, location) VALUES('Kuchizu', 'Saxt', TRUE, 1);

INSERT INTO Animals(name, type, location) VALUES('Мурка', 'Кот', 2);
INSERT INTO Animals(name, type, location, way) VALUES('Не Мурка', 'Собака', 2, 5);

INSERT INTO Cars(model, owner, pass1, pass2) VALUES('Tesla', 3, 1, 2);
INSERT INTO Cars(model, way, owner, pass1, pass2) VALUES('Mercedes', 1, 1, 2, 3);
INSERT INTO Cars(model, way, owner, pass1) VALUES('BMW', 4, 2, 1);

INSERT INTO Lycenses(person_id, car) VALUES(1, 1)
INSERT INTO Lycenses(person_id, car) VALUES(1, 2)
INSERT INTO Lycenses(person_id, car) VALUES(1, 3)
INSERT INTO Lycenses(person_id, car) VALUES(3, 1)

Drop table cars cascade;
Drop table Animals cascade;
Drop table Persons cascade;
Drop table Ways cascade;
Drop table Locations cascade;
Drop table Types cascade;
Drop table Lycenses cascade;
