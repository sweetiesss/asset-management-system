CREATE VIEW report_view AS
SELECT c.id, 
    c.name AS category, 
    COUNT(a.id) AS total, 
    COUNT(a.id) FILTER(WHERE a.state = 'ASSIGNED') AS assigned,
    COUNT(a.id) FILTER(WHERE a.state = 'AVAILABLE') AS available,
    COUNT(a.id) FILTER(WHERE a.state = 'NOT_AVAILABLE') AS not_available,
    COUNT(a.id) FILTER(WHERE a.state = 'WAITING_FOR_RECYCLING') AS waiting_for_recycling,
    COUNT(a.id) FILTER(WHERE a.state = 'RECYCLED') AS recycled

FROM categories AS c
INNER JOIN assets AS a
ON a.category_id = c.id
GROUP BY c.id;