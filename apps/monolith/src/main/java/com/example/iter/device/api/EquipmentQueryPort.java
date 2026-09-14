package com.example.iter.device.api;

import java.util.Optional;

// device 가 다른 도메인에게 공개하는 장비 조회 창구.
// 규약은 auth/api/UserQueryPort 의 주석을 따른다.
public interface EquipmentQueryPort {

    // 전체 장비 수. 삭제된 장비를 제외하지 않는다 —
    // 기존 EquipmentRepository.count() 와 동작이 같아야 한다.
    long count();

    // 장비 소유자의 회원 ID. 장비가 없으면 빈 값.
    //
    // 장비 전체를 돌려주지 않는 이유는, 현재 이걸 쓰는 호출부(알림 리스너)가
    // 소유자에게 알림을 보내려고 ID 하나만 필요로 하기 때문이다.
    // 장비의 다른 속성이 필요한 소비자는 별도 메서드를 쓴다.
    Optional<Long> findOwnerId(Long equipmentId);
}
