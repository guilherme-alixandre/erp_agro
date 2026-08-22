package br.com.gado.services;

import br.com.gado.dto.metaSetorDto.MedicaoMetaCadastroDto;
import br.com.gado.dto.metaSetorDto.MedicaoMetaPutDto;
import br.com.gado.dto.metaSetorDto.MedicaoMetaRespostaDto;
import br.com.gado.dto.metaSetorDto.MetaSetorCadastroDto;
import br.com.gado.dto.metaSetorDto.MetaSetorPutDto;
import br.com.gado.dto.metaSetorDto.MetaSetorRespostaDto;
import br.com.gado.entities.EMedicaoMeta;
import br.com.gado.entities.EMetaSetor;
import br.com.gado.entities.ELote;
import br.com.gado.entities.ESetor;
import br.com.gado.entities.EUsuario;
import br.com.gado.entities.EVendaMetaLote;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.enums.EnTipoMeta;
import br.com.gado.repositories.ILote;
import br.com.gado.repositories.IMedicaoMeta;
import br.com.gado.repositories.IMetaSetor;
import br.com.gado.repositories.ISetor;
import br.com.gado.repositories.IUsuario;
import br.com.gado.repositories.IVendaMetaLote;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SMetaSetor {

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

    @Autowired
    private IVendaMetaLote vendaMetaLoteInterface;

    // ── Validação de acesso ───────────────────────────────────────────────

    /** Restrito a ADMINISTRADOR e GERENTE — cadastro/edição/exclusão de MetaSetor. */
    public void validaAdminOuGerente(String emailUsuario) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);
        if (usuario.getPerfil() != EnPerfilUsuario.ADMINISTRADOR
                && usuario.getPerfil() != EnPerfilUsuario.GERENTE) {
            throw new IllegalArgumentException("Apenas Administradores e Gerentes podem realizar esta ação.");
        }
    }

    /** Qualquer usuário ativo pode registrar uma medição. */
    public void validaUsuarioAtivo(String emailUsuario) {
        resolveUsuarioObrigatorio(emailUsuario);
    }

    /**
     * Permissão para editar/excluir uma medição específica:
     * - ADMINISTRADOR e GERENTE: qualquer medição.
     * - CUIDADOR_CHEFE: apenas medições criadas por CUIDADOR ou CUIDADOR_CHEFE.
     * - CUIDADOR: apenas as que ele mesmo criou.
     */
    public void validaEdicaoMedicao(String emailUsuario, EMedicaoMeta medicao) {
        EUsuario usuario = resolveUsuarioObrigatorio(emailUsuario);
        EnPerfilUsuario perfil = usuario.getPerfil();

        if (perfil == EnPerfilUsuario.ADMINISTRADOR || perfil == EnPerfilUsuario.GERENTE) {
            return;
        }

        if (perfil == EnPerfilUsuario.CUIDADOR_CHEFE) {
            EnPerfilUsuario perfilCriador = resolverPerfilPorEmail(medicao.getCriadoPorEmail());
            if (perfilCriador == EnPerfilUsuario.ADMINISTRADOR || perfilCriador == EnPerfilUsuario.GERENTE) {
                throw new IllegalArgumentException(
                        "Cuidadores Chefe não podem alterar medições criadas por Administradores ou Gerentes.");
            }
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

    private EUsuario resolveUsuarioObrigatorio(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        return usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    // ── MetaSetor ─────────────────────────────────────────────────────────

    public List<MetaSetorRespostaDto> listarPorSetor(Long setorId) {
        return metaSetorInterface.findBySetor_IdAndStatus(setorId, EnStatus.A)
                .stream()
                .map(this::toRespostaDto)
                .collect(Collectors.toList());
    }

    public MetaSetorRespostaDto buscarPorId(Long id) {
        EMetaSetor meta = metaSetorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Meta não encontrada para o ID: " + id));
        return toRespostaDto(meta);
    }

    @Transactional
    public String cadastrar(MetaSetorCadastroDto dto) {
        validarDtoMeta(dto);

        ESetor setor = setorInterface.findByIdAndStatus(dto.getSetorId(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Setor não encontrado ou inativo: ID " + dto.getSetorId()));

        EMetaSetor meta = new EMetaSetor();
        meta.setSetor(setor);
        meta.setDataInicial(dto.getDataInicial());
        meta.setDataFinal(dto.getDataFinal());
        meta.setTipoMeta(dto.getTipoMeta());
        meta.setQuantidadeEsperada(dto.getQuantidadeEsperada());
        meta.setPrecoMedio(dto.getPrecoMedio());
        meta.setTipoGado(dto.getTipoMeta() == EnTipoMeta.LEITE ? null : dto.getTipoGado());

        metaSetorInterface.save(meta);
        return "Meta do setor cadastrada com sucesso.";
    }

    @Transactional
    public String alterar(Long id, MetaSetorPutDto dto) {
        EMetaSetor meta = metaSetorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Meta não encontrada para o ID: " + id));

        if (dto.getDataInicial() != null) meta.setDataInicial(dto.getDataInicial());
        if (dto.getDataFinal() != null) meta.setDataFinal(dto.getDataFinal());
        if (dto.getQuantidadeEsperada() != null) meta.setQuantidadeEsperada(dto.getQuantidadeEsperada());
        if (dto.getPrecoMedio() != null) meta.setPrecoMedio(dto.getPrecoMedio());

        if (dto.getTipoGado() != null) {
            if (meta.getTipoMeta() == EnTipoMeta.LEITE) {
                throw new IllegalArgumentException("Metas do tipo LEITE não possuem tipo de gado.");
            }
            meta.setTipoGado(dto.getTipoGado());
        }

        if (meta.getDataFinal() != null && meta.getDataInicial() != null
                && meta.getDataFinal().isBefore(meta.getDataInicial())) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }

        metaSetorInterface.save(meta);
        return "Meta do setor atualizada com sucesso.";
    }

    @Transactional
    public String deletar(Long id) {
        EMetaSetor meta = metaSetorInterface.findByIdAndStatus(id, EnStatus.A)
                .orElseThrow(() -> new EntityNotFoundException("Meta não encontrada para o ID: " + id));

        meta.setStatus(EnStatus.I);
        metaSetorInterface.save(meta);
        return "Meta do setor removida com sucesso.";
    }

    // ── MedicaoMeta ───────────────────────────────────────────────────────

    @Transactional
    public String cadastrarMedicao(MedicaoMetaCadastroDto dto, String emailCriador) {
        EMetaSetor meta = metaSetorInterface.findByIdAndStatus(dto.getMetaSetorId(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Meta não encontrada para o ID: " + dto.getMetaSetorId()));

        ELote lote = loteInterface.findByIdAndStatus(dto.getLoteId(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Lote não encontrado ou inativo: ID " + dto.getLoteId()));

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
                .orElseThrow(() -> new IllegalArgumentException("Medição não encontrada para o ID: " + medicaoId));

        validaEdicaoMedicao(emailUsuario, medicao);

        if (dto.getLoteId() != null) {
            ELote lote = loteInterface.findByIdAndStatus(dto.getLoteId(), EnStatus.A)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Lote não encontrado ou inativo: ID " + dto.getLoteId()));
            medicao.setLote(lote);
        }
        if (dto.getDataMedicao() != null) medicao.setDataMedicao(dto.getDataMedicao());
        if (dto.getQuantidadeLancada() != null) medicao.setQuantidadeLancada(dto.getQuantidadeLancada());

        medicaoMetaInterface.save(medicao);
        return "Medição atualizada com sucesso.";
    }

    @Transactional
    public String validarEDeletarMedicao(Long medicaoId, String emailUsuario) {
        EMedicaoMeta medicao = medicaoMetaInterface.findById(medicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Medição não encontrada para o ID: " + medicaoId));

        validaEdicaoMedicao(emailUsuario, medicao);

        medicaoMetaInterface.deleteById(medicaoId);
        return "Medição removida com sucesso.";
    }

    // ── Cálculo de progresso ─────────────────────────────────────────────

    private double converterQuantidade(EMedicaoMeta medicao, EMetaSetor meta) {
        if (meta.getTipoMeta() == EnTipoMeta.LEITE) {
            return medicao.getQuantidadeLancada();
        }
        double taxa = meta.getTipoGado().getTaxaRendimento();
        return (medicao.getQuantidadeLancada() * taxa) / KG_POR_ARROBA;
    }

    private String resolverNomePorEmail(String email, Map<String, String> cache) {
        if (email == null || email.isBlank()) return null;
        return cache.computeIfAbsent(email, e ->
                usuarioInterface.findByEmailAndStatus(e, EnStatus.A).map(EUsuario::getNome).orElse(null));
    }

    private EnPerfilUsuario resolverPerfilPorEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return usuarioInterface.findByEmailAndStatus(email.trim(), EnStatus.A)
                .map(EUsuario::getPerfil)
                .orElse(null);
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
        dto.setStatus(meta.getStatus());

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

        if (meta.getTipoMeta() == EnTipoMeta.LEITE) {
            double totalVendido = vendaMetaLoteInterface.findByMetaSetor_Id(meta.getId()).stream()
                    .mapToDouble(EVendaMetaLote::getLitrosVendidos)
                    .sum();
            double percentualVendido = meta.getQuantidadeEsperada() > 0
                    ? (totalVendido / meta.getQuantidadeEsperada()) * 100.0
                    : 0.0;
            dto.setQuantidadeVendida(arredondar(totalVendido));
            dto.setPercentualVendido(arredondar(percentualVendido));
        }

        Map<String, String> nomesPorEmail = new HashMap<>();
        Map<String, EnPerfilUsuario> perfisPorEmail = new HashMap<>();
        List<MedicaoMetaRespostaDto> medicaoDtos = medicoes.stream().map(m -> {
            MedicaoMetaRespostaDto mDto = new MedicaoMetaRespostaDto();
            mDto.setId(m.getId());
            mDto.setLoteId(m.getLote().getId());
            mDto.setLoteCodigo(m.getLote().getCodigo());
            mDto.setLoteDescricao(m.getLote().getDescricao());
            mDto.setDataMedicao(m.getDataMedicao());
            mDto.setQuantidadeLancada(m.getQuantidadeLancada());
            mDto.setQuantidadeConvertida(arredondar(converterQuantidade(m, meta)));
            mDto.setCriadoPorEmail(m.getCriadoPorEmail());
            mDto.setCriadoPorNome(resolverNomePorEmail(m.getCriadoPorEmail(), nomesPorEmail));
            EnPerfilUsuario perfilCriador = perfisPorEmail.computeIfAbsent(
                    m.getCriadoPorEmail() != null ? m.getCriadoPorEmail() : "",
                    e -> e.isBlank() ? null : resolverPerfilPorEmail(e));
            mDto.setCriadoPorPerfil(perfilCriador != null ? perfilCriador.name() : null);
            return mDto;
        }).collect(Collectors.toList());

        dto.setMedicoes(medicaoDtos);
        return dto;
    }

    // ── Validações de negócio ────────────────────────────────────────────

    private void validarDtoMeta(MetaSetorCadastroDto dto) {
        if (dto.getDataFinal().isBefore(dto.getDataInicial())) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }
        if (dto.getTipoMeta() == EnTipoMeta.ARROBA && dto.getTipoGado() == null) {
            throw new IllegalArgumentException("O tipo de gado é obrigatório para metas do tipo ARROBA.");
        }
    }

    private static double arredondar(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
