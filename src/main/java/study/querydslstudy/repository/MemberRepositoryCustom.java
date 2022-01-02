package study.querydslstudy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydslstudy.dto.MemberSearchCondition;
import study.querydslstudy.dto.MemberTeamDTO;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDTO> search(MemberSearchCondition condition);
    Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable);

}
