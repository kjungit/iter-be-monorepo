package com.example.iter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

// 도메인 경계를 지키는 회귀 방지 테스트.
//
// 마일스톤 2에서 도메인 간 직접 참조를 99건에서 2건으로 줄였다.
// 그런데 이 경계를 지켜주는 장치가 코드에는 없었다 — 남의 Repository 를 주입해도
// 컴파일이 되고 테스트도 통과한다. 다음 사람이 무심코 되돌리기 쉽다.
//
// 여기서는 소스의 import 문과 @Query 문자열을 직접 읽어 검사한다.
// 라이브러리를 추가하지 않는 대신 검사 범위가 좁다 — 리플렉션이나 문자열로 만든
// 클래스 이름은 못 잡는다. 그래도 "남의 Repository 를 주입했다" 같은 흔한 되돌림은 잡힌다.
//
// 규칙:
//  - 다른 도메인의 것을 쓰려면 그 도메인이 공개한 <domain>.api 패키지를 거친다
//  - common / config / admin 은 도메인이 아니다
//    (admin 은 여러 도메인을 조합해 보여주는 화면이라 조인·참조가 정상이다)
class DomainBoundaryTest {

    private static final Path SOURCE_ROOT =
            Path.of("src/main/java/com/example/iter");

    // 도메인이 아닌 패키지. 근거는 클래스 주석 참고.
    private static final Set<String> NOT_A_DOMAIN = Set.of("common", "config", "admin");

    private static final Pattern IMPORT =
            Pattern.compile("^\\s*import\\s+(?:static\\s+)?com\\.example\\.iter\\.([a-z]+)\\.([\\w.]+);",
                    Pattern.MULTILINE);

    @Test
    @DisplayName("도메인은 다른 도메인의 Repository 를 직접 참조하지 않는다")
    void 리포지토리_크로스_참조가_없다() {
        assertThat(crossDomainReferences("domain.repository")).isEmpty();
    }

    @Test
    @DisplayName("도메인은 다른 도메인의 엔티티·열거형을 직접 참조하지 않는다")
    void 엔티티와_열거형_크로스_참조가_없다() {
        // 공개해야 하는 열거형은 <domain>.api 로 옮겼다 (RentalStatus, PaymentStatus, PreferredLanguage).
        // 나머지는 의도 기반 포트 메서드 뒤로 숨어 사라졌다.
        assertThat(crossDomainReferences("domain.entity")).isEmpty();
    }

    @Test
    @DisplayName("JPQL 이 다른 도메인의 엔티티를 새로 조인하지 않는다")
    void 크로스_도메인_JPQL_이_늘지_않는다() {
        // 3차 이월 대상 12건. 줄이는 것은 환영이고 늘어나면 실패한다.
        assertThat(crossDomainJpqlQueries())
                .as("도메인 안의 크로스 조인이 늘었다. 새 쿼리는 <domain>.api 포트로 대체하거나 admin 으로 옮길 것")
                .hasSizeLessThanOrEqualTo(12);
    }

    // ---------------------------------------------------------------

    // "<소비자 파일> -> <제공자 도메인>.<경로>" 형태로 위반을 모은다.
    private List<String> crossDomainReferences(String forbiddenSubPackage) {
        List<String> violations = new ArrayList<>();

        forEachSource((relativePath, source) -> {
            String domain = relativePath.split("/")[0];
            if (NOT_A_DOMAIN.contains(domain)) {
                return;
            }

            Matcher matcher = IMPORT.matcher(source);
            while (matcher.find()) {
                String provider = matcher.group(1);
                String rest = matcher.group(2);

                if (provider.equals(domain) || NOT_A_DOMAIN.contains(provider)) {
                    continue;
                }
                // 제공자가 공개한 창구는 허용이다.
                if (rest.startsWith("api.")) {
                    continue;
                }
                if (rest.startsWith(forbiddenSubPackage)) {
                    violations.add(relativePath + " -> " + provider + "." + rest);
                }
            }
        });

        return violations;
    }

    private List<String> crossDomainJpqlQueries() {
        Map<String, String> entityOwners = entityOwnersByName();
        List<String> queries = new ArrayList<>();

        forEachSource((relativePath, source) -> {
            String domain = relativePath.split("/")[0];
            if (NOT_A_DOMAIN.contains(domain)) {
                return;
            }

            for (String block : textBlocks(source)) {
                if (!block.toLowerCase().contains("from ")) {
                    continue;
                }
                Set<String> foreign = new LinkedHashSet<>();
                entityOwners.forEach((entity, owner) -> {
                    if (!owner.equals(domain)
                            && Pattern.compile("\\b" + entity + "\\b").matcher(block).find()) {
                        foreign.add(entity);
                    }
                });
                if (!foreign.isEmpty()) {
                    queries.add(relativePath + " -> " + foreign);
                }
            }
        });

        return queries;
    }

    private Map<String, String> entityOwnersByName() {
        Map<String, String> owners = new LinkedHashMap<>();
        forEachSource((relativePath, source) -> {
            if (source.contains("@Entity")) {
                String fileName = relativePath.substring(relativePath.lastIndexOf('/') + 1);
                owners.put(fileName.replace(".java", ""), relativePath.split("/")[0]);
            }
        });
        return owners;
    }

    private List<String> textBlocks(String source) {
        List<String> blocks = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"\"\"(.*?)\"\"\"", Pattern.DOTALL).matcher(source);
        while (matcher.find()) {
            blocks.add(matcher.group(1));
        }
        return blocks;
    }

    private void forEachSource(SourceVisitor visitor) {
        try (Stream<Path> paths = Files.walk(SOURCE_ROOT)) {
            paths.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    visitor.visit(
                            SOURCE_ROOT.relativize(path).toString().replace('\\', '/'),
                            Files.readString(path));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @FunctionalInterface
    private interface SourceVisitor {
        void visit(String relativePath, String source);
    }
}
