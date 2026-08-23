package br.com.gado.services;

import br.com.gado.dto.parcerioDto.ParceiroCadastroDto;
import br.com.gado.dto.parcerioDto.ParceiroDto;
import br.com.gado.dto.parcerioDto.ParceiroPutDto;
import br.com.gado.entities.EParceiro;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnPerfilUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.IParceiro;
import br.com.gado.repositories.IUsuario;
import br.com.gado.util.DocumentoUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Cadastro de Parceiros (fornecedores/compradores). Mesma regra de acesso de Funcionários. */
@Service
public class SParceiro {

    private static final Set<EnPerfilUsuario> PERFIS_MODULO =
            EnumSet.of(EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE, EnPerfilUsuario.FINANCEIRO);

    private static final Set<EnPerfilUsuario> PERFIS_GERENCIAIS =
            EnumSet.of(EnPerfilUsuario.ADMINISTRADOR, EnPerfilUsuario.GERENTE);

    @Autowired
    private IParceiro parceiroInterface;

    @Autowired
    private IUsuario usuarioInterface;

    @Autowired
    private ModelMapper modelMapper;

    private EUsuario resolveUsuarioModulo(String emailUsuario) {
        if (emailUsuario == null || emailUsuario.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail do usuário responsável pela operação.");
        }
        EUsuario usuario = usuarioInterface.findByEmailAndStatus(emailUsuario.trim(), EnStatus.A)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (!PERFIS_MODULO.contains(usuario.getPerfil())) {
            throw new IllegalArgumentException(
                    "Apenas Administrador, Gerente ou Financeiro podem acessar o cadastro de parceiros.");
        }
        return usuario;
    }

    private void resolveUsuarioGerencial(String emailUsuario) {
        EUsuario usuario = resolveUsuarioModulo(emailUsuario);
        if (!PERFIS_GERENCIAIS.contains(usuario.getPerfil())) {
            throw new IllegalArgumentException("Apenas Administrador ou Gerente podem realizar esta ação.");
        }
    }

    public ParceiroDto buscaPorCPF_CNPJ(String cpfCnpj, String emailUsuario) {
        resolveUsuarioModulo(emailUsuario);
        EParceiro parceiro = parceiroInterface.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new EntityNotFoundException("nenhum parceiro encontrado com esse cpf/cnpj"));
        return modelMapper.map(parceiro, ParceiroDto.class);
    }

    public List<ParceiroDto> listarTodos(String tipoFiltro, String emailUsuario) {
        resolveUsuarioModulo(emailUsuario);
        return parceiroInterface.findByStatus(EnStatus.A).stream()
                .filter(p -> tipoFiltro == null || tipoFiltro.isBlank()
                        || p.getTipo().name().equalsIgnoreCase(tipoFiltro)
                        || p.getTipo().name().equals("AMBOS"))
                .map(p -> modelMapper.map(p, ParceiroDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ParceiroDto cadastra(ParceiroCadastroDto dto, String emailUsuario) {
        resolveUsuarioGerencial(emailUsuario);

        if (!DocumentoUtil.isCpfOuCnpjValido(dto.getCPF_CNPJ())) {
            throw new IllegalArgumentException("CPF/CNPJ inválido.");
        }

        boolean existe = parceiroInterface.existsByCpfCnpj(dto.getCPF_CNPJ());
        if (existe) {
            throw new IllegalArgumentException("Parceiro já existe no sistema");
        }

        EParceiro parceiro = modelMapper.map(dto, EParceiro.class);
        parceiro.setDataCadastro(LocalDateTime.now());

        EParceiro parceiroSalvo = parceiroInterface.save(parceiro);
        return modelMapper.map(parceiroSalvo, ParceiroDto.class);
    }

    @Transactional
    public String deleta(String cpfCnpj, String emailUsuario) {
        resolveUsuarioGerencial(emailUsuario);

        if (!parceiroInterface.existsByCpfCnpj(cpfCnpj)) {
            return "Esse cpf/cnpj não existe no banco de dados";
        }

        try {
            parceiroInterface.deleteByCpfCnpj(cpfCnpj);
            return "Parceiro deletado com sucesso";
        } catch (Exception e) {
            return "Não é possível excluir ele pois ele possui vinculos com outras entidades";
        }
    }

    @Transactional
    public ParceiroDto altera(String cpfCnpj, ParceiroPutDto dto, String emailUsuario) {
        resolveUsuarioGerencial(emailUsuario);

        EParceiro parceiro = parceiroInterface.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new EntityNotFoundException("Parceiro não existe no sistema"));

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, parceiro);
        EParceiro parceiroAtualizado = parceiroInterface.save(parceiro);
        return modelMapper.map(parceiroAtualizado, ParceiroDto.class);
    }
}
