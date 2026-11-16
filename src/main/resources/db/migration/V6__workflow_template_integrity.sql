-- Mantém o template mais antigo por tipo e escopo. Apaga as etapas primeiro
-- porque a chave estrangeira de workflow_steps não usa ON DELETE CASCADE.
WITH ranked_templates AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY order_type, course_id ORDER BY id) AS duplicate_rank
    FROM workflow_templates
), duplicate_templates AS (
    SELECT id
    FROM ranked_templates
    WHERE duplicate_rank > 1
)
DELETE FROM workflow_steps step
USING duplicate_templates duplicate
WHERE step.template_id = duplicate.id;

WITH ranked_templates AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY order_type, course_id ORDER BY id) AS duplicate_rank
    FROM workflow_templates
)
DELETE FROM workflow_templates template
USING ranked_templates ranked
WHERE template.id = ranked.id
  AND ranked.duplicate_rank > 1;

CREATE UNIQUE INDEX uq_workflow_templates_order_type_course
    ON workflow_templates (order_type, course_id)
    WHERE course_id IS NOT NULL;

CREATE UNIQUE INDEX uq_workflow_templates_order_type_default
    ON workflow_templates (order_type)
    WHERE course_id IS NULL;
