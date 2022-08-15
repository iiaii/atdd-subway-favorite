package nextstep.subway.acceptance.favorite;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.acceptance.AcceptanceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static nextstep.subway.acceptance.favorite.FavoriteSteps.다른_사용자_토큰_발급_및_즐겨찾기_경로_삭제_요청;
import static nextstep.subway.acceptance.favorite.FavoriteSteps.다른_사용자_토큰_발급_및_즐겨찾기_경로_조회_요청;
import static nextstep.subway.acceptance.favorite.FavoriteSteps.즐겨찾기_경로_검증;
import static nextstep.subway.acceptance.favorite.FavoriteSteps.즐겨찾기_경로_추가_요청;
import static nextstep.subway.acceptance.favorite.FavoriteSteps.토큰_발급_및_즐겨찾기_경로_삭제_요청;
import static nextstep.subway.acceptance.favorite.FavoriteSteps.토큰_발급_및_즐겨찾기_경로_조회_요청;
import static nextstep.subway.acceptance.favorite.FavoriteSteps.토큰_발급_및_즐겨찾기_경로_추가_요청;
import static nextstep.subway.acceptance.favorite.FavoriteSteps.토큰_발급_없이_즐겨찾기_경로_삭제_요청;
import static nextstep.subway.acceptance.favorite.FavoriteSteps.토큰_발급_없이_즐겨찾기_경로_조회_요청;
import static nextstep.subway.acceptance.favorite.FavoriteSteps.토큰_발급_없이_즐겨찾기_경로_추가_요청;
import static nextstep.subway.acceptance.line.LineSteps.지하철_노선_생성_요청;
import static nextstep.subway.acceptance.member.MemberSteps.회원_생성_요청;
import static nextstep.subway.acceptance.station.StationSteps.지하철역_생성_요청;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("즐겨찾기 관련")
public class FavoriteAcceptanceTest extends AcceptanceTest {

    public static final String NEW_EMAIL = "new@nextstep.com";
    public static final String NEW_PASSWORD = "newPassword";
    public static final int AGE = 30;

    private Long 교대역;
    private Long 강남역;
    private Long 양재역;
    private Long 서울역;
    private Long 시청역;

    @BeforeEach
    public void setUp() {
        super.setUp();

        교대역 = 지하철역_생성_요청("교대역").jsonPath().getLong("id");
        강남역 = 지하철역_생성_요청("강남역").jsonPath().getLong("id");
        양재역 = 지하철역_생성_요청("양재역").jsonPath().getLong("id");
        서울역 = 지하철역_생성_요청("서울역").jsonPath().getLong("id");
        시청역 = 지하철역_생성_요청("시청역").jsonPath().getLong("id");

        지하철_노선_생성_요청(createLineCreateParams("2호선", "green", 교대역, 강남역, 10));
        지하철_노선_생성_요청(createLineCreateParams("1호선", "blue", 서울역, 시청역, 7));
    }

    /**
     * When 인증 후 출발역과 도착역을 입력하고 경로를 즐겨찾기에 추가하면
     * Then 즐겨찾기 경로가 추가된다.
     * When 추가된 즐겨찾기 경로를 조회하면
     * Then 추가된 즐겨찾기 경로가 조회된다.
     */
    @DisplayName("즐겨찾기 추가")
    @Test
    void addFavorite() {
        // when
        ExtractableResponse<Response> response = 토큰_발급_및_즐겨찾기_경로_추가_요청(교대역, 강남역);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotEmpty();

        // when
        ExtractableResponse<Response> findResponse = 토큰_발급_및_즐겨찾기_경로_조회_요청(response);

        // then
        즐겨찾기_경로_검증(findResponse, "교대역", "강남역");
    }

