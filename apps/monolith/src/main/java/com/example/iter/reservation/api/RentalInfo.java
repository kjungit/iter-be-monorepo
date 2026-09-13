package com.example.iter.reservation.api;

import java.util.Objects;

// 다른 도메인이 대여 건을 가리킬 때 쓰는 최소 정보.
//
// 상품명은 Rental 이 대여 시점에 찍어둔 스냅샷(productNameSnapshot)이다.
// 장비가 나중에 이름을 바꾸거나 삭제돼도 알림·영수증에는 당시 이름이 남아야 하므로,
// 여기서 device 에 다시 물어보면 안 된다.
//
// 빌더를 두지 않는 이유는 auth/api/UserProfile 주석 참고.
public record RentalInfo(
        Long rentalId,
        Long equipmentId,
        Long renterId,
        String productName,
        String rejectReason
) {
    public RentalInfo {
        Objects.requireNonNull(rentalId, "rentalId");
        Objects.requireNonNull(equipmentId, "equipmentId");
        Objects.requireNonNull(renterId, "renterId");
        // rejectReason 은 거절된 대여에만 있다 — 계약상 null 허용.
    }

    public boolean isRenter(Long userId) {
        return renterId.equals(userId);
    }
}
