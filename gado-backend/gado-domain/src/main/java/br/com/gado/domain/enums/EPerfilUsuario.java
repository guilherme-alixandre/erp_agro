package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum EPerfilUsuario {
    // adicionar conforme for lembrando e precisando
    GERENTE("Gerente"),
    CASEIRO("Caseiro"),
    ADMINISTRADOR("Administrador");

    private final String perfil;

    EPerfilUsuario(String perfil){
        this.perfil = perfil;
    }
}
