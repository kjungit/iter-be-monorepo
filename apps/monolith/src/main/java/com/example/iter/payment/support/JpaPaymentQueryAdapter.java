package com.example.iter.payment.support;

import com.example.iter.payment.api.PaymentQueryPort;
import com.example.iter.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// payment/api/PaymentQueryPort 의 모놀리스 구현.
// 규약은 auth/support/JpaUserQueryAdapter 의 주석을 따른다.
@Component
@RequiredArgsConstructor
public class JpaPaymentQueryAdapter implements PaymentQueryPort {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return paymentRepository.count();
    }
}
