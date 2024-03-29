package se.sundsvall.templating.service.pebble.loader;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;

import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.service.pebble.IdentifierAndVersion;
import se.sundsvall.templating.util.BASE64;

import io.pebbletemplates.pebble.error.LoaderException;
import io.pebbletemplates.pebble.loader.Loader;

public class DatabaseLoader implements Loader<IdentifierAndVersion> {

    private final DbIntegration dbIntegration;

    public DatabaseLoader(final DbIntegration dbIntegration) {
        this.dbIntegration = dbIntegration;
    }

    @Override
    public Reader getReader(final IdentifierAndVersion identifierAndVersion) {
        return dbIntegration.getTemplate(identifierAndVersion.getIdentifier(), identifierAndVersion.getVersion())
            .map(template -> new BufferedReader(new StringReader(BASE64.decode(template.getContent()))))
            .orElseThrow(() -> new LoaderException(null, "Unable to find template '" + identifierAndVersion + "'"));
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
        return relativePath;
    }

    @Override
    public IdentifierAndVersion createCacheKey(final String identifierAndVersion) {
        return new IdentifierAndVersion(identifierAndVersion);
    }

    @Override
    public boolean resourceExists(final String identifierAndVersion) {
        var parsedIdentifierAndVersion = new IdentifierAndVersion(identifierAndVersion);

        return dbIntegration.getTemplate(parsedIdentifierAndVersion.getIdentifier(), parsedIdentifierAndVersion.getVersion()).isPresent();
    }
}
