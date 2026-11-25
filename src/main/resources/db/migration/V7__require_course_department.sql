-- Cursos novos devem sempre estar associados a um departamento.
-- A migration falha se houver cursos sem associação; nenhum vínculo é inferido.
ALTER TABLE courses
    ALTER COLUMN department_id SET NOT NULL;
