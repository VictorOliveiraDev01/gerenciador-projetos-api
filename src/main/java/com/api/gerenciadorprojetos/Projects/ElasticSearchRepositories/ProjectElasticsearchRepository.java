package com.api.gerenciadorprojetos.Projects.ElasticSearchRepositories;

import com.api.gerenciadorprojetos.Projects.Entities.Project;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório Elasticsearch para gerenciamento de entidades Projeto.
 * Realiza operações específicas do Elasticsearch para busca e recuperação de projetos.
 *
 * @author victor.marcelo
 */

@Repository
public interface ProjectElasticsearchRepository extends ElasticsearchRepository<Project, Long> {

    /**
     * Busca projetos pelo termo que contenha o texto especificado.
     *
     * @param termo O texto a ser buscado em diferentes campos dos projetos.
     * @return Uma lista de projetos que correspondem aos critérios de busca.
     */
    List<Project> findProjectsByTermoContaining(String termo);

    /**
     * Busca projetos onde o usuário é um membro e o termo contém o texto especificado.
     *
     * @param userId O ID do usuário para o qual a busca é realizada.
     * @param termo O texto a ser buscado em diferentes campos dos projetos.
     * @return Uma lista de projetos que correspondem aos critérios de busca.
     */
    List<Project> findByUserIdAndTermoContaining(Long userId, String termo);

}
