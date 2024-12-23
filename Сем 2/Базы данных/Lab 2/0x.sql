/*

1.
	Сделать запрос для получения атрибутов из указанных таблиц, применив фильтры по указанным условиям:
	Таблицы: Н_ЛЮДИ, Н_ВЕДОМОСТИ.
	Вывести атрибуты: Н_ЛЮДИ.ФАМИЛИЯ, Н_ВЕДОМОСТИ.ДАТА.
	Фильтры (AND):
	a) Н_ЛЮДИ.ОТЧЕСТВО = Александрович.
	b) Н_ВЕДОМОСТИ.ИД = 1250972.
	Вид соединения: INNER JOIN.

*/


SELECT Н_ЛЮДИ.ФАМИЛИЯ, Н_ВЕДОМОСТИ.ДАТА
FROM Н_ЛЮДИ
INNER JOIN Н_ВЕДОМОСТИ ON Н_ЛЮДИ.ИД = Н_ВЕДОМОСТИ.ЧЛВК_ИД
WHERE Н_ЛЮДИ.ОТЧЕСТВО = 'Александрович' AND Н_ВЕДОМОСТИ.ИД = 1250972;


/*

2.
	Сделать запрос для получения атрибутов из указанных таблиц, применив фильтры по указанным условиям:
	Таблицы: Н_ЛЮДИ, Н_ВЕДОМОСТИ, Н_СЕССИЯ.
	Вывести атрибуты: Н_ЛЮДИ.ИД, Н_ВЕДОМОСТИ.ИД, Н_СЕССИЯ.ДАТА.
	Фильтры (AND):
	a) Н_ЛЮДИ.ОТЧЕСТВО = Сергеевич.
	b) Н_ВЕДОМОСТИ.ИД < 1426978.
	c) Н_СЕССИЯ.ЧЛВК_ИД < 100622.
	Вид соединения: RIGHT JOIN.

*/


SELECT Н_ЛЮДИ.ИД, Н_ВЕДОМОСТИ.ИД, Н_СЕССИЯ.ДАТА
FROM Н_ЛЮДИ
RIGHT JOIN Н_ВЕДОМОСТИ ON Н_ЛЮДИ.ИД = Н_ВЕДОМОСТИ.ЧЛВК_ИД
RIGHT JOIN Н_СЕССИЯ ON Н_ВЕДОМОСТИ.СЭС_ИД = Н_СЕССИЯ.СЭС_ИД
WHERE Н_ЛЮДИ.ОТЧЕСТВО = 'Сергеевич' AND Н_ВЕДОМОСТИ.ИД < 1426978 AND Н_СЕССИЯ.ЧЛВК_ИД < 100622;


/*

3.
	Составить запрос, который ответит на вопрос, есть ли среди студентов ФКТИУ те, кто младше 20 лет.
*/


SELECT COUNT(DISTINCT "Н_ЛЮДИ"."ИД") AS "Кол-во студентов ФКТИУ младше 20"
FROM "Н_ЛЮДИ"
JOIN "Н_УЧЕНИКИ" ON "Н_ЛЮДИ"."ИД" = "Н_УЧЕНИКИ"."ЧЛВК_ИД"
JOIN "Н_ПЛАНЫ" ON "Н_УЧЕНИКИ"."ПЛАН_ИД" = "Н_ПЛАНЫ"."ИД"
JOIN "Н_ОТДЕЛЫ" ON "Н_ПЛАНЫ"."ОТД_ИД" = "Н_ОТДЕЛЫ"."ИД"
WHERE "Н_ОТДЕЛЫ"."КОРОТКОЕ_ИМЯ" = 'КТиУ' AND "Н_ЛЮДИ"."ДАТА_РОЖДЕНИЯ" > '2003-04-12';


/*

4.
	В таблице Н_ГРУППЫ_ПЛАНОВ найти номера планов, по которым обучается (обучалось) более 2 групп на заочной форме обучения.
	Для реализации использовать соединение таблиц.

*/


SELECT Н_ПЛАНЫ.ПЛАН_ИД
FROM Н_ПЛАНЫ
JOIN Н_ГРУППЫ_ПЛАНОВ ON Н_ПЛАНЫ.ПЛАН_ИД = Н_ГРУППЫ_ПЛАНОВ.ПЛАН_ИД
WHERE Н_ПЛАНЫ.ФО_ИД = 3
GROUP BY Н_ПЛАНЫ.ПЛАН_ИД
HAVING COUNT(DISTINCT Н_ГРУППЫ_ПЛАНОВ.ГРУППА) > 2;


/*
5.
	Выведите таблицу со средними оценками студентов группы 4100 (Номер, ФИО, Ср_оценка), у которых средняя оценка меньше максимальной оценк(е|и) в группе 3100.
*/ 

