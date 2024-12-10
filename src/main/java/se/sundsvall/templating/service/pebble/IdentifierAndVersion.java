package se.sundsvall.templating.service.pebble;

import static se.sundsvall.templating.util.TemplateUtil.bytesToString;
import static se.sundsvall.templating.util.TemplateUtil.decodeBase64;

import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import se.sundsvall.templating.service.pebble.loader.DelegatingLoader;

@Getter
public class IdentifierAndVersion {

	static final Pattern PATTERN = Pattern.compile("^((DIRECT:)?[A-Za-z\\d\\-.]+)(:(\\d+\\.\\d+))?$");

	private final String identifier;
	private final String version;
	private final String municipalityId;

	public IdentifierAndVersion(final String municipalityId, final String identifierAndVersion) {
		this.municipalityId = municipalityId;
		if (identifierAndVersion.startsWith(DelegatingLoader.DIRECT_PREFIX)) {
			identifier = DelegatingLoader.DIRECT_PREFIX + bytesToString(decodeBase64(identifierAndVersion.substring(DelegatingLoader.DIRECT_PREFIX.length())));
			version = DelegatingLoader.DIRECT_PREFIX + "NOVERSION";
		} else {
			var matcher = PATTERN.matcher(identifierAndVersion);
			if (!matcher.matches()) {
				throw new IllegalArgumentException("Unable to parse identifier and/or version");
			}

			identifier = matcher.group(1);
			version = matcher.group(4);
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		var other = (IdentifierAndVersion) o;
		return identifier.equals(other.identifier) && Objects.equals(version, other.version) && municipalityId.equals(other.municipalityId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, version, municipalityId);
	}

	@Override
	public String toString() {
		var sb = new StringBuilder(identifier);
		if (StringUtils.isNotBlank(version)) {
			sb.append(":").append(version);
		}
		sb.append("@").append(municipalityId);
		return sb.toString();
	}
}
