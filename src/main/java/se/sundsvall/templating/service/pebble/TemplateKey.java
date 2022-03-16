package se.sundsvall.templating.service.pebble;

import se.sundsvall.templating.TemplateFlavor;

public class TemplateKey {

    private final String templateId;
    private final TemplateFlavor flavor;

    public TemplateKey(final String templateIdAndFlavor) {
        var tokens = templateIdAndFlavor.split(":");

        templateId = tokens[0];
        flavor = TemplateFlavor.valueOf(tokens[1]);
    }

    public TemplateKey(final String templateId, final TemplateFlavor flavor) {
        this.templateId = templateId;
        this.flavor = flavor;
    }

    public String getTemplateId() {
        return templateId;
    }

    public TemplateFlavor getFlavor() {
        return flavor;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", templateId, flavor);
    }
}
