package se.sundsvall.templating.integration.db.spec;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpecificationsTests {

	@Test
	void test_hasMetadata_generatesSpecificationOk() {
		assertThat(Specifications.hasMetadata("someKey", "someValue")).isNotNull();
	}

	@Test
	void test_isLatest_generatesSpecificationOk() {
		assertThat(Specifications.isLatest()).isNotNull();
	}
}
