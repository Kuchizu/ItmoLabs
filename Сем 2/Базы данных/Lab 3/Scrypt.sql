-- Функция на языке PL/pgSQL `listCars` разработана для извлечения списка уникальных моделей автомобилей и их количества в заданной локации, которыми владеют люди определенного пола.
-- Входные параметры:
-- `location_title TEXT` - Название локации, в которой мы ищем автомобили. Функция вернет только автомобили в этой локации.
-- `person_sex BOOLEAN` - Пол владельцев автомобилей. True означает мужчин, False - женщин. Функция вернет только автомобили, принадлежащие людям этого пола.
-- Функция возвращает таблицу с двумя столбцами: 
-- `model TEXT`, который содержит модели автомобилей.
-- `count INT`, который содержит количество автомобилей каждой модели в указанной локации.
-- Каждая модель указана только один раз, даже если в локации больше одного такого автомобиля.
-- Если функция не найдет ни одного автомобиля, соответствующего критериям, она вызовет исключение с сообщением 'No cars found in % location', где % - название локации.

CREATE OR REPLACE FUNCTION listCars(location_title TEXT, person_sex BOOLEAN) 
RETURNS TABLE (model TEXT, cars_count BIGINT) AS $List$
BEGIN
	RETURN QUERY
	SELECT cars.model, COUNT(cars.id)
	FROM persons
	JOIN cars on persons.id = cars.owner
	JOIN ways on cars.way = ways.id
	JOIN locations on ways.to_ = locations.id
	WHERE locations.title = location_title AND persons.sex = person_sex
	GROUP BY cars.model;

	IF NOT FOUND THEN
		RAISE EXCEPTION 'No cars found in % location', location_title;
	END IF;
END;

$List$ LANGUAGE plpgsql;
