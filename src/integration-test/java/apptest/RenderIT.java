package apptest;

import configuration.TestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.templating.Application;

@Import(TestContainersConfiguration.class)
@WireMockAppTestSuite(
	files = "classpath:/RenderIT/",
	classes = Application.class
)
@Sql({"/db/truncate.sql", "/db/data.sql"})
class RenderIT extends AbstractAppTest {

	private static final String PATH_2281 = "/2281/render";
	private static final String PATH_2282 = "/2282/render";

	@Test
	void test1_renderTemplate() {
		setupCall()
			.withServicePath(PATH_2281)
			.withHttpMethod(HttpMethod.POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_renderTemplateDirect() {
		setupCall()
			.withServicePath(PATH_2281 + "/direct")
			.withHttpMethod(HttpMethod.POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_renderTemplateOn2282() {
		setupCall()
			.withServicePath(PATH_2282)
			.withHttpMethod(HttpMethod.POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse();
	}
}
