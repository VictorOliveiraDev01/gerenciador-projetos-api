package com.api.gerenciadorprojetos.Projects.Mappers;

import com.api.gerenciadorprojetos.Projects.DTO.ProjectDTO;
import com.api.gerenciadorprojetos.Projects.Entities.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Mapper para converter entidades de Projeto em DTOs e vice-versa.
 *
 * @author victor.marcelo
 */
@Mapper
public interface ProjectMapper {

    ProjectMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(ProjectMapper.class);


    @Mapping(source = "id", target = "id")
    @Mapping(source = "nomeProjeto", target = "nomeProjeto")
    @Mapping(source = "descricao", target = "descricao")
    @Mapping(source = "dataInicio", target = "dataInicio")
    @Mapping(source = "dataTerminoPrevista", target = "dataTerminoPrevista")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "gerenteProjeto", target = "gerenteProjeto")
    @Mapping(source = "orcamento", target = "orcamento")
    @Mapping(source = "prioridade", target = "prioridade")
    @Mapping(source = "membrosProjeto", target = "membrosProjeto")
    ProjectDTO toDto(Project project);

    /*
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "nomeProjeto", target = "nomeProjeto"),
            @Mapping(source = "descricao", target = "descricao"),
            @Mapping(source = "dataInicio", target = "dataInicio"),
            @Mapping(source = "dataTerminoPrevista", target = "dataTerminoPrevista"),
            @Mapping(source = "status", target = "status"),
            @Mapping(source = "gerenteProjeto", target = "gerenteProjeto"),
            @Mapping(source = "orcamento", target = "orcamento"),
            @Mapping(source = "prioridade", target = "prioridade"),
            @Mapping(source = "membrosProjeto", target = "membrosProjeto")
    })
    Project toEntity(ProjectDTO projectDTO);

     */
}

