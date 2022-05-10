package se.sundsvall.templating.service.pebble;

import se.sundsvall.templating.TemplateFlavor;

public class TemplateKey {

    private final String templateIdentifier;
    private final TemplateFlavor flavor;

    public TemplateKey(final String templateIdentifierAndFlavor) {
        var tokens = templateIdentifierAndFlavor.split(":");

        templateIdentifier = tokens[0];
        flavor = TemplateFlavor.valueOf(tokens[1]);
    }

    public TemplateKey(final String templateIdentifier, final TemplateFlavor flavor) {
        this.templateIdentifier = templateIdentifier;
        this.flavor = flavor;
    }

    public String getTemplateIdentifier() {
        return templateIdentifier;
    }

    public TemplateFlavor getFlavor() {
        return flavor;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", templateIdentifier, flavor);
    }
}
