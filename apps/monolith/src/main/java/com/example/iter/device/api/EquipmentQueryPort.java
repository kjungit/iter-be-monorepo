package com.example.iter.device.api;

// device 가 다른 도메인에게 공개하는 장비 조회 창구.
// 규약은 auth/api/UserQueryPort 의 주석을 따른다.
public interface EquipmentQueryPort {

    // 전체 장비 수. 삭제된 장비를 제외하지 않는다 —
    // 기존 EquipmentRepository.count() 와 동작이 같아야 한다.
    long count();
}
