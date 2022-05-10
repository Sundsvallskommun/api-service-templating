package se.sundsvall.templating.service.pebble;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;

import com.mitchellbosecke.pebble.error.LoaderException;
import com.mitchellbosecke.pebble.loader.Loader;

import se.sundsvall.templating.integration.db.DbIntegration;

public class DatabaseLoader implements Loader<String> {

    private final DbIntegration dbIntegration;

    public DatabaseLoader(final DbIntegration dbIntegration) {
        this.dbIntegration = dbIntegration;
    }

    @Override
    public Reader getReader(final String templateIdAndFlavor) {
        var key = new TemplateKey(templateIdAndFlavor);

        return dbIntegration.getTemplateByIdentifier(key.getTemplateIdentifier())
            .map(template -> {
                if (!template.getVariants().containsKey(key.getFlavor())) {
                    throw new LoaderException(null, "Unable to find template variant '" + key.getFlavor() + "' for template '" + key.getTemplateIdentifier() + "'");
                }

                return new BufferedReader(new StringReader(template.getVariants().get(key.getFlavor())));
            })
            .orElseThrow(() -> new LoaderException(null, "Unable to find template '" + key.getTemplateIdentifier() + "'"));
    }

    @Override
    public void setCharset(final String s) {
        // Intentionally unused
    }

    @Override
    public void setPrefix(final String s) {
        // Intentionally unused
    }

    @Override
    public void setSuffix(final String s) {
        // Intentionally unused
    }

    @Override
    public String resolveRelativePath(final String relativePath, final String anchorPath) {
        // Calculate
        if (relativePath.indexOf(':') == -1) {
            return relativePath + ":" + anchorPath.split(":")[1];
        }

        return relativePath;
    }

    @Override
    public String createCacheKey(final String templateId) {
        return templateId;
    }

    @Override
    public boolean resourceExists(final String templateId) {
        return dbIntegration.templateExists(templateId);
    }
}
