-- CREATE TABLE Types(
-- 	id SERIAL PRIMARY KEY,
-- 	type TEXT,
-- 	ecosystem TEXT
-- );

CREATE OR REPLACE FUNCTION log_insert_types()
RETURNS TRIGGER AS
$$
BEGIN
  RAISE NOTICE 'Добавлен новый объект: id=%, type=%, ecosystem=%', NEW.id, NEW.type, NEW.ecosystem;
  RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER insert_types_trigger
AFTER INSERT ON Types
FOR EACH ROW
EXECUTE FUNCTION log_insert_types();
