-- V7 introduziu o valor "OBERVACAO" (typo de "OBSERVACAO") e travou isso com um CHECK constraint.
-- Corrige o typo definitivamente: dados existentes, constraint e valor aceito.

ALTER TABLE IF EXISTS public.animal
    DROP CONSTRAINT IF EXISTS eanimal_status_animal_check;

UPDATE public.animal
SET status_animal = 'OBSERVACAO'
WHERE status_animal = 'OBERVACAO';

ALTER TABLE public.animal
    ADD CONSTRAINT eanimal_status_animal_check
        CHECK (
            status_animal IS NULL OR
            status_animal::text = ANY (
                ARRAY['ATIVO', 'OBSERVACAO', 'VENDIDO', 'OBITO', 'ABATIDO']::text[]
            )
        );
