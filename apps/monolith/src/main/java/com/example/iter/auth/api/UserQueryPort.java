package com.example.iter.auth.api;

// auth 가 다른 도메인에게 공개하는 회원 조회 창구.
//
// 다른 도메인은 UserRepository 나 User 엔티티를 직접 참조하는 대신 이 포트를 쓴다.
// 구현은 auth/support/JpaUserQueryAdapter 이며, 모놀리스에서는 같은 프로세스 안의
// 직접 호출이지만 나중에 서비스가 분리되면 어댑터만 REST 구현으로 교체한다.
// 호출부 코드는 그대로 둔다.
//
// !! 엔티티를 반환하지 않는다 !!
// User 를 넘기면 JPA 영속성 컨텍스트가 모듈 경계를 넘고, REST 로 바꿀 때 호출부를 전부 고쳐야 한다.
//
// !! 메서드는 "무엇을 조회하는가"가 아니라 "무엇을 묻는가"로 짓는다 !!
// 예: countByStatusIn(Collection<UserStatus>) 대신 의도를 담은 이름.
// 상태 집합을 인자로 받으면 열거형이 그대로 다른 도메인에 새어나가고,
// 판단 기준이 바뀔 때마다 호출부도 같이 고쳐야 한다.
public interface UserQueryPort {

    // 전체 회원 수. 탈퇴 회원을 제외하지 않는다 —
    // 필터를 넣으려면 별도 메서드를 만들 것. 기존 UserRepository.count() 와 동작이 같아야 한다.
    long count();
}
