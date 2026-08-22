package br.com.gado.services;

import br.com.gado.dto.notaFiscalDto.NotaFiscalResumoDto;
import br.com.gado.entities.EDocumentoEntrada;
import br.com.gado.entities.EDocumentoSaida;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IDocumentoEntrada;
import br.com.gado.repositories.IDocumentoSaida;
import br.com.gado.repositories.IUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Listagem unificada da tela de "Notas Fiscais": une EDocumentoEntrada (direção ENTRADA — NF-e
 * importada ou recibo simples) e EDocumentoSaida (direção RECEITA — venda de leite/animal), com
 * filtros por número, chave de acesso e direção.
 */
@Service
public class SNotaFiscal {

    private static final Set<EnPerfilUsuario> PERFIS_MODULO =
            EnumSet.of(EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE, EnPerfilUsuario.FINANCEIRO);

    @Autowired
    private IDocumentoEntrada documentoEntradaInterface;

    @Autowired
    private IDocumentoSaida documentoSaidaInterface;

    @Autowired
    private IUsuario usuarioInterface;

    private void validaAcessoModulo(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (!PERFIS_MODULO.contains(usuario.getPerfil())) {
            throw new IllegalArgumentException(
                    "Apenas Administrador, Gerente ou Financeiro podem acessar o módulo financeiro.");
        }
    }

    @Transactional
    public List<NotaFiscalResumoDto> listar(String numero, String chave, String direcao, String emailUsuario) {
        validaAcessoModulo(emailUsuario);

        String numeroTermo = numero == null ? "" : numero.trim().toLowerCase();
        String chaveTermo = chave == null ? "" : chave.trim().toLowerCase();
        String direcaoFiltro = direcao == null ? "" : direcao.trim().toUpperCase();

        Stream<NotaFiscalResumoDto> entradas = "RECEITA".equals(direcaoFiltro)
                ? Stream.empty()
                : documentoEntradaInterface.findByStatusOrderByDataEntradaDesc(EnStatus.A).stream()
                        .map(this::toResumoDto);

        Stream<NotaFiscalResumoDto> saidas = "ENTRADA".equals(direcaoFiltro)
                ? Stream.empty()
                : documentoSaidaInterface.findByStatusOrderByDataEmissaoDesc(EnStatus.A).stream()
                        .map(this::toResumoDto);

        return Stream.concat(entradas, saidas)
                .filter(nf -> numeroTermo.isBlank()
                        || (nf.getNumeroDocumento() != null && nf.getNumeroDocumento().toLowerCase().contains(numeroTermo)))
                .filter(nf -> chaveTermo.isBlank()
                        || (nf.getChaveAcesso() != null && nf.getChaveAcesso().toLowerCase().contains(chaveTermo)))
                .sorted(Comparator.comparing(NotaFiscalResumoDto::getDataEmissao,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private NotaFiscalResumoDto toResumoDto(EDocumentoEntrada documento) {
        return new NotaFiscalResumoDto(
                documento.getId(), "ENTRADA", documento.getTipoDocumento().name(),
                documento.getNumeroDocumento(), documento.getChaveAcessoNfe(),
                documento.getDataEmissao(), documento.getValorTotal(),
                documento.getStatusAprovacao().name());
    }

    private NotaFiscalResumoDto toResumoDto(EDocumentoSaida documento) {
        return new NotaFiscalResumoDto(
                documento.getId(), "RECEITA", documento.getTipoDocumento().name(),
                documento.getNumeroDocumento(), documento.getChaveAcesso(),
                documento.getDataEmissao(), documento.getValorTotal(), "CONFIRMADO");
    }
}
