CREATE OR REPLACE FUNCTION rename_table_if_exists(old_name TEXT, new_name TEXT)
RETURNS VOID AS
$$
BEGIN
    IF to_regclass(old_name) IS NOT NULL AND to_regclass(new_name) IS NULL THEN
        EXECUTE format('ALTER TABLE %I RENAME TO %I', old_name, new_name);
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION rename_sequence_if_exists(old_name TEXT, new_name TEXT)
RETURNS VOID AS
$$
BEGIN
    IF to_regclass(old_name) IS NOT NULL AND to_regclass(new_name) IS NULL THEN
        EXECUTE format('ALTER SEQUENCE %I RENAME TO %I', old_name, new_name);
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT rename_table_if_exists('eanimal', 'animal');
SELECT rename_table_if_exists('e_animal', 'animal');
SELECT rename_sequence_if_exists('eanimal_id_seq', 'animal_id_seq');
SELECT rename_sequence_if_exists('e_animal_id_seq', 'animal_id_seq');

SELECT rename_table_if_exists('ecategoria', 'categoria');
SELECT rename_table_if_exists('e_categoria', 'categoria');
SELECT rename_sequence_if_exists('ecategoria_id_seq', 'categoria_id_seq');
SELECT rename_sequence_if_exists('e_categoria_id_seq', 'categoria_id_seq');

SELECT rename_table_if_exists('einsumo', 'insumo');
SELECT rename_table_if_exists('e_insumo', 'insumo');
SELECT rename_sequence_if_exists('einsumo_id_seq', 'insumo_id_seq');
SELECT rename_sequence_if_exists('e_insumo_id_seq', 'insumo_id_seq');

SELECT rename_table_if_exists('elistas_tarefas', 'listas_tarefas');
SELECT rename_table_if_exists('e_listas_tarefas', 'listas_tarefas');
SELECT rename_sequence_if_exists('elistas_tarefas_id_seq', 'listas_tarefas_id_seq');
SELECT rename_sequence_if_exists('e_listas_tarefas_id_seq', 'listas_tarefas_id_seq');

SELECT rename_table_if_exists('elote', 'lote');
SELECT rename_table_if_exists('e_lote', 'lote');
SELECT rename_sequence_if_exists('elote_id_seq', 'lote_id_seq');
SELECT rename_sequence_if_exists('e_lote_id_seq', 'lote_id_seq');

SELECT rename_table_if_exists('emovimentacao_estoque', 'movimentacao_estoque');
SELECT rename_table_if_exists('e_movimentacao_estoque', 'movimentacao_estoque');
SELECT rename_sequence_if_exists('emovimentacao_estoque_id_seq', 'movimentacao_estoque_id_seq');
SELECT rename_sequence_if_exists('e_movimentacao_estoque_id_seq', 'movimentacao_estoque_id_seq');

SELECT rename_table_if_exists('eocorrencia_animal', 'ocorrencia_animal');
SELECT rename_table_if_exists('e_ocorrencia_animal', 'ocorrencia_animal');
SELECT rename_sequence_if_exists('eocorrencia_animal_id_seq', 'ocorrencia_animal_id_seq');
SELECT rename_sequence_if_exists('e_ocorrencia_animal_id_seq', 'ocorrencia_animal_id_seq');

SELECT rename_table_if_exists('eparceiro', 'parceiro');
SELECT rename_table_if_exists('e_parceiro', 'parceiro');
SELECT rename_sequence_if_exists('eparceiro_id_seq', 'parceiro_id_seq');
SELECT rename_sequence_if_exists('e_parceiro_id_seq', 'parceiro_id_seq');

SELECT rename_table_if_exists('eregistro_financeiro', 'registro_financeiro');
SELECT rename_table_if_exists('e_registro_financeiro', 'registro_financeiro');
SELECT rename_sequence_if_exists('eregistro_financeiro_id_seq', 'registro_financeiro_id_seq');
SELECT rename_sequence_if_exists('e_registro_financeiro_id_seq', 'registro_financeiro_id_seq');

SELECT rename_table_if_exists('esetor', 'setor');
SELECT rename_table_if_exists('e_setor', 'setor');
SELECT rename_sequence_if_exists('esetor_id_seq', 'setor_id_seq');
SELECT rename_sequence_if_exists('e_setor_id_seq', 'setor_id_seq');

SELECT rename_table_if_exists('etarefa', 'tarefa');
SELECT rename_table_if_exists('e_tarefa', 'tarefa');
SELECT rename_sequence_if_exists('etarefa_id_seq', 'tarefa_id_seq');
SELECT rename_sequence_if_exists('e_tarefa_id_seq', 'tarefa_id_seq');

SELECT rename_table_if_exists('etranasacao', 'transacao');
SELECT rename_table_if_exists('e_tranasacao', 'transacao');
SELECT rename_table_if_exists('etransacao', 'transacao');
SELECT rename_table_if_exists('e_transacao', 'transacao');
SELECT rename_sequence_if_exists('etranasacao_id_seq', 'transacao_id_seq');
SELECT rename_sequence_if_exists('e_tranasacao_id_seq', 'transacao_id_seq');
SELECT rename_sequence_if_exists('etransacao_id_seq', 'transacao_id_seq');
SELECT rename_sequence_if_exists('e_transacao_id_seq', 'transacao_id_seq');

SELECT rename_table_if_exists('eunidade_medida', 'unidade_medida');
SELECT rename_table_if_exists('e_unidade_medida', 'unidade_medida');
SELECT rename_sequence_if_exists('eunidade_medida_id_seq', 'unidade_medida_id_seq');
SELECT rename_sequence_if_exists('e_unidade_medida_id_seq', 'unidade_medida_id_seq');

SELECT rename_table_if_exists('eusuario', 'usuario');
SELECT rename_table_if_exists('e_usuario', 'usuario');
SELECT rename_sequence_if_exists('eusuario_id_seq', 'usuario_id_seq');
SELECT rename_sequence_if_exists('e_usuario_id_seq', 'usuario_id_seq');

SELECT rename_table_if_exists('evacinacao', 'vacinacao');
SELECT rename_table_if_exists('e_vacinacao', 'vacinacao');
SELECT rename_sequence_if_exists('evacinacao_id_seq', 'vacinacao_id_seq');
SELECT rename_sequence_if_exists('e_vacinacao_id_seq', 'vacinacao_id_seq');

DROP FUNCTION rename_sequence_if_exists(TEXT, TEXT);
DROP FUNCTION rename_table_if_exists(TEXT, TEXT);
