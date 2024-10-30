package se.sundsvall.templating.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import se.sundsvall.templating.api.domain.DetailedTemplateResponse;
import se.sundsvall.templating.api.domain.TemplateRequest;
import se.sundsvall.templating.api.domain.TemplateResponse;
import se.sundsvall.templating.api.domain.filter.expression.EmptyExpression;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.service.TemplateService;

@ExtendWith(MockitoExtension.class)
class TemplateResourceTests {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String IDENTIFIER = "identifier";

	@Mock
	private TemplateService mockTemplatingService;
	@Mock
	private TemplateResponse mockTemplateResponse;

	private TemplateResource resource;

	@BeforeEach
	void setUp() {
		resource = new TemplateResource(mockTemplatingService);
	}

	@Test
	void test_getAll() {
		when(mockTemplatingService.getTemplates(any(), ArgumentMatchers.<Specification<TemplateEntity>>any()))
			.thenReturn(List.of(mockTemplateResponse));

		var result = resource.searchTemplates(MUNICIPALITY_ID, new EmptyExpression());
		assertThat(result).hasSize(1);

		verify(mockTemplatingService, times(1)).getTemplates(eq(MUNICIPALITY_ID), ArgumentMatchers.<Specification<TemplateEntity>>any());
	}

	@Test
	void test_getTemplate() {
		when(mockTemplatingService.getTemplate(any(), any(), nullable(String.class)))
			.thenReturn(Optional.of(DetailedTemplateResponse.builder().build()));

		var result = resource.getTemplate(MUNICIPALITY_ID, IDENTIFIER);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();

		verify(mockTemplatingService, times(1)).getTemplate(MUNICIPALITY_ID, IDENTIFIER, null);
	}

	@Test
	void test_getTemplate_whenTemplateDoesNotExist() {
		when(mockTemplatingService.getTemplate(any(), any(), nullable(String.class)))
			.thenReturn(Optional.empty());

		var result = resource.getTemplate(MUNICIPALITY_ID, IDENTIFIER);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(result.getBody()).isNull();

		verify(mockTemplatingService, times(1)).getTemplate(MUNICIPALITY_ID, IDENTIFIER, null);
	}

	@Test
	void test_saveTemplate() {
		when(mockTemplatingService.saveTemplate(any(), any()))
			.thenReturn(mockTemplateResponse);
		when(mockTemplateResponse.getIdentifier()).thenReturn("someId");
		var request = new TemplateRequest();
		var result = resource.saveTemplate(MUNICIPALITY_ID, request);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(result.getBody()).isNotNull();

		verify(mockTemplatingService, times(1)).saveTemplate(eq(MUNICIPALITY_ID), same(request));
	}

	@Test
	void test_updateTemplate() {
		when(mockTemplatingService.updateTemplate(any(), any(), nullable(String.class), any()))
			.thenReturn(mockTemplateResponse);

		var result = resource.updateTemplate(MUNICIPALITY_ID, IDENTIFIER, null, null);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();

		verify(mockTemplatingService, times(1)).updateTemplate(MUNICIPALITY_ID, IDENTIFIER, null, null);
	}

	@Test
	void test_deleteTemplate() {
		resource.deleteTemplate(MUNICIPALITY_ID, IDENTIFIER);

		verify(mockTemplatingService, times(1)).deleteTemplate(MUNICIPALITY_ID, IDENTIFIER, null);
	}
}
