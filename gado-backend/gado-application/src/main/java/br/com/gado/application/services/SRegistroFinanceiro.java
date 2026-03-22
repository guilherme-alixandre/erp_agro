package br.com.gado.application.services;

import br.com.gado.domain.entities.ECategoria;
import br.com.gado.domain.entities.ERegistroFinanceiro;
import br.com.gado.domain.entities.EUsuario;
import br.com.gado.domain.enums.EnStatus;
import br.com.gado.dto.RegistroFinanceiroDTO;
import br.com.gado.infrastructure.persistence.repositories.ICategoria;
import br.com.gado.infrastructure.persistence.repositories.IRegistroFinanceiro;
import br.com.gado.infrastructure.persistence.repositories.IUsuario;
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
                .findByEmail(registroFinanceiroDto.getUsuarioId().getEmail())
                .orElseThrow(EntityNotFoundException::new);

        ECategoria existingCategoria = this.categoriaInterface
                .findByCategoriaId(registroFinanceiroDto.getCategoriaId().getId())
                .orElseThrow(EntityNotFoundException::new);

        ERegistroFinanceiro novoRegistroFinanceiro = new ERegistroFinanceiro();

        novoRegistroFinanceiro.setUsuarioId(existingUsuario);
        novoRegistroFinanceiro.setCategoriaId(existingCategoria);

        novoRegistroFinanceiro.setDescricao(registroFinanceiroDto.getDescricao());
        novoRegistroFinanceiro.setTipoDespesa(registroFinanceiroDto.getTipoDespesa());
        novoRegistroFinanceiro.setValor(registroFinanceiroDto.getValor());
        novoRegistroFinanceiro.setDataPagamento(registroFinanceiroDto.getDataPagamento());
        novoRegistroFinanceiro.setDataVencimento(registroFinanceiroDto.getDataVencimento());
        novoRegistroFinanceiro.setStatusDespesa(registroFinanceiroDto.getStatusDespesa());

        try{
            ERegistroFinanceiro registroFinanceiroSalvo = this.registroFinanceiroInterface.save(novoRegistroFinanceiro);
            return modelMapper.map(registroFinanceiroSalvo, RegistroFinanceiroDTO.class);
        } catch (Exception e){
            log.error("Erro ao salvar registro financeiro com Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RegistroFinanceiroDTO buscarRegistroFinanceiroPorId(RegistroFinanceiroDTO registroFinanceiroDto) {
        ERegistroFinanceiro existingEntitye = this.registroFinanceiroInterface
                .buscarRegistroFinanceiroPorId(registroFinanceiroDto.getId())
                .orElseThrow(EntityNotFoundException::new);

        return modelMapper.map(existingEntitye, RegistroFinanceiroDTO.class);
    }

    public RegistroFinanceiroDTO atualizarRegistroFinanceiroPorId(RegistroFinanceiroDTO registroFinanceiroDto) {
        ERegistroFinanceiro existingEntitye = this.registroFinanceiroInterface
                .buscarRegistroFinanceiroPorId(registroFinanceiroDto.getId())
                .orElseThrow(EntityNotFoundException::new);

        this.modelMapper.getConfiguration().setSkipNullEnabled(true);

        this.modelMapper.map(registroFinanceiroDto, existingEntitye);

        try {
            this.registroFinanceiroInterface.save(existingEntitye);
            return modelMapper.map(existingEntitye, RegistroFinanceiroDTO.class);
        }catch (Exception e){
            log.error("Erro ao atualizar registro financeiro com Id: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean excluirRegistroFinanceiroPorId(Long registroFinanceiroId) {

        ERegistroFinanceiro existingEntitye = this.registroFinanceiroInterface
                .buscarRegistroFinanceiroPorId(registroFinanceiroId)
                .orElseThrow(EntityNotFoundException::new);

        existingEntitye.setStatus(EnStatus.I);

        try {
            this.registroFinanceiroInterface.save(existingEntitye);
            return true;
        }catch (Exception e){
            log.error("Erro ao excluir registro financeiro com Id: {}", e.getMessage(), e);
            return false;
        }
    }
}
