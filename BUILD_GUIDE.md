# 빌드 및 설치 가이드

## 📦 빌드 방법

### 1. Maven 설치 확인
```bash
mvn -version
```

Maven이 설치되어 있지 않다면:
- **Windows**: [Maven 다운로드](https://maven.apache.org/download.cgi)에서 다운로드 후 환경변수 설정
- **Linux/Mac**: `sudo apt install maven` 또는 `brew install maven`

### 2. 프로젝트 빌드
```bash
cd c:\Users\user\Desktop\project\Battleroyale-2
mvn clean package
```

### 3. 빌드 결과
빌드가 성공하면 `target/BattleRoyale2-2.0.0.jar` 파일이 생성됩니다.

## 🚀 서버 설치 및 실행

### 1. Paper 서버 준비
1. [PaperMC](https://papermc.io/downloads/paper)에서 1.20.1 버전 다운로드
2. 서버 폴더 생성 및 Paper JAR 파일 복사
3. 서버 실행 후 `eula.txt`에서 `eula=true` 설정

### 2. TACZ 모드 설치 (선택사항)
- Magma 등 하이브리드 서버 사용 시 TACZ 모드 설치
- Forge 모드 폴더에 TACZ JAR 파일 복사

### 3. 플러그인 설치
```bash
# 빌드된 JAR 파일을 서버의 plugins 폴더로 복사
copy target\BattleRoyale2-2.0.0.jar <서버폴더>\plugins\
```

### 4. 월드 생성기 설정
`bukkit.yml` 파일 수정:
```yaml
worlds:
  world:
    generator: BattleRoyale2
```

또는 새 월드 생성 시:
```bash
# server.properties
level-type=BattleRoyale2
```

### 5. 서버 시작
```bash
java -Xmx4G -Xms4G -jar paper-1.20.1.jar nogui
```

## 🎮 게임 시작

1. 서버 접속
2. OP 권한 획득: `/op <플레이어명>`
3. 게임 시작: `/br start <팀원 수>`
   - 예: `/br start 2` (2:2 팀전)
   - 예: `/br start 1` (개인전)

## 🔧 문제 해결

### Maven 빌드 오류
```bash
# 의존성 강제 업데이트
mvn clean install -U

# 오프라인 모드로 빌드
mvn clean package -o
```

### 플러그인 로드 오류
1. Paper 1.20.1 버전 확인
2. Java 17 이상 사용 확인
3. 서버 로그 확인: `logs/latest.log`

### TACZ 통합 이슈
현재 코드는 TACZ의 기본 구조만 제공합니다. 실제 TACZ API 통합이 필요합니다:
- `SupplyLootGenerator.java`의 `createTaczGun()` 메서드 수정
- TACZ API 문서 참조하여 NBT 데이터 설정

## 📝 개발 환경 설정

### IntelliJ IDEA
1. `File > Open` → `pom.xml` 선택
2. Maven 프로젝트로 import
3. JDK 17 설정
4. Run Configuration 추가 (Paper 서버 실행)

### Eclipse
1. `File > Import > Maven > Existing Maven Projects`
2. 프로젝트 폴더 선택
3. JDK 17 설정

### VS Code
1. Java Extension Pack 설치
2. 폴더 열기
3. Maven for Java 확장 사용

## 🧪 테스트

### 로컬 테스트 서버 설정
```bash
# 테스트 서버 폴더 생성
mkdir test-server
cd test-server

# Paper 다운로드 및 실행
# plugins 폴더에 빌드된 JAR 복사
# 서버 시작 후 테스트
```

### 테스트 체크리스트
- [ ] 게임 시작 (`/br start 2`)
- [ ] 포인트 시스템 작동
- [ ] 현상금 시스템 (5분 후)
- [ ] 보급품 투하 (시작 직후, 5분, 10분)
- [ ] 자기장 축소 (5분마다)
- [ ] 데스타임 (모두 2번 사망 후)
- [ ] 스코어보드 표시
- [ ] 건축 제한 (땅파기, 타워링 금지)

## 📚 추가 리소스

- [Paper API 문서](https://docs.papermc.io/)
- [Bukkit API 문서](https://hub.spigotmc.org/javadocs/bukkit/)
- [Maven 가이드](https://maven.apache.org/guides/)

---

문제가 발생하면 GitHub Issues에 등록하거나 서버 로그를 첨부하여 문의하세요.
