package com.example.iter.dispute.support;

import com.example.iter.dispute.api.ReportQueryPort;
import com.example.iter.dispute.domain.entity.ReportStatus;
import com.example.iter.dispute.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// dispute/api/ReportQueryPort 의 모놀리스 구현.
// 규약은 auth/support/JpaUserQueryAdapter 의 주석을 따른다.
//
// "미처리 = RECEIVED" 라는 판단이 여기 있다. 이전에는 이 지식이 관리자 대시보드
// (admin/stats/AdminStatsService) 코드에 박혀 있었다.
// 상태가 늘어나면 이 클래스만 고치면 된다.
@Component
@RequiredArgsConstructor
public class JpaReportQueryAdapter implements ReportQueryPort {

    private final ReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public long countReceived() {
        return reportRepository.countByStatus(ReportStatus.RECEIVED);
    }
}
