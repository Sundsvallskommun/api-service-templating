package se.sundsvall.templating.service.pebble.loader;

import java.io.Reader;

import se.sundsvall.templating.domain.ContextMunicipalityId;
import se.sundsvall.templating.service.pebble.IdentifierAndVersion;

import io.pebbletemplates.pebble.loader.Loader;
import io.pebbletemplates.pebble.loader.StringLoader;

public class DelegatingLoader implements Loader<IdentifierAndVersion> {

    public static final String DIRECT_PREFIX = "DIRECT:";

    private final DatabaseLoader databaseLoader;
    private final StringLoader stringLoader;
    private final ContextMunicipalityId requestScopedMunicipalityId;

    public DelegatingLoader(final DatabaseLoader databaseLoader,
            final StringLoader stringLoader, ContextMunicipalityId requestScopedMunicipalityId) {
        this.databaseLoader = databaseLoader;
        this.stringLoader = stringLoader;
        this.requestScopedMunicipalityId = requestScopedMunicipalityId;
    }

    @Override
    public Reader getReader(final IdentifierAndVersion identifier) {
        if (identifier.getIdentifier().startsWith(DIRECT_PREFIX)) {
            return stringLoader.getReader(identifier.getIdentifier());
        }

        return databaseLoader.getReader(identifier);
    }

    @Override
    public void setCharset(final String charset) {
        // Intentionally empty
    }

    @Override
    public void setPrefix(final String prefix) {
        // Intentionally empty
    }

    @Override
    public void setSuffix(final String suffix) {
        // Intentionally empty
    }

    @Override
    public String resolveRelativePath(final String relativePath, final String anchorPath) {
        if (!anchorPath.startsWith(DIRECT_PREFIX)) {
            return databaseLoader.resolveRelativePath(relativePath, anchorPath);
        }

        return stringLoader.resolveRelativePath(relativePath, anchorPath);
    }

    @Override
    public IdentifierAndVersion createCacheKey(final String identifier) {
        return new IdentifierAndVersion(requestScopedMunicipalityId.getValue(), identifier);
    }

    @Override
    public boolean resourceExists(final String identifier) {
        if (identifier.startsWith(DIRECT_PREFIX)) {
            return stringLoader.resourceExists(identifier);
        }

        return databaseLoader.resourceExists(identifier);
    }
}
