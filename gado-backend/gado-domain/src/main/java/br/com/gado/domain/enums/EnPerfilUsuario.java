package br.com.gado.domain.enums;

import lombok.Getter;

@Getter
public enum EnPerfilUsuario {
    // adicionar conforme for lembrando e precisando
    GERENTE("Gerente"),
    CASEIRO("Caseiro"),
    ADMINISTRADOR("Administrador");

    private final String perfil;

    EnPerfilUsuario(String perfil){
        this.perfil = perfil;
    }
}
