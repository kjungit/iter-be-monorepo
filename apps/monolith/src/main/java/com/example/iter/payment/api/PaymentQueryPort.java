package com.example.iter.payment.api;

// payment 가 다른 도메인에게 공개하는 결제 조회 창구.
// 규약은 auth/api/UserQueryPort 의 주석을 따른다.
public interface PaymentQueryPort {

    // 전체 결제 건수. 상태로 거르지 않는다 (취소·환불 포함) —
    // 기존 PaymentRepository.count() 와 동작이 같아야 한다.
    long count();
}
