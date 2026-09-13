package com.example.iter.payment.support;

import com.example.iter.payment.api.PaymentQueryPort;
import com.example.iter.payment.domain.entity.Payment;
import com.example.iter.payment.domain.entity.PaymentStatus;
import com.example.iter.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// payment/api/PaymentQueryPort 의 모놀리스 구현.
// 규약은 auth/support/JpaUserQueryAdapter 의 주석을 따른다.
//
// "환불됨 = REFUNDED" 라는 판단이 여기 있다. 이전에는 알림 리스너가
// PaymentStatus 를 직접 꺼내 비교했다.
@Component
@RequiredArgsConstructor
public class JpaPaymentQueryAdapter implements PaymentQueryPort {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return paymentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRefundedForRental(Long rentalId) {
        return paymentRepository.findByRentalId(rentalId)
                .map(Payment::getStatus)
                .map(status -> status == PaymentStatus.REFUNDED)
                .orElse(false);
    }
}
