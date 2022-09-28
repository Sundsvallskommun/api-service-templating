package se.sundsvall.templating.integration.db.spec;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SpecificationsTests {

    @Test
    void test_hasMetadata_generatesSpecificationOk() {
        assertThat(Specifications.hasMetadata("someKey", "someValue")).isNotNull();
    }
}