SELECT * FROM
(
	SELECT DISTINCT "Н_ЛЮДИ"."ИД",
	       "Н_ЛЮДИ"."ФАМИЛИЯ",
	       "Н_ЛЮДИ"."ИМЯ",
	       "Н_ЛЮДИ"."ОТЧЕСТВО",
	       AVG(CAST("Н_ВЕДОМОСТИ"."ОЦЕНКА" AS INTEGER)) as avrg
	FROM "Н_ЛЮДИ"
	JOIN "Н_ВЕДОМОСТИ" ON "Н_ВЕДОМОСТИ"."ЧЛВК_ИД" = "Н_ЛЮДИ"."ИД"
	JOIN "Н_ОБУЧЕНИЯ" ON "Н_ЛЮДИ"."ИД" = "Н_ОБУЧЕНИЯ"."ЧЛВК_ИД"
	JOIN "Н_УЧЕНИКИ" ON "Н_ОБУЧЕНИЯ"."ЧЛВК_ИД" = "Н_УЧЕНИКИ"."ЧЛВК_ИД"
	WHERE "Н_ВЕДОМОСТИ"."ОЦЕНКА" ~ '^[0-9]'
	AND "Н_УЧЕНИКИ"."ГРУППА" = '4100'
	GROUP BY "Н_ЛЮДИ"."ИД", "Н_ЛЮДИ"."ФАМИЛИЯ", "Н_ЛЮДИ"."ИМЯ", "Н_ЛЮДИ"."ОТЧЕСТВО"
) AS "Н_ЛЮДИ"
WHERE avrg < (
    SELECT MAX(CAST("Н_ВЕДОМОСТИ"."ОЦЕНКА" AS INTEGER))
    FROM "Н_ЛЮДИ"
    JOIN "Н_ВЕДОМОСТИ" ON "Н_ВЕДОМОСТИ"."ЧЛВК_ИД" = "Н_ЛЮДИ"."ИД"
    JOIN "Н_ОБУЧЕНИЯ" ON "Н_ЛЮДИ"."ИД" = "Н_ОБУЧЕНИЯ"."ЧЛВК_ИД"
    JOIN "Н_УЧЕНИКИ" ON "Н_ОБУЧЕНИЯ"."ЧЛВК_ИД" = "Н_УЧЕНИКИ"."ЧЛВК_ИД"
    WHERE "Н_ВЕДОМОСТИ"."ОЦЕНКА" ~ '^[0-9]' AND "Н_УЧЕНИКИ"."ГРУППА" = '3100'
)


/*

6.
	Получить список студентов, отчисленных ровно первого сентября 2012 года с заочной формы обучения (специальность: 230101). В результат включить:
	номер группы;
	номер, фамилию, имя и отчество студента;
	номер пункта приказа;
	Для реализации использовать соединение таблиц.

*/


SELECT "ВНЕШ_УЧЕНИКИ"."ГРУППА",
       "ВНЕШ_УЧЕНИКИ"."ИД",
       "Н_ЛЮДИ"."ФАМИЛИЯ",
       "Н_ЛЮДИ"."ИМЯ",
       "Н_ЛЮДИ"."ОТЧЕСТВО",
       "ВНЕШ_УЧЕНИКИ"."П_ПРКОК_ИД"
FROM "Н_УЧЕНИКИ" "ВНЕШ_УЧЕНИКИ"
  JOIN "Н_ЛЮДИ" ON "Н_ЛЮДИ"."ИД" = "ВНЕШ_УЧЕНИКИ"."ЧЛВК_ИД"
  JOIN "Н_ПЛАНЫ" ON "ВНЕШ_УЧЕНИКИ"."ПЛАН_ИД" = "Н_ПЛАНЫ"."ИД"
  JOIN "Н_ФОРМЫ_ОБУЧЕНИЯ" ON "Н_ПЛАНЫ"."ФО_ИД" = "Н_ФОРМЫ_ОБУЧЕНИЯ"."ИД"
    AND "Н_ФОРМЫ_ОБУЧЕНИЯ"."НАИМЕНОВАНИЕ" = 'Заочная'
  JOIN "Н_НАПРАВЛЕНИЯ_СПЕЦИАЛ" ON "Н_ПЛАНЫ"."НАПС_ИД" = "Н_НАПРАВЛЕНИЯ_СПЕЦИАЛ"."ИД"
  JOIN "Н_НАПР_СПЕЦ" ON "Н_НАПР_СПЕЦ"."ИД" = "Н_НАПРАВЛЕНИЯ_СПЕЦИАЛ"."НС_ИД"
    AND "Н_НАПР_СПЕЦ"."НАИМЕНОВАНИЕ" = 'Программная инженерия'
WHERE "ВНЕШ_УЧЕНИКИ"."ПРИЗНАК" = 'отчисл'
  AND "ВНЕШ_УЧЕНИКИ"."СОСТОЯНИЕ" = 'утвержден'
  AND DATE("ВНЕШ_УЧЕНИКИ"."НАЧАЛО") = '2012-09-01';


/*

7.
	Вывести список людей, не являющихся или не являвшихся студентами СПбГУ ИТМО (данные, о которых отсутствуют в таблице Н_УЧЕНИКИ). В запросе нельзя использовать DISTINCT.

*/


SELECT "people"."ИД",
       "people"."ФАМИЛИЯ",
       "people"."ИМЯ",
       "people"."ОТЧЕСТВО"
FROM "Н_ЛЮДИ" AS people
WHERE NOT EXISTS (
  SELECT *
  FROM "Н_УЧЕНИКИ"
    JOIN "Н_ПЛАНЫ" ON "Н_УЧЕНИКИ"."ПЛАН_ИД" = "Н_ПЛАНЫ"."ИД"
    JOIN "Н_ОТДЕЛЫ" ON "Н_ПЛАНЫ"."ОТД_ИД" = "Н_ОТДЕЛЫ"."ИД"
      AND "Н_ОТДЕЛЫ"."КОРОТКОЕ_ИМЯ" = 'СПбГУИТМО'
  WHERE "Н_УЧЕНИКИ"."ЧЛВК_ИД" = "people"."ИД"
);


/* DONE. */


/* ADDITIONAL */
-- Print type of location where car is located which owner is male

SELECT types.type, persons.name FROM persons
JOIN cars on persons.id = cars.owner
JOIN ways on cars.way = ways.id
JOIN locations on ways.to_ = locations.id
JOIN types on locations.type = types.id
WHERE persons.sex = true;