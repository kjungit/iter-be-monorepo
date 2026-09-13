package com.example.iter.dispute.service;

import com.example.iter.common.security.UserStatus;
import com.example.iter.auth.api.UserQueryPort;
import com.example.iter.common.exception.CustomException;
import com.example.iter.common.exception.ErrorCode;
import com.example.iter.device.domain.entity.Equipment;
import com.example.iter.device.domain.entity.EquipmentCategory;
import com.example.iter.device.domain.entity.EquipmentStatus;
import com.example.iter.device.api.EquipmentInfo;
import com.example.iter.device.api.EquipmentQueryPort;
import com.example.iter.dispute.domain.entity.ReportTargetType;
import com.example.iter.reservation.domain.entity.Rental;
import com.example.iter.reservation.api.RentalInfo;
import com.example.iter.reservation.api.RentalStatus;
import com.example.iter.reservation.api.RentalQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportTargetValidatorTest {

    private static final Long REPORTER_ID = 1L;

    @Mock
    private UserQueryPort userQueryPort;

    @Mock
    private EquipmentQueryPort equipmentQueryPort;

    @Mock
    private RentalQueryPort rentalQueryPort;

    @InjectMocks
    private ReportTargetValidator reportTargetValidator;

    @Test
    void 자기_자신은_회원_신고할_수_없다() {
        assertThatThrownBy(() -> reportTargetValidator.validate(
                ReportTargetType.USER,
                REPORTER_ID,
                REPORTER_ID
        ))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REPORT_SELF_TARGET_NOT_ALLOWED);

        verifyNoInteractions(userQueryPort);
    }

    @Test
    void 존재하지_않거나_탈퇴한_회원은_신고할_수_없다() {
        when(userQueryPort.isReportable(2L)).thenReturn(false);

        assertThatThrownBy(() -> reportTargetValidator.validate(
                ReportTargetType.USER,
                2L,
                REPORTER_ID
        ))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        when(userQueryPort.isReportable(3L)).thenReturn(false);

        assertThatThrownBy(() -> reportTargetValidator.validate(
                ReportTargetType.USER,
                3L,
                REPORTER_ID
        ))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 정지된_회원은_신고_대상으로_선택할_수_있다() {
        when(userQueryPort.isReportable(2L)).thenReturn(true);

        assertThatCode(() -> reportTargetValidator.validate(
                ReportTargetType.USER,
                2L,
                REPORTER_ID
        )).doesNotThrowAnyException();
    }

    @Test
    void 본인_소유_장비는_신고할_수_없다() {
        when(equipmentQueryPort.find(10L))
                .thenReturn(Optional.of(equipment(10L, REPORTER_ID, EquipmentStatus.ACTIVE)));

        assertThatThrownBy(() -> reportTargetValidator.validate(
                ReportTargetType.EQUIPMENT,
                10L,
                REPORTER_ID
        ))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REPORT_SELF_TARGET_NOT_ALLOWED);
    }

    @Test
    void 삭제된_장비는_신고할_수_없다() {
        when(equipmentQueryPort.find(10L))
                .thenReturn(Optional.of(equipment(10L, 2L, EquipmentStatus.DELETED)));

        assertThatThrownBy(() -> reportTargetValidator.validate(
                ReportTargetType.EQUIPMENT,
                10L,
                REPORTER_ID
        ))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EQUIPMENT_NOT_FOUND);
    }

    @Test
    void 타인_소유의_점검중인_장비도_신고할_수_있다() {
        when(equipmentQueryPort.find(10L))
                .thenReturn(Optional.of(equipment(10L, 2L, EquipmentStatus.MAINTENANCE)));

        assertThatCode(() -> reportTargetValidator.validate(
                ReportTargetType.EQUIPMENT,
                10L,
                REPORTER_ID
        )).doesNotThrowAnyException();
    }

    @Test
    void 거래의_대여자와_장비_등록자는_거래를_신고할_수_있다() {
        RentalInfo renterRental = rental(100L, 10L, REPORTER_ID);
        EquipmentInfo ownerEquipment = equipment(10L, 2L, EquipmentStatus.DELETED);
        when(rentalQueryPort.find(100L)).thenReturn(Optional.of(renterRental));
        when(equipmentQueryPort.find(10L)).thenReturn(Optional.of(ownerEquipment));

        assertThatCode(() -> reportTargetValidator.validate(
                ReportTargetType.RENTAL,
                100L,
                REPORTER_ID
        )).doesNotThrowAnyException();

        RentalInfo ownerRental = rental(101L, 11L, 3L);
        EquipmentInfo reporterEquipment = equipment(11L, REPORTER_ID, EquipmentStatus.ACTIVE);
        when(rentalQueryPort.find(101L)).thenReturn(Optional.of(ownerRental));
        when(equipmentQueryPort.find(11L)).thenReturn(Optional.of(reporterEquipment));

        assertThatCode(() -> reportTargetValidator.validate(
                ReportTargetType.RENTAL,
                101L,
                REPORTER_ID
        )).doesNotThrowAnyException();
    }

    @Test
    void 거래_제3자는_거래를_신고할_수_없다() {
        RentalInfo rental = rental(100L, 10L, 2L);
        EquipmentInfo equipment = equipment(10L, 3L, EquipmentStatus.ACTIVE);
        when(rentalQueryPort.find(100L)).thenReturn(Optional.of(rental));
        when(equipmentQueryPort.find(10L)).thenReturn(Optional.of(equipment));

        assertThatThrownBy(() -> reportTargetValidator.validate(
                ReportTargetType.RENTAL,
                100L,
                REPORTER_ID
        ))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RENTAL_NOT_PARTY);
    }

    @Test
    void 존재하지_않는_거래는_신고할_수_없다() {
        when(rentalQueryPort.find(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportTargetValidator.validate(
                ReportTargetType.RENTAL,
                100L,
                REPORTER_ID
        ))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RENTAL_NOT_FOUND);

        verify(equipmentQueryPort, never()).find(10L);
    }


    private EquipmentInfo equipment(Long id, Long ownerId, EquipmentStatus status) {
        return new EquipmentInfo(
                id,
                ownerId,
                "테스트 장비",
                EquipmentCategory.CAMERA.name(),
                java.math.BigDecimal.valueOf(30000),
                status == EquipmentStatus.ACTIVE,
                status == EquipmentStatus.DELETED
        );
    }

    private RentalInfo rental(Long id, Long equipmentId, Long renterId) {
        return new RentalInfo(id, equipmentId, renterId, "테스트 장비", null,
                RentalStatus.RENTING, java.math.BigDecimal.valueOf(150000));
    }
}
