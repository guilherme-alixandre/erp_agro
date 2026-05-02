-- This constraint can exist in databases created/updated previously by Hibernate.
-- Ensure it matches the current EnStatusAnimal enum values.
ALTER TABLE IF EXISTS public.animal
    DROP CONSTRAINT IF EXISTS eanimal_status_animal_check;

-- Ensure status_animal is a string column (some older schemas stored ordinals).
DO
$$
DECLARE
    status_animal_type text;
BEGIN
    SELECT c.data_type
    INTO status_animal_type
    FROM information_schema.columns c
    WHERE c.table_schema = 'public'
      AND c.table_name = 'animal'
      AND c.column_name = 'status_animal';

    IF status_animal_type IS NOT NULL
        AND status_animal_type NOT IN ('character varying', 'text', 'character') THEN
        EXECUTE 'ALTER TABLE public.animal ALTER COLUMN status_animal TYPE varchar(255) USING status_animal::text';
    END IF;
END
$$;

-- Normalize legacy persisted values (e.g. ordinal values stored as text)
-- before (re)adding the enum check constraint.
UPDATE public.animal
SET status_animal = CASE btrim(status_animal::text)
    WHEN '' THEN NULL
    WHEN '0' THEN 'ATIVO'
    WHEN '1' THEN 'OBERVACAO'
    WHEN '2' THEN 'VENDIDO'
    WHEN '3' THEN 'OBITO'
    WHEN '4' THEN 'ABATIDO'
    WHEN 'OBSERVACAO' THEN 'OBERVACAO'
    ELSE btrim(status_animal::text)
END
WHERE status_animal IS NOT NULL
  AND (
      status_animal::text <> btrim(status_animal::text)
      OR btrim(status_animal::text) IN ('', '0', '1', '2', '3', '4', 'OBSERVACAO')
  );

ALTER TABLE public.animal
    ADD CONSTRAINT eanimal_status_animal_check
        CHECK (
            status_animal IS NULL OR
            status_animal::text = ANY (
                ARRAY['ATIVO', 'OBERVACAO', 'VENDIDO', 'OBITO', 'ABATIDO']::text[]
            )
        );
