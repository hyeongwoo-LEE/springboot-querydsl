package study.querydslstudy.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberTeamDTO {
    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;

    //Q 파일 생성
    @QueryProjection
    public MemberTeamDTO(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
