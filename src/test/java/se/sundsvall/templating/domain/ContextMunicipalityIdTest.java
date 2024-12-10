package se.sundsvall.templating.domain;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class ContextMunicipalityIdTest {

	@Test
	void testBean() {
		assertThat(ContextMunicipalityId.class, hasValidGettersAndSetters());
	}
}
