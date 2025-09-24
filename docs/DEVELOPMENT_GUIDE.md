# Car Center API 개발 가이드

## 목차
1. [개발 환경 설정](#개발-환경-설정)
2. [프로젝트 구조](#프로젝트-구조)
3. [코딩 컨벤션](#코딩-컨벤션)
4. [도메인 개발 가이드](#도메인-개발-가이드)
5. [API 개발 가이드](#api-개발-가이드)
6. [데이터베이스 가이드](#데이터베이스-가이드)
7. [보안 가이드](#보안-가이드)
8. [테스트 가이드](#테스트-가이드)

## 개발 환경 설정

### 필수 요구사항
- Java 17 이상
- Gradle 8.14.3
- PostgreSQL 13 이상
- IDE (IntelliJ IDEA 권장)

### 로컬 개발 환경 설정

1. **PostgreSQL 설치 및 설정**
```bash
# PostgreSQL 설치 (Ubuntu/Debian)
sudo apt-get install postgresql postgresql-contrib

# PostgreSQL 설치 (macOS)
brew install postgresql

# PostgreSQL 설치 (Windows)
# https://www.postgresql.org/download/windows/ 에서 다운로드
```

2. **데이터베이스 생성**
```sql
-- PostgreSQL에 접속
psql -U postgres

-- 데이터베이스 및 사용자 생성
CREATE DATABASE carcenter_local;
CREATE USER carcenter_local WITH PASSWORD 'carcenter123';
GRANT ALL PRIVILEGES ON DATABASE carcenter_local TO carcenter_local;
```

3. **환경변수 설정**
```bash
# .bashrc 또는 .zshrc에 추가
export DB_USERNAME=carcenter_local
export DB_PASSWORD=carcenter123
export JWT_SECRET=carcenter-jwt-secret-key-2024-very-long-and-secure
```

## 프로젝트 구조

### 패키지 구조 원칙
- **도메인 중심 설계**: 각 도메인별로 패키지를 분리
- **계층 분리**: Controller, Service, Repository 계층 명확히 분리
- **의존성 방향**: Controller → Service → Repository

### 표준 패키지 구조
```
com.carcenter.domain.{domain}/
├── controller/          # REST API 컨트롤러
├── service/            # 비즈니스 로직
├── repository/         # 데이터 접근 계층
├── entity/             # 도메인 엔티티
├── dto/                # 데이터 전송 객체
│   ├── request/        # 요청 DTO
│   └── response/       # 응답 DTO
└── exception/          # 도메인별 예외
```

## 코딩 컨벤션

### Java 코딩 스타일
- **클래스명**: PascalCase (예: `UserController`)
- **메서드명**: camelCase (예: `createUser`)
- **변수명**: camelCase (예: `userName`)
- **상수명**: UPPER_SNAKE_CASE (예: `MAX_RETRY_COUNT`)

### 어노테이션 사용 규칙
```java
// 컨트롤러
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated

// 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

// 리포지토리
@Repository
@Mapper
```

### Lombok 사용 규칙
```java
// 엔티티
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

// DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor

// 서비스/컨트롤러
@RequiredArgsConstructor
```

## 도메인 개발 가이드

### 1. 새로운 도메인 생성

#### 1.1 엔티티 생성
```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Car {
    private Long id;
    private String model;
    private String brand;
    private Integer year;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 1.2 DTO 생성
```java
// 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CarCreateRequest {
    @NotBlank(message = "모델명은 필수입니다")
    private String model;
    
    @NotBlank(message = "브랜드는 필수입니다")
    private String brand;
    
    @NotNull(message = "연도는 필수입니다")
    @Min(value = 1900, message = "연도는 1900년 이후여야 합니다")
    private Integer year;
    
    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다")
    private BigDecimal price;
}

// 응답 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarResponse {
    private Long id;
    private String model;
    private String brand;
    private Integer year;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static CarResponse from(Car car) {
        return CarResponse.builder()
                .id(car.getId())
                .model(car.getModel())
                .brand(car.getBrand())
                .year(car.getYear())
                .price(car.getPrice())
                .createdAt(car.getCreatedAt())
                .updatedAt(car.getUpdatedAt())
                .build();
    }
}
```

#### 1.3 리포지토리 생성
```java
@Mapper
public interface CarRepository {
    void save(Car car);
    Optional<Car> findById(Long id);
    List<Car> findAll();
    void update(Car car);
    void deleteById(Long id);
    List<Car> findByBrand(String brand);
}
```

#### 1.4 서비스 생성
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarService {
    
    private final CarRepository carRepository;
    
    @Transactional
    public CarResponse createCar(CarCreateRequest request) {
        Car car = Car.builder()
                .model(request.getModel())
                .brand(request.getBrand())
                .year(request.getYear())
                .price(request.getPrice())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        carRepository.save(car);
        return CarResponse.from(car);
    }
    
    public CarResponse getCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Car not found with id: " + id));
        return CarResponse.from(car);
    }
    
    public List<CarResponse> getCars() {
        return carRepository.findAll().stream()
                .map(CarResponse::from)
                .collect(Collectors.toList());
    }
}
```

#### 1.5 컨트롤러 생성
```java
@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
@Validated
public class CarController {
    
    private final CarService carService;
    
    @PostMapping
    public ResponseEntity<CarResponse> createCar(@Valid @RequestBody CarCreateRequest request) {
        CarResponse response = carService.createCar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCar(@PathVariable Long id) {
        CarResponse response = carService.getCar(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<CarResponse>> getCars() {
        List<CarResponse> responses = carService.getCars();
        return ResponseEntity.ok(responses);
    }
}
```

#### 1.6 MyBatis 매퍼 생성
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.carcenter.domain.car.repository.CarRepository">
    
    <resultMap id="CarResultMap" type="com.carcenter.domain.car.entity.Car">
        <id property="id" column="id"/>
        <result property="model" column="model"/>
        <result property="brand" column="brand"/>
        <result property="year" column="year"/>
        <result property="price" column="price"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>
    
    <insert id="save" parameterType="com.carcenter.domain.car.entity.Car" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO cars (model, brand, year, price, created_at, updated_at)
        VALUES (#{model}, #{brand}, #{year}, #{price}, #{createdAt}, #{updatedAt})
    </insert>
    
    <select id="findById" parameterType="long" resultMap="CarResultMap">
        SELECT * FROM cars WHERE id = #{id}
    </select>
    
    <select id="findAll" resultMap="CarResultMap">
        SELECT * FROM cars ORDER BY created_at DESC
    </select>
    
    <update id="update" parameterType="com.carcenter.domain.car.entity.Car">
        UPDATE cars
        SET model = #{model},
            brand = #{brand},
            year = #{year},
            price = #{price},
            updated_at = #{updatedAt}
        WHERE id = #{id}
    </update>
    
    <delete id="deleteById" parameterType="long">
        DELETE FROM cars WHERE id = #{id}
    </delete>
    
    <select id="findByBrand" parameterType="string" resultMap="CarResultMap">
        SELECT * FROM cars WHERE brand = #{brand} ORDER BY created_at DESC
    </select>
    
</mapper>
```

## API 개발 가이드

### RESTful API 설계 원칙

#### HTTP 메서드 사용
- `GET`: 조회
- `POST`: 생성
- `PUT`: 전체 수정
- `PATCH`: 부분 수정
- `DELETE`: 삭제

#### URL 설계 규칙
```
GET    /api/users          # 사용자 목록 조회
GET    /api/users/{id}     # 특정 사용자 조회
POST   /api/users          # 사용자 생성
PUT    /api/users/{id}     # 사용자 전체 수정
PATCH  /api/users/{id}     # 사용자 부분 수정
DELETE /api/users/{id}     # 사용자 삭제
```

#### 응답 형식
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "user1",
    "email": "user1@example.com"
  }
}
```

#### 에러 응답 형식
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "입력값 검증에 실패했습니다",
  "errors": {
    "username": "사용자명은 필수입니다",
    "email": "올바른 이메일 형식이 아닙니다"
  }
}
```

### 페이징 처리
```java
@GetMapping
public ResponseEntity<Page<CarResponse>> getCars(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "id") String sortBy,
    @RequestParam(defaultValue = "desc") String sortDir) {
    
    Pageable pageable = PageRequest.of(page, size, 
        Sort.by(Sort.Direction.fromString(sortDir), sortBy));
    
    Page<CarResponse> responses = carService.getCars(pageable);
    return ResponseEntity.ok(responses);
}
```

## 데이터베이스 가이드

### 테이블 설계 규칙

#### 기본 컬럼
```sql
CREATE TABLE cars (
    id BIGSERIAL PRIMARY KEY,
    model VARCHAR(100) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### 인덱스 생성
```sql
-- 단일 컬럼 인덱스
CREATE INDEX idx_cars_brand ON cars(brand);
CREATE INDEX idx_cars_year ON cars(year);

-- 복합 인덱스
CREATE INDEX idx_cars_brand_year ON cars(brand, year);
```

#### 트리거 설정
```sql
-- 업데이트 시간 자동 갱신
CREATE TRIGGER update_cars_updated_at 
    BEFORE UPDATE ON cars 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
```

### MyBatis 사용 가이드

#### 동적 쿼리
```xml
<select id="findCarsByCondition" parameterType="CarSearchCondition" resultMap="CarResultMap">
    SELECT * FROM cars
    <where>
        <if test="brand != null and brand != ''">
            AND brand = #{brand}
        </if>
        <if test="minYear != null">
            AND year >= #{minYear}
        </if>
        <if test="maxYear != null">
            AND year <= #{maxYear}
        </if>
        <if test="minPrice != null">
            AND price >= #{minPrice}
        </if>
        <if test="maxPrice != null">
            AND price <= #{maxPrice}
        </if>
    </where>
    ORDER BY created_at DESC
</select>
```

#### 페이징 쿼리
```xml
<select id="findCarsWithPaging" parameterType="map" resultMap="CarResultMap">
    SELECT * FROM cars
    ORDER BY ${sortBy} ${sortDir}
    LIMIT #{size} OFFSET #{offset}
</select>
```

## 보안 가이드

### JWT 토큰 관리

#### 토큰 생성
```java
public String generateToken(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
    Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);

    return Jwts.builder()
            .setSubject(userPrincipal.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
}
```

#### 토큰 검증
```java
public boolean validateToken(String authToken) {
    try {
        Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
        return true;
    } catch (JwtException | IllegalArgumentException e) {
        log.error("Invalid JWT token: {}", e.getMessage());
        return false;
    }
}
```

### 권한 체크
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public ResponseEntity<List<UserResponse>> getUsersForAdmin() {
    // 관리자만 접근 가능
}

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@GetMapping("/profile")
public ResponseEntity<UserResponse> getProfile() {
    // 사용자와 관리자 모두 접근 가능
}
```

## 테스트 가이드

### 단위 테스트
```java
@ExtendWith(MockitoExtension.class)
class CarServiceTest {
    
    @Mock
    private CarRepository carRepository;
    
    @InjectMocks
    private CarService carService;
    
    @Test
    @DisplayName("자동차 생성 성공")
    void createCar_Success() {
        // Given
        CarCreateRequest request = new CarCreateRequest("Model S", "Tesla", 2023, new BigDecimal("100000"));
        Car savedCar = Car.builder()
                .id(1L)
                .model("Model S")
                .brand("Tesla")
                .year(2023)
                .price(new BigDecimal("100000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(carRepository.save(any(Car.class))).thenReturn(savedCar);
        
        // When
        CarResponse response = carService.createCar(request);
        
        // Then
        assertThat(response.getModel()).isEqualTo("Model S");
        assertThat(response.getBrand()).isEqualTo("Tesla");
        verify(carRepository).save(any(Car.class));
    }
}
```

### 통합 테스트
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CarControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("자동차 생성 API 테스트")
    void createCar_IntegrationTest() {
        // Given
        CarCreateRequest request = new CarCreateRequest("Model S", "Tesla", 2023, new BigDecimal("100000"));
        
        // When
        ResponseEntity<CarResponse> response = restTemplate.postForEntity(
            "/api/cars", request, CarResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getModel()).isEqualTo("Model S");
    }
}
```

### 테스트 실행
```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests CarServiceTest

# 통합 테스트 실행
./gradlew integrationTest
```

## 성능 최적화 가이드

### 데이터베이스 최적화
1. **인덱스 최적화**: 자주 조회되는 컬럼에 인덱스 생성
2. **쿼리 최적화**: N+1 문제 해결, 불필요한 조인 제거
3. **연결 풀 설정**: HikariCP 설정 최적화

### 애플리케이션 최적화
1. **캐싱**: Redis를 이용한 캐싱 전략
2. **비동기 처리**: @Async를 이용한 비동기 처리
3. **배치 처리**: 대량 데이터 처리 시 배치 사용

## 모니터링 가이드

### Actuator 설정
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

### 로깅 설정
```yaml
logging:
  level:
    com.carcenter: INFO
    org.springframework.security: WARN
    org.mybatis: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/car-center-api.log
```

이 가이드를 따라 개발하시면 일관성 있고 유지보수가 용이한 코드를 작성할 수 있습니다.
