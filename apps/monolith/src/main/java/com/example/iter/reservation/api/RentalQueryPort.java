package com.example.iter.reservation.api;

import java.util.Optional;

// reservation 이 다른 도메인에게 공개하는 대여 조회 창구.
// 규약은 auth/api/UserQueryPort 의 주석을 따른다.
public interface RentalQueryPort {

    // 대여 한 건. 상태로 거르지 않는다 (취소·완료 건도 그대로 돌려준다).
    Optional<RentalInfo> find(Long rentalId);
}
