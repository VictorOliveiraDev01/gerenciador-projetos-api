package com.api.gerenciadorprojetos.Tasks.Enums;

public enum StatusTarefa {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDA("Concluída"),
    ATRASADA("Atrasada");
    // Adicione outros estados conforme necessário

    private final String descricao;

    StatusTarefa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
    }

