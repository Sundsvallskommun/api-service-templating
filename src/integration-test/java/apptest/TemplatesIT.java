package apptest;

import static org.springframework.web.util.UriComponentsBuilder.fromPath;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.templating.Application;

@WireMockAppTestSuite(
    files = "classpath:/TemplatesIT/",
    classes = Application.class
)
@Sql({"/db/truncate.sql", "/db/data.sql"})
class TemplatesIT extends AbstractAppTest {

    private static final String PATH = "/templates";

    @Test
    @Sql("/db/truncate.sql")
    void test1_getAllTemplatesWhenNoTemplatesExist() {
        setupCall()
            .withServicePath(PATH)
            .withHttpMethod(HttpMethod.GET)
            .withExpectedResponseStatus(HttpStatus.OK)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();
    }

    @Test
    void test2_getAllTemplatesWithoutMetadataFilters() {
        setupCall()
            .withServicePath(PATH)
            .withHttpMethod(HttpMethod.GET)
            .withExpectedResponseStatus(HttpStatus.OK)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();
    }

    @Test
    void test3_getAllTemplatesWithMetadataFilters() {
        var path = fromPath(PATH)
            .queryParam("verksamhet", "SBK")
            .queryParam("process", "PRH")
            .build()
            .toString();

        setupCall()
            .withServicePath(path)
            .withHttpMethod(HttpMethod.GET)
            .withExpectedResponseStatus(HttpStatus.OK)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();
    }

    @Test
    void test4_searchTemplatesWithNoInput() {
        setupCall()
            .withServicePath(PATH + "/search")
            .withHttpMethod(HttpMethod.POST)
            .withRequest("{}")
            .withExpectedResponseStatus(HttpStatus.OK)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();
    }

    @Test
    void test5_searchTemplates() {
        setupCall()
            .withServicePath(PATH + "/search")
            .withHttpMethod(HttpMethod.POST)
            .withRequest("request.json")
            .withExpectedResponseStatus(HttpStatus.OK)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();
    }

    @Test
    void test6_getTemplateWhenTemplateDoesNotExist() {
        setupCall()
            .withServicePath(PATH + "/some.nonexistent.identifier")
            .withHttpMethod(HttpMethod.GET)
            .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
            .sendRequestAndVerifyResponse();
    }

    @Test
    void test7_getTemplateWithRequestedVersion() {
        setupCall()
            .withServicePath(PATH + "/third.template/1.0")
            .withHttpMethod(HttpMethod.GET)
            .withExpectedResponseStatus(HttpStatus.OK)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();
    }

    @Test
    void test8_getTemplateWithoutRequestedVersionReturnsLatestVersion() {
        setupCall()
            .withServicePath(PATH + "/third.template")
            .withHttpMethod(HttpMethod.GET)
            .withExpectedResponseStatus(HttpStatus.OK)
            .withExpectedResponse("expected-response.json")
            .sendRequestAndVerifyResponse();
    }

}
