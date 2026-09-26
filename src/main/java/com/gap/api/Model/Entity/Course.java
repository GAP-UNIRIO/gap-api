package com.gap.api.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="courses")
public class Course {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name="name", nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id", referencedColumnName = "id")
    private User director;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id", referencedColumnName = "id")
    private User coordinator;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "secretary_id", referencedColumnName = "id")
    private User secretary;

     /**
         Como course e user apontam uma para a outra de duas formas diferentes,
         corre um grande risco de gerar um loop infinito na hora de retornar
         isso numa API. O Spring vai tentar converter o Curso, que tem um Coordenador,
         que pertence a um Curso, que tem um Coordenador... até a memória acabar.
         Ja devemos usar dto para retorno de qualquer jeito, mas nesse caso aqui
         principalmente para evitar problemas.
     **/

    @OneToMany(mappedBy = "course")
    private List<User> users = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reitoria_id")
    private OrgUnit reitoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordenacao_id")
    private OrgUnit coordenacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direcao_id")
    private OrgUnit direcao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secretaria_id")
    private OrgUnit secretaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escola_id")
    private OrgUnit escola;

}
