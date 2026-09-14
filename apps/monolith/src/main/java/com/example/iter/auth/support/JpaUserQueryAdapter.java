package com.example.iter.auth.support;

import com.example.iter.auth.api.UserQueryPort;
import com.example.iter.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// auth/api/UserQueryPort 의 모놀리스 구현. 같은 프로세스이므로 리포지토리를 직접 호출한다.
// 서비스가 분리되면 이 클래스만 RestUserQueryAdapter 로 교체한다.
//
// readOnly = true 는 JpaAuthUserLoader 와 같은 이유다 — 호출자 트랜잭션이 있으면 참여하고,
// 없으면 자기 읽기 트랜잭션을 연다. 조회 어댑터는 어떤 엔티티도 변경하지 않는다.
//
// !! 조회 조건을 추가하지 말 것 !!
// 포트 주석에 적힌 대로 동작해야 한다. 여기서 조용히 필터를 넣으면
// 호출부는 숫자가 왜 달라졌는지 알 방법이 없다.
@Component
@RequiredArgsConstructor
public class JpaUserQueryAdapter implements UserQueryPort {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }
}
