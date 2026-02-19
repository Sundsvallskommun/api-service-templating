package se.sundsvall.templating.domain;

import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;

class ContextMunicipalityIdTest {

	@Test
	void testBean() {
		assertThat(ContextMunicipalityId.class, hasValidGettersAndSetters());
	}
}
