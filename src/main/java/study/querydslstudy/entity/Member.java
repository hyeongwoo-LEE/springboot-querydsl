package study.querydslstudy.entity;


import lombok.*;

import javax.persistence.*;

@ToString(exclude = {"team"})
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private String username;

    private int age;

    public void changeTeam(Team team){
        this.team = team;
        team.getMembers().add(this);
    }

}
