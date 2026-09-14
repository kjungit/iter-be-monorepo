package com.example.iter.payment.support;

import com.example.iter.payment.domain.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaPaymentQueryAdapterTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private JpaPaymentQueryAdapter adapter;

    // 포트 주석의 "상태로 거르지 않는다 (취소·환불 포함)"를 지키는 테스트.
    @Test
    void 전체_결제_건수를_거르지_않고_그대로_돌려준다() {
        when(paymentRepository.count()).thenReturn(300L);

        assertThat(adapter.count()).isEqualTo(300L);

        verify(paymentRepository).count();
        verifyNoMoreInteractions(paymentRepository);
    }
}
