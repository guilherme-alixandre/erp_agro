package br.com.gado.application.services;

import br.com.gado.application.dto.metaSetorDto.MedicaoMetaCadastroDto;
import br.com.gado.application.dto.metaSetorDto.MedicaoMetaPutDto;
import br.com.gado.application.dto.metaSetorDto.MedicaoMetaRespostaDto;
import br.com.gado.application.dto.metaSetorDto.MetaSetorCadastroDto;
import br.com.gado.application.dto.metaSetorDto.MetaSetorPutDto;
import br.com.gado.application.dto.metaSetorDto.MetaSetorRespostaDto;
import br.com.gado.domain.entities.EMedicaoMeta;
import br.com.gado.domain.entities.EMetaSetor;
import br.com.gado.domain.entities.ESetor;
import br.com.gado.domain.entities.ELote;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnPerfilUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.domain.enums.EnTipoMeta;
import br.com.gado.infrastructure.persistence.repositories.IMedicaoMeta;
import br.com.gado.infrastructure.persistence.repositories.IMetaSetor;
import br.com.gado.infrastructure.persistence.repositories.ISetor;
import br.com.gado.infrastructure.persistence.repositories.ILote;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SMetaSetor {

    // ── Constante de conversão de arrobas ─────────────────────────────────────
    // 1 arroba = 15 kg (padrão brasileiro)
    private static final double KG_POR_ARROBA = 15.0;

    @Autowired
    private IMetaSetor metaSetorInterface;

    @Autowired
    private IMedicaoMeta medicaoMetaInterface;

    @Autowired
    private ISetor setorInterface;

    @Autowired
    private ILote loteInterface;

    @Autowired
    private IUsuario usuarioInterface;

    // ── Validação de acesso ───────────────────────────────────────────────────

    /**
     * Valida que o usuário (por email) tem perfil ADMINISTRADOR ou GERENTE.
     * Utilizado nas operações de cadastro/edição de MetaSetor.
     */
    public void validaAdminOuGerente(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Apenas Administradores e Gerentes podem realizar esta ação.");
        }
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Apenas Administradores e Gerentes podem realizar esta ação."));

        if (usuario.getPerfil() != EnPerfilUsuario.ADMINISTRADOR
                && usuario.getPerfil() != EnPerfilUsuario.GERENTE) {
            throw new IllegalArgumentException(
                    "Apenas Administradores e Gerentes podem realizar esta ação.");
        }
    }

    /**
     * Valida que o usuário tem perfil ADMINISTRADOR, GERENTE ou CASEIRO.
     * Utilizado nas operações de cadastro de MedicaoMeta.
     */
    public void validaQualquerPerfil(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("É necessário informar o e-mail do usuário.");
        }
        usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    /**
     * Valida se o usuário tem permissão para editar uma medição específica.
     * ADMINISTRADOR, GERENTE e CUIDADOR_CHEFE podem editar qualquer medição.
     * CUIDADOR só pode editar a medição que ele mesmo criou.
     */
    public void validaEdicaoMedicao(String emailUsuario, EMedicaoMeta medicao) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("É necessário informar o e-mail do usuário.");
        }
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        EnPerfilUsuario perfil = usuario.getPerfil();

        if (perfil == EnPerfilUsuario.ADMINISTRADOR
                || perfil == EnPerfilUsuario.GERENTE
                || perfil == EnPerfilUsuario.CUIDADOR_CHEFE) {
            return;
        }

        if (perfil == EnPerfilUsuario.CUIDADOR) {
            if (medicao.getCriadoPorEmail() == null
                    || !emailUsuario.trim().equalsIgnoreCase(medicao.getCriadoPorEmail())) {
                throw new IllegalArgumentException("Você só pode editar medições que você mesmo criou.");
            }
            return;
        }

        throw new IllegalArgumentException("Seu perfil não permite editar medições.");
    }

    // ── MetaSetor ─────────────────────────────────────────────────────────────

    public List<MetaSetorRespostaDto> listarPorSetor(Long setorId) {
        return metaSetorInterface.findBySetor_Id(setorId)
                .stream()
                .map(this::toRespostaDto)
                .toList();
    }

    public MetaSetorRespostaDto buscarPorId(Long id) {
        EMetaSetor meta = metaSetorInterface.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meta não encontrada para o ID: " + id));
        return toRespostaDto(meta);
    }

    @Transactional
    public String cadastrar(MetaSetorCadastroDto dto) {
        validarDtoMeta(dto);

        ESetor setor = setorInterface.findById(dto.getSetorId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Setor não encontrado para o ID: " + dto.getSetorId()));

        EMetaSetor meta = new EMetaSetor();
        meta.setSetor(setor);
        meta.setDataInicial(dto.getDataInicial());
        meta.setDataFinal(dto.getDataFinal());
        meta.setTipoMeta(dto.getTipoMeta());
        meta.setQuantidadeEsperada(dto.getQuantidadeEsperada());
        meta.setPrecoMedio(dto.getPrecoMedio());

        // tipoGado é nulo para LEITE, obrigatório para ARROBA (já validado acima)
        if (dto.getTipoMeta() == EnTipoMeta.LEITE) {
            meta.setTipoGado(null);
        } else {
            meta.setTipoGado(dto.getTipoGado());
        }

        metaSetorInterface.save(meta);
        return "Meta do setor cadastrada com sucesso.";
    }

    @Transactional
    public String alterar(Long id, MetaSetorPutDto dto) {
        EMetaSetor meta = metaSetorInterface.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meta não encontrada para o ID: " + id));

        if (dto.getDataInicial() != null) meta.setDataInicial(dto.getDataInicial());
        if (dto.getDataFinal() != null) meta.setDataFinal(dto.getDataFinal());
        if (dto.getQuantidadeEsperada() != null) meta.setQuantidadeEsperada(dto.getQuantidadeEsperada());
        if (dto.getPrecoMedio() != null) meta.setPrecoMedio(dto.getPrecoMedio());

        // Só permite alterar tipoGado em metas do tipo ARROBA
        if (dto.getTipoGado() != null) {
            if (meta.getTipoMeta() == EnTipoMeta.LEITE) {
                throw new IllegalArgumentException(
                        "Metas do tipo LEITE não possuem tipo de gado.");
            }
            meta.setTipoGado(dto.getTipoGado());
        }

        // Validação cruzada de datas após a edição
        if (meta.getDataFinal() != null && meta.getDataInicial() != null
                && meta.getDataFinal().isBefore(meta.getDataInicial())) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }

        metaSetorInterface.save(meta);
        return "Meta do setor atualizada com sucesso.";
    }

    @Transactional
    public String deletar(Long id) {
        if (!metaSetorInterface.existsById(id)) {
            return "Meta não encontrada para o ID: " + id;
        }
        metaSetorInterface.deleteById(id);
        return "Meta do setor removida com sucesso.";
    }

    // ── MedicaoMeta ───────────────────────────────────────────────────────────

    @Transactional
    public String cadastrarMedicao(MedicaoMetaCadastroDto dto, String emailCriador) {
        EMetaSetor meta = metaSetorInterface.findById(dto.getMetaSetorId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Meta não encontrada para o ID: " + dto.getMetaSetorId()));

        ELote lote = loteInterface.findById(dto.getLoteId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Lote não encontrado para o ID: " + dto.getLoteId()));

        EMedicaoMeta medicao = new EMedicaoMeta();
        medicao.setMetaSetor(meta);
        medicao.setLote(lote);
        medicao.setDataMedicao(dto.getDataMedicao());
        medicao.setQuantidadeLancada(dto.getQuantidadeLancada());
        medicao.setCriadoPorEmail(emailCriador != null ? emailCriador.trim() : null);

        medicaoMetaInterface.save(medicao);
        return "Medição cadastrada com sucesso.";
    }

    @Transactional
    public String validarEAtualizarMedicao(Long medicaoId, MedicaoMetaPutDto dto, String emailUsuario) {
        EMedicaoMeta medicao = medicaoMetaInterface.findById(medicaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Medição não encontrada para o ID: " + medicaoId));

        validaEdicaoMedicao(emailUsuario, medicao);

        if (dto.getLoteId() != null) {
            ELote lote = loteInterface.findById(dto.getLoteId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Lote não encontrado para o ID: " + dto.getLoteId()));
            medicao.setLote(lote);
        }
        if (dto.getDataMedicao() != null) medicao.setDataMedicao(dto.getDataMedicao());
        if (dto.getQuantidadeLancada() != null) medicao.setQuantidadeLancada(dto.getQuantidadeLancada());

        medicaoMetaInterface.save(medicao);
        return "Medição atualizada com sucesso.";
    }

    @Transactional
    public String deletarMedicao(Long medicaoId) {
        if (!medicaoMetaInterface.existsById(medicaoId)) {
            return "Medição não encontrada para o ID: " + medicaoId;
        }
        medicaoMetaInterface.deleteById(medicaoId);
        return "Medição removida com sucesso.";
    }

    // ── Lógica de cálculo de progresso ───────────────────────────────────────

    /**
     * Converte uma medição bruta para a unidade-alvo da meta.
     *
     * <p><b>LEITE:</b> sem conversão — a quantidade lançada já é em Litros.
     *   Retorna {@code quantidadeLancada} diretamente.</p>
     *
     * <p><b>ARROBA:</b> o lançamento é o Peso Vivo do animal em Kg.
     *   Aplica a fórmula:<br>
     *   {@code arrobas = (pesoVivoKg * taxaRendimento) / KG_POR_ARROBA}<br>
     *   onde {@code taxaRendimento} vem do {@link br.com.gado.domain.enums.EnTipoGado}
     *   associado à meta (0.50, 0.475 ou 0.56).</p>
     */
    private double converterQuantidade(EMedicaoMeta medicao, EMetaSetor meta) {
        if (meta.getTipoMeta() == EnTipoMeta.LEITE) {
            return medicao.getQuantidadeLancada();
        }
        // ARROBA: (Peso Vivo * Taxa de Rendimento) / 15 kg
        double taxa = meta.getTipoGado().getTaxaRendimento();
        return (medicao.getQuantidadeLancada() * taxa) / KG_POR_ARROBA;
    }

    private MetaSetorRespostaDto toRespostaDto(EMetaSetor meta) {
        MetaSetorRespostaDto dto = new MetaSetorRespostaDto();
        dto.setId(meta.getId());
        dto.setSetorId(meta.getSetor().getId());
        dto.setSetorNome(meta.getSetor().getNome());
        dto.setDataInicial(meta.getDataInicial());
        dto.setDataFinal(meta.getDataFinal());
        dto.setTipoMeta(meta.getTipoMeta());
        dto.setQuantidadeEsperada(meta.getQuantidadeEsperada());
        dto.setPrecoMedio(meta.getPrecoMedio());
        dto.setTipoGado(meta.getTipoGado());

        // ── Carregar medições e calcular progresso ──────────────────────────
        List<EMedicaoMeta> medicoes = medicaoMetaInterface.findByMetaSetor_Id(meta.getId());

        double totalRealizado = medicoes.stream()
                .mapToDouble(m -> converterQuantidade(m, meta))
                .sum();

        double percentual = meta.getQuantidadeEsperada() > 0
                ? (totalRealizado / meta.getQuantidadeEsperada()) * 100.0
                : 0.0;

        dto.setQuantidadeRealizada(arredondar(totalRealizado));
        dto.setPercentualProgresso(arredondar(percentual));
        dto.setValorRealizado(arredondar(totalRealizado * meta.getPrecoMedio()));
        dto.setValorEsperado(arredondar(meta.getQuantidadeEsperada() * meta.getPrecoMedio()));

        // ── Montar lista de medições com quantidade convertida ──────────────
        List<MedicaoMetaRespostaDto> medicaoDtos = medicoes.stream()
                .map(m -> {
                    MedicaoMetaRespostaDto mDto = new MedicaoMetaRespostaDto();
                    mDto.setId(m.getId());
                    mDto.setLoteId(m.getLote().getId());
                    mDto.setLoteDescricao(m.getLote().getDescricao());
                    mDto.setDataMedicao(m.getDataMedicao());
                    mDto.setQuantidadeLancada(m.getQuantidadeLancada());
                    mDto.setQuantidadeConvertida(arredondar(converterQuantidade(m, meta)));
                    mDto.setCriadoPorEmail(m.getCriadoPorEmail());
                    return mDto;
                })
                .toList();

        dto.setMedicoes(medicaoDtos);
        return dto;
    }

    // ── Validações de negócio ─────────────────────────────────────────────────

    private void validarDtoMeta(MetaSetorCadastroDto dto) {
        if (dto.getDataFinal().isBefore(dto.getDataInicial())) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }
        if (dto.getTipoMeta() == EnTipoMeta.ARROBA && dto.getTipoGado() == null) {
            throw new IllegalArgumentException(
                    "O tipo de gado é obrigatório para metas do tipo ARROBA.");
        }
        if (dto.getTipoMeta() == EnTipoMeta.LEITE && dto.getTipoGado() != null) {
            // Aviso tolerante: aceita, mas o Service ignora/anula o campo
            // Poderia lançar exceção caso queira ser mais rígido
        }
    }

    // ── Utilitário ────────────────────────────────────────────────────────────

    private static double arredondar(double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
