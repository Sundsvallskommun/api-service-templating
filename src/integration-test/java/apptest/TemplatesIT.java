package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.templating.Application;
import se.sundsvall.templating.integration.db.TemplateRepository;
import se.sundsvall.templating.integration.db.entity.Version;

@WireMockAppTestSuite(
	files = "classpath:/TemplatesIT/",
	classes = Application.class
)
@Sql({ "/db/truncate.sql", "/db/data.sql" })
class TemplatesIT extends AbstractAppTest {

	private static final String PATH_2281 = "/2281/templates";
	private static final String PATH_2282 = "/2282/templates";
	private static final String REQUEST = "request.json";
	private static final String RESPONSE = "expected-response.json";

	@Autowired
	private TemplateRepository templateRepository;

	@Test
	@Sql("/db/truncate.sql")
	void test1_getAllTemplatesWhenNoTemplatesExist() {
		setupCall()
			.withServicePath(PATH_2281)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_getAllTemplatesWithoutMetadataFilters() {
		setupCall()
			.withServicePath(PATH_2281)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_getAllTemplatesWithMetadataFilters() {
		var path = fromPath(PATH_2281)
			.queryParam("verksamhet", "SBK")
			.queryParam("process", "PRH")
			.build()
			.toString();

		setupCall()
			.withServicePath(path)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_searchTemplatesWithNoInput() {
		setupCall()
			.withServicePath(PATH_2281 + "/search")
			.withHttpMethod(POST)
			.withRequest("{}")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_searchTemplates() {
		setupCall()
			.withServicePath(PATH_2281 + "/search")
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_getTemplateWhenTemplateDoesNotExist() {
		setupCall()
			.withServicePath(PATH_2281 + "/some.nonexistent.identifier")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test7_getTemplateWithRequestedVersion() {
		setupCall()
			.withServicePath(PATH_2281 + "/third.template/1.0")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test8_getTemplateWithoutRequestedVersionReturnsLatestVersion() {
		setupCall()
			.withServicePath(PATH_2281 + "/third.template")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test9_getAllTemplatesWithoutMetadataFilters() {
		setupCall()
			.withServicePath(PATH_2282)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_deleteTemplateVersion() {
		assertThat(templateRepository.findByIdentifierAndVersionAndMunicipalityId("second.template", new Version(1, 0), "2281")).isPresent();

		setupCall()
			.withServicePath(PATH_2281 + "/second.template/1.0")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(templateRepository.findByIdentifierAndVersionAndMunicipalityId("second.template", new Version(1, 0), "2281")).isEmpty();
	}

	@Test
	void test11_deleteTemplate() {
		assertThat(templateRepository.findByIdentifierAndMunicipalityId("second.template", "2281")).isNotEmpty();

		setupCall()
			.withServicePath(PATH_2281 + "/second.template")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(templateRepository.findByIdentifierAndMunicipalityId("second.template", "2281")).isEmpty();
	}
}
