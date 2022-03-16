package se.sundsvall.templating.service.pebble;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.templating.TemplateFlavor;

class TemplateKeyTests {

    @Test
    void test_constructorAcceptingTemplateIdAndFlavorAsParseableString() {
        var templateKey = new TemplateKey("someTemplateId:HTML");

        assertThat(templateKey.getTemplateId()).isEqualTo("someTemplateId");
        assertThat(templateKey.getFlavor()).isEqualTo(TemplateFlavor.HTML);
    }

    @Test
    void test_constructorAcceptingTemplateIdAndFlavorAsSeparateArgs() {
        var templateKey = new TemplateKey("someTemplateId", TemplateFlavor.TEXT);

        assertThat(templateKey.getTemplateId()).isEqualTo("someTemplateId");
        assertThat(templateKey.getFlavor()).isEqualTo(TemplateFlavor.TEXT);
    }
}
