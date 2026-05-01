ALTER TABLE vacinacao ADD COLUMN IF NOT EXISTS animal_id BIGINT;
ALTER TABLE vacinacao ADD CONSTRAINT fk_vacinacao_animal FOREIGN KEY (animal_id) REFERENCES animal (id);
