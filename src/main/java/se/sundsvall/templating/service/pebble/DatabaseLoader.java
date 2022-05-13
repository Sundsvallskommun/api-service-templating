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
    public Reader getReader(final String templateIdentifier) {
        return dbIntegration.getTemplate(templateIdentifier)
            .map(template -> new BufferedReader(new StringReader(template.getContent())))
            .orElseThrow(() -> new LoaderException(null, "Unable to find template '" + templateIdentifier + "'"));
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
    public String createCacheKey(final String templateIdentifier) {
        return templateIdentifier;
    }

    @Override
    public boolean resourceExists(final String templateIdentifier) {
        return dbIntegration.getTemplate(templateIdentifier).isPresent();
    }
}
