package com.example.iter.device.support;

import com.example.iter.device.api.EquipmentQueryPort;
import com.example.iter.device.domain.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// device/api/EquipmentQueryPort 의 모놀리스 구현.
// 규약은 auth/support/JpaUserQueryAdapter 의 주석을 따른다.
@Component
@RequiredArgsConstructor
public class JpaEquipmentQueryAdapter implements EquipmentQueryPort {

    private final EquipmentRepository equipmentRepository;

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return equipmentRepository.count();
    }
}
