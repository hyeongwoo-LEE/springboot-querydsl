package study.querydslstudy.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydslstudy.dto.MemberSearchCondition;
import study.querydslstudy.dto.MemberTeamDTO;
import study.querydslstudy.entity.Member;
import study.querydslstudy.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired EntityManager em;
    @Autowired MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() throws Exception{
        Member member = Member.builder()
                .username("member1")
                .age(10)
                .build();

        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();

        Assertions.assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll();
        Assertions.assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        Assertions.assertThat(result2).containsExactly(member);
    }
    
    @Test
    void basicQuerydslTest() throws Exception{
        Member member = Member.builder()
                .username("member1")
                .age(10)
                .build();

        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();

        Assertions.assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll_Querydsl();
        Assertions.assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUsername_Querydsl("member1");
        Assertions.assertThat(result2).containsExactly(member);
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

        List<MemberTeamDTO> result = memberJpaRepository.searchByBuilder(condition);

        Assertions.assertThat(result).extracting("username").containsExactly("member4");


    }
}