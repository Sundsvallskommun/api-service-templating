package se.sundsvall.templating.service.pebble.loader;

import static se.sundsvall.templating.util.TemplateUtil.bytesToString;
import static se.sundsvall.templating.util.TemplateUtil.decodeBase64;

import io.pebbletemplates.pebble.error.LoaderException;
import io.pebbletemplates.pebble.loader.Loader;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import se.sundsvall.templating.domain.ContextMunicipalityId;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.service.pebble.IdentifierAndVersion;

public class DatabaseLoader implements Loader<IdentifierAndVersion> {

	private final DbIntegration dbIntegration;
	private final ContextMunicipalityId requestScopedMunicipalityId;

	public DatabaseLoader(final DbIntegration dbIntegration, ContextMunicipalityId requestScopedMunicipalityId) {
		this.dbIntegration = dbIntegration;
		this.requestScopedMunicipalityId = requestScopedMunicipalityId;
	}

	@Override
	public Reader getReader(final IdentifierAndVersion identifierAndVersion) {
		return dbIntegration.getTemplate(identifierAndVersion.getMunicipalityId(), identifierAndVersion.getIdentifier(), identifierAndVersion.getVersion())
			.map(template -> new BufferedReader(new StringReader(bytesToString(decodeBase64(template.getContent())))))
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
		return new IdentifierAndVersion(requestScopedMunicipalityId.getValue(), identifierAndVersion);
	}

	@Override
	public boolean resourceExists(final String identifierAndVersion) {
		var parsedIdentifierAndVersion = new IdentifierAndVersion(requestScopedMunicipalityId.getValue(), identifierAndVersion);

		return dbIntegration.getTemplate(parsedIdentifierAndVersion.getMunicipalityId(), parsedIdentifierAndVersion.getIdentifier(), parsedIdentifierAndVersion.getVersion()).isPresent();
	}
}
