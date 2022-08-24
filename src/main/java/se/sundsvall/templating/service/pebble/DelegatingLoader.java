package se.sundsvall.templating.service.pebble;

import java.io.Reader;

import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.loader.StringLoader;

public class DelegatingLoader implements Loader<String> {

    public static final String DIRECT_PREFIX = "DIRECT:";

    private final DatabaseLoader databaseLoader;
    private final StringLoader stringLoader;

    public DelegatingLoader(final DatabaseLoader databaseLoader,
            final StringLoader stringLoader) {
        this.databaseLoader = databaseLoader;
        this.stringLoader = stringLoader;
    }

    @Override
    public Reader getReader(final String identifier) {
        if (identifier.startsWith(DIRECT_PREFIX)) {
            return stringLoader.getReader(identifier);
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
    public String createCacheKey(final String identifier) {
        return identifier;
    }

    @Override
    public boolean resourceExists(final String identifier) {
        if (identifier.startsWith(DIRECT_PREFIX)) {
            return stringLoader.resourceExists(identifier);
        }

        return databaseLoader.resourceExists(identifier);
    }
}