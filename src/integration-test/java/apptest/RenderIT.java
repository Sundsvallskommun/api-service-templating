package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import org.docx4j.TextUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.templating.Application;
import se.sundsvall.templating.api.domain.RenderResponse;

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

	/**
	 * Only verifies the content of the rendered Word document, not the entire response structure.
	 * Also doesn't verify the exact formatting of the document, just that the expected text is present.
	 * @throws Exception if there is an error during the test execution
	 */
	@Test
	void test4_renderTemplateWordDocument() throws Exception {
		setupCall()
			.withServicePath(PATH_2281)
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(OK)
			.sendRequest();

		final var output = getResponseBody(RenderResponse.class).getOutput();
		assertThat(output).isNotBlank();

		// Decode and validate the DOCX content
		final var expectedContent = fromTestFile("expected-content.txt");
		final var docxBytes = Base64.getDecoder().decode(output);

		try (final var inputStream = new ByteArrayInputStream(docxBytes)) {
			final var wordMLPackage = WordprocessingMLPackage.load(inputStream);
			final var documentText = TextUtils.getText(wordMLPackage.getMainDocumentPart().getJaxbElement());

			assertThat(documentText).isEqualToNormalizingWhitespace(expectedContent);
		}
	}
}
