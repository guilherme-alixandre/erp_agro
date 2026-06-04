package br.com.gado.services;

import br.com.gado.dto.RegistroFinanceiroDTO;
import br.com.gado.entities.ECategoria;
import br.com.gado.entities.ERegistroFinanceiro;
import br.com.gado.entities.EUsuario;
import br.com.gado.enums.EnStatus;
import br.com.gado.repositories.ICategoria;
import br.com.gado.repositories.IRegistroFinanceiro;
import br.com.gado.repositories.IUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SRegistroFinanceiro {

    private final IRegistroFinanceiro registroFinanceiroInterface;
    private final IUsuario usuarioInterface;
    private final ICategoria categoriaInterface;
    private static final Logger log = LoggerFactory.getLogger(SRegistroFinanceiro.class);
    private final ModelMapper modelMapper;

    public SRegistroFinanceiro(IRegistroFinanceiro registroFinanceiroInterface, IUsuario usuarioInterface, ICategoria categoriaInterface, ModelMapper modelMapper) {
        this.registroFinanceiroInterface = registroFinanceiroInterface;
        this.usuarioInterface = usuarioInterface;
        this.categoriaInterface = categoriaInterface;
        this.modelMapper = modelMapper;
    }

    public RegistroFinanceiroDTO criarRegistroFinanceiro(RegistroFinanceiroDTO registroFinanceiroDto) {
        EUsuario existingUsuario = this.usuarioInterface
                .findByEmailAndStatus(registroFinanceiroDto.getUsuarioId().getEmail(), registroFinanceiroDto.getUsuarioId().getStatus())
                .orElseThrow(EntityNotFoundException::new);

        ECategoria existingCategoria = this.categoriaInterface
                .findById(registroFinanceiroDto.getCategoriaId().getId())
                .orElseThrow(EntityNotFoundException::new);

        ERegistroFinanceiro novoRegistroFinanceiro = modelMapper.map(registroFinanceiroDto, ERegistroFinanceiro.class);
        novoRegistroFinanceiro.setUsuarioId(existingUsuario);
        novoRegistroFinanceiro.setCategoriaId(existingCategoria);

        try {
            ERegistroFinanceiro registroFinanceiroSalvo = this.registroFinanceiroInterface.save(novoRegistroFinanceiro);
            return modelMapper.map(registroFinanceiroSalvo, RegistroFinanceiroDTO.class);
        } catch (Exception e) {
            log.error("Erro ao salvar registro financeiro com Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RegistroFinanceiroDTO buscarRegistroFinanceiroPorId(Long registroFinanceiroId) {
        ERegistroFinanceiro existingEntity = this.registroFinanceiroInterface
                .findById(registroFinanceiroId)
                .orElseThrow(EntityNotFoundException::new);
        return modelMapper.map(existingEntity, RegistroFinanceiroDTO.class);
    }

    public RegistroFinanceiroDTO atualizarRegistroFinanceiroPorId(Long registroFinanceiroId, RegistroFinanceiroDTO registroFinanceiroDto) {
        ERegistroFinanceiro existingEntity = this.registroFinanceiroInterface
                .findById(registroFinanceiroId)
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
        this.modelMapper.map(registroFinanceiroDto, existingEntity);

        try {
            ERegistroFinanceiro registroFinanceiroAtualizado = this.registroFinanceiroInterface.save(existingEntity);
            return modelMapper.map(registroFinanceiroAtualizado, RegistroFinanceiroDTO.class);
        } catch (Exception e) {
            log.error("Erro ao atualizar registro financeiro com Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String excluirRegistroFinanceiroPorId(Long registroFinanceiroId) {
        ERegistroFinanceiro existingEntity = this.registroFinanceiroInterface
                .findById(registroFinanceiroId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntity.setStatus(EnStatus.I);

        try {
            this.registroFinanceiroInterface.save(existingEntity);
            return "registro financeiro excluído com sucesso";
        } catch (Exception e) {
            log.error("Erro ao excluir registro financeiro com Id: {}", e.getMessage(), e);
            return "erro ao excluir registro financeiro";
        }
    }
}
