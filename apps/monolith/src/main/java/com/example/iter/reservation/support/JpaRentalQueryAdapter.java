package com.example.iter.reservation.support;

import com.example.iter.reservation.api.RentalInfo;
import com.example.iter.reservation.api.RentalQueryPort;
import com.example.iter.reservation.domain.entity.Rental;
import com.example.iter.reservation.domain.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// reservation/api/RentalQueryPort 의 모놀리스 구현.
// 규약은 auth/support/JpaUserQueryAdapter 의 주석을 따른다.
@Component
@RequiredArgsConstructor
public class JpaRentalQueryAdapter implements RentalQueryPort {

    private final RentalRepository rentalRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<RentalInfo> find(Long rentalId) {
        return rentalRepository.findById(rentalId).map(JpaRentalQueryAdapter::toInfo);
    }

    private static RentalInfo toInfo(Rental rental) {
        return new RentalInfo(
                rental.getId(),
                rental.getEquipmentId(),
                rental.getRenterId(),
                rental.getProductNameSnapshot(),
                rental.getRejectReason()
        );
    }
}
