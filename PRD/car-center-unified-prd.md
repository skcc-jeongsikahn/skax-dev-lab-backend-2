# 자동차 정비 예약 시스템 통합 PRD

## 📋 목차
1. [제품 개요](#1-제품-개요)
2. [기술 아키텍처](#2-기술-아키텍처)
3. [업무별 개발 가이드](#3-업무별-개발-가이드)
   - [Task 1: 사용자 인증 시스템](#task-1-사용자-인증-시스템)
   - [Task 2: 차량 관리 시스템](#task-2-차량-관리-시스템)
   - [Task 3: 정비소 관리 시스템](#task-3-정비소-관리-시스템)
   - [Task 4: 예약 시스템](#task-4-예약-시스템)
   - [Task 5: 견적 관리 시스템](#task-5-견적-관리-시스템)
   - [Task 6: 정비 프로세스 시스템](#task-6-정비-프로세스-시스템)
   - [Task 7: 결제 시스템](#task-7-결제-시스템)
   - [Task 8: 리뷰 평점 시스템](#task-8-리뷰-평점-시스템)
   - [Task 9: 알림 시스템](#task-9-알림-시스템)
   - [Task 10: 관리자 시스템](#task-10-관리자-시스템)
4. [공통 기술 스펙](#4-공통-기술-스펙)
5. [개발 계획](#5-개발-계획)

---

## 1. 제품 개요

### 1.1 제품 비전
고객과 정비소를 연결하는 디지털 플랫폼으로, 투명하고 편리한 자동차 정비 서비스를 제공합니다.

### 1.2 제품 목표
- **고객**: 편리한 정비 예약 및 진행상황 실시간 추적
- **정비소**: 효율적인 예약 관리 및 고객 소통
- **플랫폼**: 신뢰할 수 있는 정비 서비스 생태계 구축

### 1.3 핵심 성공 지표 (KPI)
- 월 활성 사용자 수 (MAU)
- 예약 전환율 (방문자 → 예약)
- 고객 재이용률
- 정비소 만족도
- 플랫폼 수수료 수익

---

## 2. 기술 아키텍처

### 2.1 기술 스택
참조: [001-project-structure.mdc]
- **Backend**: Spring Boot 3.4.1, Java 17
- **Database**: PostgreSQL (운영), H2 (로컬)
- **ORM**: MyBatis
- **Security**: Spring Security + JWT
- **Build**: Gradle 8.14.3

### 2.2 시스템 구조
```
car-center-api/
├── auth/              # 인증 관리 (Task 1)
├── user/              # 사용자 관리 (Task 1)
├── vehicle/           # 차량 정보 관리 (Task 2)
├── garage/            # 정비소 관리 (Task 3)
├── reservation/       # 예약 시스템 (Task 4)
├── quote/             # 견적 관리 (Task 5)
├── maintenance/       # 정비 프로세스 (Task 6)
├── payment/           # 결제 시스템 (Task 7)
├── review/            # 리뷰 및 평점 (Task 8)
├── notification/      # 알림 시스템 (Task 9)
└── admin/             # 관리자 시스템 (Task 10)
```

---

## 3. 업무별 개발 가이드

## Task 1: 사용자 인증 시스템

### 📋 Task 개요
- **목표**: 사용자 회원가입, 로그인, 권한 관리
- **우선순위**: 1 (필수 기반 기능)
- **예상 기간**: 1-2주
- **의존성**: 없음

### 🏗️ 기술 설계

#### 엔티티 설계
참조: [005-security-jwt.mdc]

```java
// 사용자 역할 정의
public enum UserRole {
    CUSTOMER,      // 일반 고객
    GARAGE_STAFF,  // 정비소 직원
    GARAGE_ADMIN,  // 정비소 관리자
    SYSTEM_ADMIN   // 시스템 관리자
}

@Entity
public class User {
    private Long id;
    private String email;
    private String password;      // BCrypt 암호화
    private String name;
    private String phone;
    private UserRole role;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Entity
public class UserProfile {
    private Long id;
    private Long userId;
    private String address;
    private Date birthDate;
    private String gender;
    private String profileImageUrl;
    private boolean notificationEmail;
    private boolean notificationSms;
    private boolean notificationPush;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 데이터베이스 설계
```sql
-- 사용자 테이블
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 프로필 테이블
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    address VARCHAR(255),
    birth_date DATE,
    gender VARCHAR(10),
    profile_image_url VARCHAR(500),
    notification_email BOOLEAN DEFAULT TRUE,
    notification_sms BOOLEAN DEFAULT TRUE,
    notification_push BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
```

#### API 설계
```
# 인증 API
POST   /api/auth/register        # 회원가입
POST   /api/auth/login          # 로그인
POST   /api/auth/refresh        # 토큰 갱신
POST   /api/auth/logout         # 로그아웃
POST   /api/auth/verify-phone   # 휴대폰 인증

# 사용자 API
GET    /api/users/profile       # 프로필 조회
PUT    /api/users/profile       # 프로필 수정
POST   /api/users/avatar        # 프로필 사진 업로드
PUT    /api/users/password      # 비밀번호 변경
```

#### 테스트 계획
참조: [008-testing-guidelines.mdc]
- **단위 테스트**: UserService, AuthService 로직 테스트
- **통합 테스트**: 회원가입/로그인 API 테스트
- **보안 테스트**: JWT 토큰 검증, 권한 체크

---

## Task 2: 차량 관리 시스템

### 📋 Task 개요
- **목표**: 사용자별 차량 등록, 관리, 이력 추적
- **우선순위**: 2
- **예상 기간**: 1주
- **의존성**: Task 1 (사용자 인증)

### 🏗️ 기술 설계

#### 엔티티 설계
참조: [004-database-mybatis.mdc]

```java
@Entity
public class Vehicle {
    private Long id;
    private Long ownerId;         // User ID
    private String licensePlate;  // 차량번호
    private String manufacturer;  // 제조사
    private String model;         // 모델
    private String year;          // 연식
    private EngineType engineType;
    private Integer currentMileage;
    private String photoUrl;
    private boolean isDefault;    // 기본 차량 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum EngineType {
    GASOLINE, DIESEL, HYBRID, ELECTRIC
}

@Entity
public class VehicleHistory {
    private Long id;
    private Long vehicleId;
    private Long reservationId;
    private String maintenanceType;
    private String description;
    private Integer mileageAtService;
    private BigDecimal cost;
    private LocalDateTime serviceDate;
    private LocalDateTime createdAt;
}
```

#### 데이터베이스 설계
```sql
-- 차량 테이블
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    manufacturer VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year VARCHAR(4) NOT NULL,
    engine_type VARCHAR(20) NOT NULL DEFAULT 'GASOLINE',
    current_mileage INTEGER DEFAULT 0,
    photo_url VARCHAR(500),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 차량 이력 테이블
CREATE TABLE vehicle_histories (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    reservation_id BIGINT REFERENCES reservations(id),
    maintenance_type VARCHAR(100) NOT NULL,
    description TEXT,
    mileage_at_service INTEGER,
    cost DECIMAL(10,2),
    service_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### API 설계
```
GET    /api/vehicles              # 내 차량 목록
POST   /api/vehicles              # 차량 등록
GET    /api/vehicles/{id}         # 차량 상세 조회
PUT    /api/vehicles/{id}         # 차량 정보 수정
DELETE /api/vehicles/{id}         # 차량 삭제
GET    /api/vehicles/{id}/history # 정비 이력
POST   /api/vehicles/{id}/mileage # 주행거리 업데이트
```

#### 테스트 계획
- **단위 테스트**: VehicleService CRUD 테스트
- **통합 테스트**: 차량 등록/조회 API 테스트
- **비즈니스 로직 테스트**: 차량 소유권 검증

---

## Task 3: 정비소 관리 시스템

### 📋 Task 개요
- **목표**: 정비소 정보 관리, 운영시간, 서비스 항목 관리
- **우선순위**: 3
- **예상 기간**: 1-2주
- **의존성**: Task 1 (사용자 인증)

### 🏗️ 기술 설계

#### 엔티티 설계
```java
@Entity
public class Garage {
    private Long id;
    private String businessName;
    private String businessNumber;
    private String address;
    private String detailedAddress;
    private String phone;
    private String email;
    private Double latitude;
    private Double longitude;
    private String description;
    private String photoUrl;
    private GarageStatus status;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Entity
public class GarageOperatingHours {
    private Long id;
    private Long garageId;
    private DayOfWeek dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean isHoliday;
    private LocalDateTime createdAt;
}

@Entity
public class GarageService {
    private Long id;
    private Long garageId;
    private String serviceName;
    private String serviceCategory;
    private String description;
    private BigDecimal basePrice;
    private Integer estimatedDuration;
    private boolean isAvailable;
    private LocalDateTime createdAt;
}

@Entity
public class Mechanic {
    private Long id;
    private Long garageId;
    private Long userId;
    private String name;
    private String certification;
    private Integer experienceYears;
    private String specialization;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### API 설계
```
# 정비소 검색 및 조회
GET    /api/garages                    # 정비소 검색 (위치, 서비스 기반)
GET    /api/garages/{id}               # 정비소 상세 정보
GET    /api/garages/nearby             # 위치 기반 검색
GET    /api/garages/{id}/services      # 정비소 서비스 목록
GET    /api/garages/{id}/mechanics     # 정비사 목록
GET    /api/garages/{id}/reviews       # 정비소 리뷰 목록

# 정비소 관리 (사업자용)
POST   /api/garages                    # 정비소 등록
PUT    /api/garages/{id}               # 정비소 정보 수정
POST   /api/garages/{id}/services      # 서비스 추가
PUT    /api/garages/{id}/services/{serviceId} # 서비스 수정
DELETE /api/garages/{id}/services/{serviceId} # 서비스 삭제
POST   /api/garages/{id}/mechanics     # 정비사 추가
PUT    /api/garages/{id}/mechanics/{mechanicId} # 정비사 정보 수정
```

---

## Task 4: 예약 시스템

### 📋 Task 개요
- **목표**: 정비 예약 생성, 관리, 상태 추적
- **우선순위**: 4 (핵심 비즈니스 로직)
- **예상 기간**: 2-3주
- **의존성**: Task 1, 2, 3

### 🏗️ 기술 설계

#### 엔티티 설계
```java
@Entity
public class Reservation {
    private Long id;
    private String reservationNumber;
    private Long customerId;
    private Long garageId;
    private Long vehicleId;
    private Long mechanicId;
    private ReservationStatus status;
    private LocalDateTime reservationDateTime;
    private String serviceType;
    private String description;
    private UrgencyLevel urgencyLevel;
    private Integer estimatedDuration;
    private BigDecimal estimatedCost;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private String photoUrl;
    private String customerNotes;
    private String mechanicNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum ReservationStatus {
    PENDING,        // 예약 대기
    CONFIRMED,      // 예약 확정
    IN_PROGRESS,    // 정비 진행중
    COMPLETED,      // 정비 완료
    CANCELLED       // 예약 취소
}

public enum UrgencyLevel {
    LOW, NORMAL, HIGH, URGENT
}

@Entity
public class AvailableSlot {
    private Long id;
    private Long garageId;
    private Long mechanicId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isAvailable;
    private String serviceType;
    private LocalDateTime createdAt;
}
```

#### API 설계
```
# 예약 관리
POST   /api/reservations                    # 예약 생성
GET    /api/reservations                    # 내 예약 목록
GET    /api/reservations/{id}               # 예약 상세
PUT    /api/reservations/{id}               # 예약 수정
DELETE /api/reservations/{id}               # 예약 취소
GET    /api/reservations/{id}/status        # 진행상황 조회

# 예약 가능 시간
GET    /api/garages/{garageId}/available-slots  # 예약 가능 시간 조회
POST   /api/garages/{garageId}/available-slots  # 예약 가능 시간 생성 (정비소용)

# 정비소용 예약 관리
GET    /api/admin/reservations               # 정비소 예약 관리
PUT    /api/admin/reservations/{id}/confirm  # 예약 확정
PUT    /api/admin/reservations/{id}/start    # 정비 시작
PUT    /api/admin/reservations/{id}/complete # 정비 완료
```

---

## Task 5: 견적 관리 시스템

### 📋 Task 개요
- **목표**: 견적서 생성, 관리, 승인/거부 프로세스
- **우선순위**: 5
- **예상 기간**: 1-2주
- **의존성**: Task 4 (예약 시스템)

### 🏗️ 기술 설계

#### 엔티티 설계
```java
@Entity
public class Quote {
    private Long id;
    private Long reservationId;
    private String quoteNumber;
    private QuoteStatus status;
    private BigDecimal laborCost;
    private BigDecimal partsCost;
    private BigDecimal totalCost;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private String description;
    private LocalDateTime validUntil;
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum QuoteStatus {
    PENDING, APPROVED, REJECTED, EXPIRED
}

@Entity
public class QuoteItem {
    private Long id;
    private Long quoteId;
    private String itemName;
    private ItemType itemType;
    private String category;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String description;
    private LocalDateTime createdAt;
}

public enum ItemType {
    LABOR, PARTS, CONSUMABLES, OTHER
}
```

#### API 설계
```
# 견적 관리
POST   /api/quotes                      # 견적서 생성 (정비소용)
GET    /api/quotes/{id}                 # 견적서 조회
PUT    /api/quotes/{id}                 # 견적서 수정 (정비소용)
PUT    /api/quotes/{id}/approve         # 견적 승인 (고객용)
PUT    /api/quotes/{id}/reject          # 견적 거부 (고객용)
GET    /api/reservations/{id}/quotes    # 예약별 견적 목록

# 견적 항목 관리
POST   /api/quotes/{id}/items           # 견적 항목 추가
PUT    /api/quotes/{id}/items/{itemId}  # 견적 항목 수정
DELETE /api/quotes/{id}/items/{itemId}  # 견적 항목 삭제
```

---

## Task 6: 정비 프로세스 시스템

### 📋 Task 개요
- **목표**: 정비 진행 단계 관리 및 실시간 상태 추적
- **우선순위**: 6
- **예상 기간**: 2주
- **의존성**: Task 4, 5

### 🏗️ 기술 설계

#### 엔티티 설계
```java
@Entity
public class MaintenanceProcess {
    private Long id;
    private Long reservationId;
    private MaintenanceStage currentStage;
    private Integer progressPercentage;
    private String mechanicComment;
    private String photoUrl;
    private LocalDateTime stageStartTime;
    private LocalDateTime estimatedEndTime;
    private LocalDateTime actualEndTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum MaintenanceStage {
    RECEIVED,           // 접수 완료
    DIAGNOSING,         // 진단 중
    QUOTE_PENDING,      // 견적 대기
    IN_PROGRESS,        // 정비 진행
    QUALITY_CHECK,      // 품질 검사
    COMPLETED           // 완료
}

@Entity
public class MaintenanceStageLog {
    private Long id;
    private Long reservationId;
    private MaintenanceStage stage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String comment;
    private String photoUrl;
    private Long mechanicId;
    private LocalDateTime createdAt;
}
```

#### API 설계
```
# 정비 진행 상황 (고객용)
GET    /api/maintenance/{reservationId}/status     # 진행상황 조회
GET    /api/maintenance/{reservationId}/timeline   # 진행 타임라인

# 정비 관리 (정비소용)
PUT    /api/maintenance/{reservationId}/stage      # 단계 업데이트
POST   /api/maintenance/{reservationId}/comment    # 코멘트 추가
POST   /api/maintenance/{reservationId}/photo      # 사진 업로드
GET    /api/maintenance/{reservationId}/logs       # 진행 로그 조회
```

---

## Task 7: 결제 시스템

### 📋 Task 개요
- **목표**: 결제 처리, 영수증 발행, 환불 관리
- **우선순위**: 7
- **예상 기간**: 2-3주
- **의존성**: Task 5 (견적 관리)

### 🏗️ 기술 설계

#### 엔티티 설계
```java
@Entity
public class Payment {
    private Long id;
    private Long reservationId;
    private String paymentNumber;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionId;
    private String pgTransactionId;
    private String receiptUrl;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum PaymentMethod {
    CARD, BANK_TRANSFER, MOBILE, CASH, VIRTUAL_ACCOUNT
}

public enum PaymentStatus {
    PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED, PARTIALLY_REFUNDED
}
```

#### API 설계
```
# 결제 관리
POST   /api/payments                    # 결제 요청
GET    /api/payments/{id}               # 결제 상세
POST   /api/payments/{id}/refund        # 환불 요청
GET    /api/payments/history            # 결제 이력
GET    /api/reservations/{id}/payments  # 예약별 결제 내역

# 결제 게이트웨이 연동
POST   /api/payments/webhook            # PG사 웹훅 처리
GET    /api/payments/{id}/receipt       # 영수증 조회
```

---

## Task 8: 리뷰 평점 시스템

### 📋 Task 개요
- **목표**: 고객 리뷰 작성, 정비소 답글, 평점 관리
- **우선순위**: 8
- **예상 기간**: 1-2주
- **의존성**: Task 4 (예약 시스템)

### 🏗️ 기술 설계

#### 엔티티 설계
```java
@Entity
public class Review {
    private Long id;
    private Long reservationId;
    private Long customerId;
    private Long garageId;
    private Integer rating;           // 1-5점
    private String content;
    private String photoUrl;
    private boolean isRecommended;
    private boolean isAnonymous;
    private Integer helpfulCount;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

public enum ReviewStatus {
    ACTIVE, HIDDEN, DELETED
}

@Entity
public class ReviewReply {
    private Long id;
    private Long reviewId;
    private Long garageId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Entity
public class ReviewHelpful {
    private Long id;
    private Long reviewId;
    private Long userId;
    private boolean isHelpful;
    private LocalDateTime createdAt;
}
```

#### API 설계
```
# 리뷰 관리
POST   /api/reviews                     # 리뷰 작성
GET    /api/reviews                     # 리뷰 목록 (필터링 지원)
GET    /api/reviews/{id}                # 리뷰 상세
PUT    /api/reviews/{id}                # 리뷰 수정
DELETE /api/reviews/{id}                # 리뷰 삭제
POST   /api/reviews/{id}/reply          # 리뷰 답글 (정비소용)
POST   /api/reviews/{id}/helpful        # 도움이 되었나요?

# 정비소별 리뷰
GET    /api/garages/{id}/reviews        # 정비소 리뷰 목록
GET    /api/garages/{id}/rating-stats   # 평점 통계
```

---

## Task 9: 알림 시스템

### 📋 Task 개요
- **목표**: 실시간 알림, 예약/정비 상태 변경 알림
- **우선순위**: 9
- **예상 기간**: 1-2주
- **의존성**: 모든 이전 Task (통합 기능)

### 🏗️ 기술 설계

#### 엔티티 설계
```java
@Entity
public class Notification {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private String targetUrl;
    private boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime scheduledTime;
    private LocalDateTime sentTime;
    private DeliveryStatus deliveryStatus;
    private LocalDateTime createdAt;
}

public enum NotificationType {
    RESERVATION_CONFIRMED,
    MAINTENANCE_PROGRESS,
    QUOTE_READY,
    MAINTENANCE_COMPLETED,
    PAYMENT_COMPLETED,
    REVIEW_REQUEST,
    REGULAR_CHECK_REMINDER
}

public enum DeliveryStatus {
    PENDING, SENT, FAILED, CANCELLED
}

@Entity
public class NotificationTemplate {
    private Long id;
    private NotificationType type;
    private String titleTemplate;
    private String messageTemplate;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### API 설계
```
# 알림 관리
GET    /api/notifications               # 알림 목록
PUT    /api/notifications/{id}/read     # 알림 읽음 처리
POST   /api/notifications/settings     # 알림 설정
GET    /api/notifications/unread-count  # 읽지 않은 알림 수

# 알림 설정
GET    /api/users/notification-settings # 알림 설정 조회
PUT    /api/users/notification-settings # 알림 설정 변경
```

---

## Task 10: 관리자 시스템

### 📋 Task 개요
- **목표**: 대시보드, 통계, 시스템 관리 기능
- **우선순위**: 10
- **예상 기간**: 2-3주
- **의존성**: 모든 이전 Task

### 🏗️ 기술 설계

#### 엔티티 설계
```java
@Entity
public class DashboardStats {
    private Long id;
    private Long garageId;
    private LocalDate date;
    private Integer totalReservations;
    private Integer completedReservations;
    private Integer cancelledReservations;
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
    private LocalDateTime createdAt;
}

@Entity
public class SystemLog {
    private Long id;
    private Long userId;
    private String action;
    private String targetType;
    private Long targetId;
    private String ipAddress;
    private String userAgent;
    private String details; // JSON 형태
    private LocalDateTime createdAt;
}
```

#### API 설계
```
# 대시보드
GET    /api/admin/dashboard              # 종합 대시보드
GET    /api/admin/dashboard/stats        # 통계 데이터
GET    /api/admin/dashboard/charts       # 차트 데이터

# 예약 관리
GET    /api/admin/reservations           # 전체 예약 관리
GET    /api/admin/reservations/stats     # 예약 통계
PUT    /api/admin/reservations/{id}/status # 예약 상태 변경

# 정비소 관리
GET    /api/admin/garages                # 정비소 목록 관리
PUT    /api/admin/garages/{id}/status    # 정비소 승인/정지
GET    /api/admin/garages/stats          # 정비소 통계

# 고객 관리
GET    /api/admin/customers              # 고객 목록
GET    /api/admin/customers/stats        # 고객 통계
PUT    /api/admin/customers/{id}/status  # 고객 계정 관리

# 수익 분석
GET    /api/admin/revenue                # 수익 분석
GET    /api/admin/revenue/summary        # 수익 요약
GET    /api/admin/revenue/trends         # 수익 트렌드

# 시스템 관리
GET    /api/admin/system/logs            # 시스템 로그
GET    /api/admin/system/health          # 시스템 상태
POST   /api/admin/system/notifications   # 공지사항 발송
```

---

## 4. 공통 기술 스펙

### 4.1 API 공통 규칙
참조: [003-api-design.mdc]

#### 기본 정보
- **Base URL**: `http://localhost:8080/api/v1` (로컬), `https://api.car-center.com/api/v1` (운영)
- **인증 방식**: JWT Bearer Token
- **응답 형식**: JSON
- **문자 인코딩**: UTF-8

#### 공통 응답 형식
```json
// 성공 응답
{
  "success": true,
  "data": {
    // 응답 데이터
  },
  "message": "성공 메시지"
}

// 에러 응답
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지",
    "details": {
      // 상세 에러 정보
    }
  }
}
```

#### HTTP 상태 코드
- `200 OK`: 요청 성공
- `201 Created`: 리소스 생성 성공
- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스 없음
- `500 Internal Server Error`: 서버 오류

### 4.2 보안 가이드라인
참조: [005-security-jwt.mdc]

#### JWT 토큰 사용
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 권한별 접근 제어
```java
@PreAuthorize("hasRole('CUSTOMER')")
public ResponseEntity<List<Vehicle>> getMyVehicles();

@PreAuthorize("hasRole('GARAGE_ADMIN')")
public ResponseEntity<List<Reservation>> getGarageReservations();

@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public ResponseEntity<DashboardStats> getSystemStats();
```

### 4.3 데이터베이스 공통 규칙
참조: [004-database-mybatis.mdc]

#### 테이블 명명 규칙
- **테이블명**: 복수형, snake_case (예: `users`, `vehicle_histories`)
- **컬럼명**: snake_case (예: `created_at`, `updated_at`)
- **기본키**: `id BIGSERIAL PRIMARY KEY`
- **필수 컬럼**: `created_at`, `updated_at` (타임스탬프)

#### 공통 인덱스 전략
```sql
-- 성능 최적화를 위한 주요 인덱스
CREATE INDEX idx_reservation_customer_id ON reservations(customer_id);
CREATE INDEX idx_reservation_garage_id ON reservations(garage_id);
CREATE INDEX idx_reservation_datetime ON reservations(reservation_date_time);
CREATE INDEX idx_vehicle_owner_id ON vehicles(owner_id);
CREATE INDEX idx_garage_location ON garages(latitude, longitude);
```

### 4.4 에러 처리
참조: [007-error-handling.mdc]

#### 에러 코드 정의
```java
// 인증 관련
AUTH_001: 인증 토큰이 없습니다
AUTH_002: 유효하지 않은 토큰입니다
AUTH_003: 토큰이 만료되었습니다
AUTH_004: 권한이 없습니다

// 사용자 관련
USER_001: 존재하지 않는 사용자입니다
USER_002: 이미 존재하는 이메일입니다
USER_003: 비밀번호가 일치하지 않습니다

// 예약 관련
RESERVATION_001: 예약 가능한 시간이 아닙니다
RESERVATION_002: 이미 예약된 시간입니다
RESERVATION_003: 예약을 찾을 수 없습니다

// 일반 오류
VALIDATION_001: 입력값이 올바르지 않습니다
SERVER_001: 서버 내부 오류가 발생했습니다
```

### 4.5 테스트 전략
참조: [008-testing-guidelines.mdc]

#### 테스트 계층별 가이드
- **단위 테스트**: Service 레이어 로직 테스트 (JUnit 5 + Mockito)
- **통합 테스트**: API 엔드포인트 테스트 (@SpringBootTest)
- **E2E 테스트**: 사용자 시나리오 테스트

#### 테스트 데이터 관리
- H2 인메모리 DB 활용 (로컬 테스트)
- 테스트용 더미 데이터 생성
- @Transactional로 테스트 격리

---

## 5. 개발 계획

### 5.1 Phase별 개발 계획

#### Phase 1: 기반 시스템 (4-5주)
- **Task 1**: 사용자 인증 시스템 (1-2주)
- **Task 2**: 차량 관리 시스템 (1주)
- **Task 3**: 정비소 관리 시스템 (1-2주)

#### Phase 2: 핵심 비즈니스 로직 (6-7주)
- **Task 4**: 예약 시스템 (2-3주)
- **Task 5**: 견적 관리 시스템 (1-2주)
- **Task 6**: 정비 프로세스 시스템 (2주)

#### Phase 3: 부가 기능 (4-5주)
- **Task 7**: 결제 시스템 (2-3주)
- **Task 8**: 리뷰 평점 시스템 (1-2주)

#### Phase 4: 고도화 (3-5주)
- **Task 9**: 알림 시스템 (1-2주)
- **Task 10**: 관리자 시스템 (2-3주)

### 5.2 개발 환경 설정
참조: [006-local-dev-environment.mdc]

#### 로컬 개발 환경
- **Database**: H2 메모리 DB
- **Profile**: `spring.profiles.active=local`
- **실행 명령어**: `./gradlew bootRun --args='--spring.profiles.active=local'`

#### 개발 서버 환경
- **Database**: PostgreSQL 개발 서버
- **Profile**: `spring.profiles.active=dev`

### 5.3 각 Task별 완료 기준

#### 개발 완료 기준
1. **기능 구현**: 모든 API 엔드포인트 구현
2. **단위 테스트**: 코드 커버리지 80% 이상
3. **통합 테스트**: 주요 시나리오 테스트 완료
4. **문서화**: API 문서 및 기술 문서 작성
5. **코드 리뷰**: 팀 리뷰 완료 및 승인

#### 테스트 완료 기준
1. **기능 테스트**: 모든 요구사항 만족
2. **성능 테스트**: 응답시간 기준 충족
3. **보안 테스트**: 인증/권한 검증 완료
4. **통합 테스트**: 다른 모듈과의 연동 확인

### 5.4 리스크 관리

#### 기술적 리스크
- **데이터베이스 성능**: 인덱스 최적화 및 쿼리 튜닝
- **보안 취약점**: 정기적인 보안 검토 및 업데이트
- **확장성**: 마이크로서비스 전환 고려

#### 일정 리스크
- **의존성 지연**: Task 간 의존관계로 인한 지연 가능성
- **요구사항 변경**: 고객 피드백에 따른 요구사항 변경
- **인력 부족**: 개발 인력 확보 및 교육

---

이 통합 PRD는 Task별로 독립적인 개발과 테스트가 가능하도록 설계되었습니다. 각 Task는 명확한 의존성과 완료 기준을 가지고 있어, 단계별로 점진적인 개발이 가능합니다.
