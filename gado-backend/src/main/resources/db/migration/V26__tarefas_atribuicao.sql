ALTER TABLE listas_tarefas ADD COLUMN usuario_id BIGINT;
ALTER TABLE listas_tarefas ADD CONSTRAINT fk_listas_tarefas_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id);
ALTER TABLE listas_tarefas ADD CONSTRAINT uq_listas_tarefas_usuario UNIQUE (usuario_id);

ALTER TABLE tarefa ADD COLUMN atribuido_por_email VARCHAR(255);
