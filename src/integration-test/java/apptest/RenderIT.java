package apptest;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.templating.Application;

@WireMockAppTestSuite(
	files = "classpath:/RenderIT/",
	classes = Application.class
)
@Sql({ "/db/truncate.sql", "/db/data.sql" })
class RenderIT extends AbstractAppTest {

	private static final String PATH_2281 = "/2281/render";
	private static final String PATH_2282 = "/2282/render";
	private static final String REQUEST = "request.json";
	private static final String RESPONSE = "expected-response.json";

	@Test
	void test1_renderTemplate() {
		setupCall()
			.withServicePath(PATH_2281)
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_renderTemplateDirect() {
		setupCall()
			.withServicePath(PATH_2281 + "/direct")
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_renderTemplateOn2282() {
		setupCall()
			.withServicePath(PATH_2282)
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}
}
