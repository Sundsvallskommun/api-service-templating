package se.sundsvall.templating.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Configuration
class OpenPdfConfiguration {

    @Bean
    ITextRenderer iTextRenderer() {
        var renderer = new ITextRenderer();
        var sharedContext = renderer.getSharedContext();
        sharedContext.setPrint(true);
        sharedContext.setInteractive(false);
        return renderer;
    }
}
