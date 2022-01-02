package study.querydslstudy.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import study.querydslstudy.dto.MemberSearchCondition;
import study.querydslstudy.dto.MemberTeamDTO;
import study.querydslstudy.entity.Member;
import study.querydslstudy.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired EntityManager em;
    @Autowired MemberRepository memberRepepository;

    @Test
    void basicTest() throws Exception{
        Member member = Member.builder()
                .username("member1")
                .age(10)
                .build();

        memberRepepository.save(member);

        Member findMember = memberRepepository.findById(member.getId()).get();

        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    void searchTest() throws Exception{
        Team teamA = Team.builder()
                .name("teamA").build();

        Team teamB = Team.builder()
                .name("teamB").build();
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = Member.builder()
                .username("member1")
                .age(10)
                .team(teamA)
                .build();

        Member member2 = Member.builder()
                .username("member2")
                .age(20)
                .team(teamA)
                .build();

        Member member3 = Member.builder()
                .username("member3")
                .age(30)
                .team(teamB)
                .build();

        Member member4 = Member.builder()
                .username("member4")
                .age(40)
                .team(teamB)
                .build();

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDTO> result = memberRepepository.search(condition);

        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    void searchPageSimple() throws Exception{
        Team teamA = Team.builder()
                .name("teamA").build();

        Team teamB = Team.builder()
                .name("teamB").build();
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = Member.builder()
                .username("member1")
                .age(10)
                .team(teamA)
                .build();

        Member member2 = Member.builder()
                .username("member2")
                .age(20)
                .team(teamA)
                .build();

        Member member3 = Member.builder()
                .username("member3")
                .age(30)
                .team(teamB)
                .build();

        Member member4 = Member.builder()
                .username("member4")
                .age(40)
                .team(teamB)
                .build();

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDTO> result = memberRepepository.searchPageSimple(condition, pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1","member2","member3");
    }

}