    /**
     * When 인증 없이 출발역과 도착역을 입력하고 경로를 즐겨찾기에 추가하면
     * Then 예외가 발생한다.
     */
    @DisplayName("인증 없이 즐겨찾기 추가하면 예외")
    @Test
    void addFavoriteWithoutToken() {

        // when
        ExtractableResponse<Response> response = 토큰_발급_없이_즐겨찾기_경로_추가_요청(교대역, 강남역);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * Given 존재하는 역을 출발역으로, 존재하지 않는 역을 도착역으로 입력하고
     * When 인증 후 경로를 즐겨찾기에 추가하면
     * Then 예외가 발생한다.
     */
    @DisplayName("존재하지 않는 역 즐겨찾기 추가하면 예외")
    @Test
    void addFavoriteWithUnknownStation() {
        // given
        Long 존재하지_않는_역 = 1234L;

        // when
        ExtractableResponse<Response> response = 토큰_발급_및_즐겨찾기_경로_추가_요청(교대역, 존재하지_않는_역);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * When 인증 후 이어지지 않은 출발역과 도착역을 즐겨찾기에 추가하면
     * Then 예외가 발생한다.
     */
    @DisplayName("이어지지 않은 경로 즐겨찾기 추가하면 예외")
    @Test
    void addFavoriteNonConnectStations() {
        // when
        ExtractableResponse<Response> response = 토큰_발급_및_즐겨찾기_경로_추가_요청(교대역, 서울역);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given 즐겨찾기 경로를 추가하고
     * When 인증 후 추가된 즐겨찾기 경로를 조회하면
     * Then 새로 추가된 즐겨찾기 경로가 조회된다.
     */
    @DisplayName("즐겨찾기 조회")
    @Test
    void findFavorite() {
        // given
        ExtractableResponse<Response> createResponse = 즐겨찾기_경로_추가_요청(교대역, 강남역);

        // when
        ExtractableResponse<Response> response = 토큰_발급_및_즐겨찾기_경로_조회_요청(createResponse);

        // then
        즐겨찾기_경로_검증(response, "교대역", "강남역");
    }

    /**
     * Given 즐겨찾기 경로를 추가하고
     * When 인증 없이 추가된 즐겨찾기 경로를 조회하면
     * Then 예외가 발생한다.
     */
    @DisplayName("인증 없이 즐겨찾기 조회하면 예외")
    @Test
    void findFavoriteWithoutToken() {
        // given
        ExtractableResponse<Response> createResponse = 즐겨찾기_경로_추가_요청(교대역, 강남역);

        // when
        ExtractableResponse<Response> response = 토큰_발급_없이_즐겨찾기_경로_조회_요청(createResponse);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * Given 즐겨찾기 경로를 추가하고
     * Given 다른 사용자를 추가하고
     * When 인증 후 추가된 즐겨찾기 경로를 조회하면
     * Then 예외가 발생한다.
     */
    @DisplayName("다른 사용자가 즐겨찾기 조회하면 예외")
    @Test
    void findFavoriteOtherUser() {
        // given
        ExtractableResponse<Response> createResponse = 즐겨찾기_경로_추가_요청(교대역, 강남역);

        // Given
        회원_생성_요청(NEW_EMAIL, NEW_PASSWORD, AGE);

        // when
        ExtractableResponse<Response> response = 다른_사용자_토큰_발급_및_즐겨찾기_경로_조회_요청(createResponse);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    /**
     * Given 즐겨찾기 경로를 추가하고
     * When 인증 후 추가한 즐겨찾기 경로를 삭제하면
     * Then 추가된 즐겨찾기 경로가 삭제된다.
     * When 즐겨찾기 경로를 조회하면
     * Then 예외가 발생한다.
     */
    @DisplayName("즐겨찾기 삭제")
    @Test
    void deleteFavorite() {
        // given
        ExtractableResponse<Response> createResponse = 즐겨찾기_경로_추가_요청(교대역, 강남역);

        // when
        ExtractableResponse<Response> deleteResponse = 토큰_발급_및_즐겨찾기_경로_삭제_요청(createResponse);

        // then
        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        // when
        ExtractableResponse<Response> response = 토큰_발급_및_즐겨찾기_경로_조회_요청(createResponse);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given 즐겨찾기 경로를 추가하고
     * When 인증 없이 추가한 즐겨찾기 경로를 삭제하면
     * Then 예외가 발생한다.
     */
    @DisplayName("인증 없이 즐겨찾기 삭제하면 예외")
    @Test
    void deleteFavoriteWithoutToken() {
        // given
        ExtractableResponse<Response> createResponse = 즐겨찾기_경로_추가_요청(교대역, 강남역);

        // when
        ExtractableResponse<Response> deleteResponse = 토큰_발급_없이_즐겨찾기_경로_삭제_요청(createResponse);

        // then
        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * Given 즐겨찾기 경로를 추가하고
     * Given 다른 사용자를 추가하고
     * When 다른 사용자로 인증 후 추가한 즐겨찾기 경로를 삭제하면
     * Then 예외가 발생한다.
     */
    @DisplayName("다른 사용자가 즐겨찾기 삭제하면 예외")
    @Test
    void deleteFavoriteOtherUser() {
        // given
        ExtractableResponse<Response> createResponse = 즐겨찾기_경로_추가_요청(교대역, 강남역);

        // Given
        회원_생성_요청(NEW_EMAIL, NEW_PASSWORD, AGE);

        // when
        ExtractableResponse<Response> deleteResponse = 다른_사용자_토큰_발급_및_즐겨찾기_경로_삭제_요청(createResponse);

        // then
        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    private Map<String, String> createLineCreateParams(String name, String color, Long upStationId, Long downStationId, int distance) {
        Map<String, String> lineCreateParams;
        lineCreateParams = new HashMap<>();
        lineCreateParams.put("name", "신분당선");
        lineCreateParams.put("color", "bg-red-600");
        lineCreateParams.put("upStationId", upStationId + "");
        lineCreateParams.put("downStationId", downStationId + "");
        lineCreateParams.put("distance", 10 + "");
        return lineCreateParams;
    }

}