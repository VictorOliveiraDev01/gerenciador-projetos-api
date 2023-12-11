package com.api.gerenciadorprojetos.audit.Entities;

import com.api.gerenciadorprojetos.Users.Entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private LocalDateTime horarioRegistro;

    @Column(name = "id_usuario")
    private User usuario;

    @Column(name = "acao")
    private String acaoRealizada;

    @Column(name = "entidade_afetada")
    private String entidadeAfetada;


    @Column(name = "endereco_ip")
    private String enderecoIP;

    @Column(name = "agente_usuario")
    private String agenteUsuario;

    @Column(name = "origem")
    private String origemAcao;

    @Column(name = "informacoes_sessao")
    private String informacoesSessao;
}
