package com.api.gerenciadorprojetos.Tasks.Mappers;

import com.api.gerenciadorprojetos.Tasks.DTO.TaskDTO;
import com.api.gerenciadorprojetos.Tasks.Entities.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Mapper para converter entidades de Tarafeas de Projeto em DTOs e vice-versa.
 *
 * @author victor.marcelo
 */
@Mapper
public interface TaskMapper {

    TaskMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(TaskMapper.class);

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "nomeTarefa", target = "nomeTarefa"),
            @Mapping(source = "descricao", target = "descricao"),
            @Mapping(source = "dataInicio", target = "dataInicio"),
            @Mapping(source = "dataTerminoPrevista", target = "dataTerminoPrevista"),
            @Mapping(source = "status", target = "status"),
            @Mapping(source = "responsaveis", target = "responsaveis"),
            @Mapping(source = "projeto", target = "projeto"),
            @Mapping(source = "dataConclusao", target = "dataConclusao"),
            @Mapping(source = "porcentagemConcluida", target = "porcentagemConcluida")
    })
    TaskDTO toDto(Task task);

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "nomeTarefa", target = "nomeTarefa"),
            @Mapping(source = "descricao", target = "descricao"),
            @Mapping(source = "dataInicio", target = "dataInicio"),
            @Mapping(source = "dataTerminoPrevista", target = "dataTerminoPrevista"),
            @Mapping(source = "status", target = "status"),
            @Mapping(source = "responsaveis", target = "responsaveis"),
            @Mapping(source = "projeto", target = "projeto"),
            @Mapping(source = "dataConclusao", target = "dataConclusao"),
            @Mapping(source = "porcentagemConcluida", target = "porcentagemConcluida")
    })
    Task toEntity(TaskDTO taskDTO);;
}

