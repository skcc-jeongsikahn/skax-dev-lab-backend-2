# Car Center API

Spring Boot 3.4.1 기반의 RESTful API 서버입니다.

## 기술 스택

- **Spring Boot**: 3.4.1
- **Java**: 17
- **Gradle**: 8.14.3
- **Database**: PostgreSQL
- **ORM**: MyBatis
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle

## 프로젝트 구조

```
src/main/java/com/carcenter/
├── CarCenterApplication.java          # 메인 애플리케이션 클래스
├── config/                            # 설정 클래스
│   ├── SecurityConfig.java           # Spring Security 설정
│   └── MyBatisConfig.java            # MyBatis 설정
├── domain/                           # 도메인별 패키지
│   ├── auth/                         # 인증 도메인
│   │   ├── controller/
│   │   ├── service/
│   │   └── dto/
│   └── user/                         # 사용자 도메인
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       ├── dto/
│       └── exception/
├── security/                         # 보안 관련 클래스
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   └── CustomUserDetailsService.java
└── global/                           # 전역 설정
    └── exception/
        └── GlobalExceptionHandler.java
```

## 환경 설정

### 1. 데이터베이스 설정

PostgreSQL 데이터베이스를 설치하고 다음 설정을 진행하세요:

```sql
-- 데이터베이스 생성
CREATE DATABASE carcenter_local;
CREATE DATABASE carcenter_dev;
CREATE DATABASE carcenter_prod;

-- 사용자 생성
CREATE USER carcenter_local WITH PASSWORD 'carcenter123';
CREATE USER carcenter_dev WITH PASSWORD 'carcenter123';
CREATE USER carcenter_prod WITH PASSWORD 'carcenter123';

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE carcenter_local TO carcenter_local;
GRANT ALL PRIVILEGES ON DATABASE carcenter_dev TO carcenter_dev;
GRANT ALL PRIVILEGES ON DATABASE carcenter_prod TO carcenter_prod;
```

### 2. 환경변수 설정

로컬 개발 환경에서는 다음 환경변수를 설정하세요:

```bash
# 데이터베이스 설정
export DB_USERNAME=carcenter_local
export DB_PASSWORD=carcenter123

# JWT 시크릿 (프로덕션에서는 반드시 변경)
export JWT_SECRET=your-very-long-and-secure-jwt-secret-key-here
```

## 실행 방법

### 1. 프로젝트 빌드

```bash
# Gradle Wrapper를 사용한 빌드
./gradlew build

# Windows의 경우
gradlew.bat build
```

### 2. 애플리케이션 실행

```bash
# 로컬 환경으로 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 JAR 파일로 실행
java -jar build/libs/car-center-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### 3. 개발 서버 실행

```bash
# 개발 모드로 실행 (자동 재시작)
./gradlew bootRun --args='--spring.profiles.active=local'
```

## API 엔드포인트

### 인증 API

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | 로그인 | No |
| POST | `/api/auth/refresh` | 토큰 갱신 | No |
| POST | `/api/auth/logout` | 로그아웃 | Yes |

### 사용자 API

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/users` | 사용자 생성 | Yes |
| GET | `/api/users/{id}` | 사용자 조회 | Yes |
| GET | `/api/users` | 사용자 목록 | Yes |

## 개발 가이드

### 1. 새로운 도메인 추가

새로운 도메인을 추가할 때는 다음 구조를 따르세요:

```
src/main/java/com/carcenter/domain/{domain}/
├── controller/           # REST 컨트롤러
├── service/             # 비즈니스 로직
├── repository/          # 데이터 접근 계층
├── entity/              # 엔티티 클래스
├── dto/                 # 데이터 전송 객체
└── exception/           # 도메인별 예외
```

### 2. 컨트롤러 작성 가이드

```java
@RestController
@RequestMapping("/api/{domain}")
@RequiredArgsConstructor
public class {Domain}Controller {

    private final {Domain}Service {domain}Service;

    @PostMapping
    public ResponseEntity<{Domain}Response> create{Domain}(@Valid @RequestBody {Domain}CreateRequest request) {
        {Domain}Response response = {domain}Service.create{Domain}(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### 3. 서비스 작성 가이드

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class {Domain}Service {

    private final {Domain}Repository {domain}Repository;

    @Transactional
    public {Domain}Response create{Domain}({Domain}CreateRequest request) {
        // 비즈니스 로직 구현
    }
}
```

### 4. MyBatis 매퍼 작성 가이드

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.carcenter.domain.{domain}.repository.{Domain}Repository">
    
    <resultMap id="{Domain}ResultMap" type="com.carcenter.domain.{domain}.entity.{Domain}">
        <!-- 매핑 설정 -->
    </resultMap>

    <insert id="save" parameterType="com.carcenter.domain.{domain}.entity.{Domain}">
        <!-- INSERT 쿼리 -->
    </insert>
</mapper>
```

## 보안 설정

### JWT 토큰 사용

1. **로그인**: `/api/auth/login` 엔드포인트로 로그인
2. **토큰 저장**: 응답받은 `accessToken`을 Authorization 헤더에 포함
3. **API 호출**: `Authorization: Bearer {accessToken}` 형식으로 요청

### 권한 설정

- `ROLE_ADMIN`: 관리자 권한
- `ROLE_USER`: 일반 사용자 권한

## 테스트

### 단위 테스트 실행

```bash
./gradlew test
```

### 통합 테스트 실행

```bash
./gradlew integrationTest
```

## 배포

### 1. 프로덕션 빌드

```bash
./gradlew build -Pprofile=prod
```

### 2. Docker 이미지 빌드

```bash
docker build -t car-center-api .
```

### 3. 환경변수 설정

프로덕션 환경에서는 다음 환경변수를 반드시 설정하세요:

```bash
export DB_HOST=your-db-host
export DB_PORT=5432
export DB_NAME=carcenter_prod
export DB_USERNAME=your-db-username
export DB_PASSWORD=your-db-password
export JWT_SECRET=your-very-secure-jwt-secret
```

## 문제 해결

### 자주 발생하는 문제

1. **데이터베이스 연결 오류**
   - PostgreSQL 서비스가 실행 중인지 확인
   - 데이터베이스 URL, 사용자명, 비밀번호 확인

2. **JWT 토큰 오류**
   - JWT_SECRET 환경변수 설정 확인
   - 토큰 만료 시간 확인

3. **MyBatis 매퍼 오류**
   - XML 파일 경로 확인
   - 네임스페이스와 인터페이스 패키지 일치 확인

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.
