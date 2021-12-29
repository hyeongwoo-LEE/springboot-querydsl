package study.querydslstudy;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydslstudy.dto.MemberDTO;
import study.querydslstudy.dto.QMemberDTO;
import study.querydslstudy.dto.UserDTO;
import study.querydslstudy.entity.Member;
import study.querydslstudy.entity.QMember;
import study.querydslstudy.entity.QTeam;
import study.querydslstudy.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

@Transactional
@SpringBootTest
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void before() throws Exception {

        queryFactory = new JPAQueryFactory(em);

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
    }


    @Test
    void startQuerydsl3() throws Exception {

        QMember member = new QMember("m");

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void search() throws Exception {

        QMember member = new QMember("m");

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void searchAndParam() throws Exception {
        QMember member = new QMember("m");

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"), member.age.eq(10))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void resultFetch() throws Exception {
        QMember member = new QMember("m");

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst(); //.limit(1).fetchOne();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순
     * 2. 회원 이름 오름차순
     * 단 2에서 회원 이름이 없으면 마지막에 출력 (nulls last)
     */
    @Test
    void sort() throws Exception {
        em.persist(Member.builder()
                .username(null)
                .age(100).build());

        em.persist(Member.builder()
                .username("member5")
                .age(100).build());

        em.persist(Member.builder()
                .username("member6")
                .age(100).build());

        QMember member = new QMember("m");

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        Assertions.assertThat(member5.getUsername()).isEqualTo("member5");
        Assertions.assertThat(member6.getUsername()).isEqualTo("member6");
        Assertions.assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    void paging1() throws Exception {

        QMember member = new QMember("m");

        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        Assertions.assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void paging2() throws Exception {
        QMember member = new QMember("m");

        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        Assertions.assertThat(queryResults.getTotal()).isEqualTo(4);
        Assertions.assertThat(queryResults.getLimit()).isEqualTo(2);
        Assertions.assertThat(queryResults.getOffset()).isEqualTo(1);
        Assertions.assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    void aggregation() throws Exception {
        QMember member = new QMember("m");
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.avg(),
                        member.age.sum(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        Assertions.assertThat(tuple.get(member.count())).isEqualTo(4);
        Assertions.assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        Assertions.assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        Assertions.assertThat(tuple.get(member.age.max())).isEqualTo(40);
        Assertions.assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라
     */
    @Test
    void group() throws Exception {
        QMember member = new QMember("m");
        QTeam team = new QTeam("t");

        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        Assertions.assertThat(teamA.get(team.name)).isEqualTo("teamA");
        Assertions.assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        Assertions.assertThat(teamB.get(team.name)).isEqualTo("teamB");
        Assertions.assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * 팀 A에 소속된 모든 회원을 찾아라.
     */
    @Test
    void join() throws Exception {
        QMember member = new QMember("m");
        QTeam team = new QTeam("t");

        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        Assertions.assertThat(result).extracting("username").containsExactly("member1", "member2");
    }

    /**
     * 세타조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    void theta_join() throws Exception {

        QMember member = new QMember("m");
        QTeam team = new QTeam("t");

        em.persist(Member.builder()
                .username("teamA").build());
        em.persist(Member.builder()
                .username("teamB").build());
        em.persist(Member.builder()
                .username("teamC").build());

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        Assertions.assertThat(result).extracting("username").containsExactly("teamA", "teamB");
    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL : select m,t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    void join_on_filtering() throws Exception {
        QMember member = new QMember("m");
        QTeam team = new QTeam("t");

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple: " + tuple);
        }

    }

    /**
     * 연관관계 없는 엔티티 외부조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    void join_on_no_relation() throws Exception {

        QMember member = new QMember("m");
        QTeam team = new QTeam("t");

        em.persist(Member.builder()
                .username("teamA").build());
        em.persist(Member.builder()
                .username("teamB").build());
        em.persist(Member.builder()
                .username("teamC").build());

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple: " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void fetchJoinNo() throws Exception {
        QMember member = new QMember("m");

        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        //isLoaded - 초기화된 엔티티인지 가르쳐 주는 함수
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        Assertions.assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    void fetchJoinUse() throws Exception {
        QMember member = new QMember("m");
        QTeam team = new QTeam("t");

        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        //isLoaded - 초기화된 엔티티인지 가르쳐 주는 함수
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        Assertions.assertThat(loaded).as("페치 조인 미적용").isTrue();

    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    void subQuery() throws Exception {
        QMember member = new QMember("m");
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        Assertions.assertThat(result).extracting("age").containsExactly(40);

    }

    /**
     * 나이가 평균 이상인 회원 조회
     */
    @Test
    void subQueryGoe() throws Exception {
        QMember member = new QMember("m");
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        Assertions.assertThat(result).extracting("age").containsExactly(30, 40);
    }


    @Test
    void subQueryIn() throws Exception {
        QMember member = new QMember("m");
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        Assertions.assertThat(result).extracting("age").containsExactly(20, 30, 40);
    }

    @Test
    void selectSubQuery() throws Exception {
        QMember member = new QMember("m");
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple: " + tuple);
        }
    }

    @Test
    void simpleProjection() throws Exception {
        QMember member = new QMember("m");

        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void tupleProjection() throws Exception {
        QMember member = new QMember("m");

        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }

    @Test
    void findDTOByJPQL() throws Exception {
        List<MemberDTO> result = em.createQuery("select new study.querydslstudy.dto.MemberDTO(m.username,m.age) from Member m", MemberDTO.class)
                .getResultList();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }

    @Test
    void findDTOBySetter() throws Exception {
        QMember member = new QMember("m");
        List<MemberDTO> result = queryFactory
                .select(Projections.bean(MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }


    @Test
    void findDTOByField() throws Exception {
        QMember member = new QMember("m");
        List<MemberDTO> result = queryFactory
                .select(Projections.fields(MemberDTO.class, //getter,setter 없어도 가능
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }

    @Test
    void findDTOByConstructor() throws Exception {
        QMember member = new QMember("m");
        List<MemberDTO> result = queryFactory
                .select(Projections.constructor(MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }

    @Test
    void findDTOByUserDTO() throws Exception {
        QMember member = new QMember("m");
        List<UserDTO> result = queryFactory
                .select(Projections.bean(UserDTO.class,
                        member.username.as("name"), //DTO의 변수명과 같아야함 - 다를시 as 처리
                        member.age))
                .from(member)
                .fetch();

        for (UserDTO userDTO : result) {
            System.out.println("userDTO = " + userDTO);
        }
    }

    @Test
    void findUserDTO() throws Exception {
        QMember member = new QMember("m");
        QMember memberSub = new QMember("sub");

        List<UserDTO> result = queryFactory
                .select(Projections.fields(UserDTO.class,
                        member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();

        for (UserDTO userDTO : result) {
            System.out.println("userDTO = " + userDTO);
        }
    }

    //생성자 파라미터 외 주입시 런타임오류
    @Test
    void findUserDTOByConstructor() throws Exception {
        QMember member = new QMember("m");
        List<UserDTO> result = queryFactory
                .select(Projections.constructor(UserDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (UserDTO userDTO : result) {
            System.out.println("UserDTO = " + userDTO);
        }
    }

    //생성자 파라미터 외 주입시 컴파일 오류
    @Test
    void findDTOByQueryProjection() throws Exception {
        QMember member = new QMember("m");

        List<MemberDTO> result = queryFactory
                .select(new QMemberDTO(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }

    @Test
    void dynamicQuery_BooleanBuilder() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        QMember member = new QMember("m");

        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }

        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    void dynamicQuery_WhereParam() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {

        QMember member = new QMember("m");
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))  //null 이면 그냥 무시.
                //.where(allEq(usernameCond,ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        QMember member = new QMember("m");
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        QMember member = new QMember("m");
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    //조합도 가능 - 재사용성 증가가
    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    /**
     * 벌크연산 - update 문은 영속성 컨텍스트를 거치지 않고 바로 DB에 전송 -> 변경된 내용이 영속성 컨텍스트에 반영x
     */
    @Test
    void bulkUpdate() throws Exception {
        QMember member = new QMember("m");

        //member1 = 10 -> 비회원
        //member2 = 20 -> 비회원
        //member3 = 30 -> 유지
        //member4 = 40 -> 유지

        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        em.flush(); //?
        em.clear();
    }

    @Test
    void bulkAdd() throws Exception {
        QMember member = new QMember("m");

        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();

        em.flush();
        em.clear();
    }

    @Test
    void bulkDelete() throws Exception {
        QMember member = new QMember("m");

        long count = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();

        em.flush();
        em.clear();
    }

    @Test
    void sqlFunction() throws Exception {
        QMember member = new QMember("m");
        List<String> result = queryFactory
                .select(
                        Expressions.stringTemplate("function('replace', {0},{1},{2})",
                                member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void sqlFunction2() throws Exception {
        QMember member = new QMember("m");
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
//                .where(member.username.eq(
////                        Expressions.stringTemplate("function('lower', {0})", member.username)))
                .where(member.username.eq(member.username.lower()))
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }

    }
}










