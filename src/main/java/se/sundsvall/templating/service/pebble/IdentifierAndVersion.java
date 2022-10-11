package se.sundsvall.templating.service.pebble;

import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import se.sundsvall.templating.service.pebble.loader.DelegatingLoader;
import se.sundsvall.templating.util.BASE64;

import lombok.Getter;

@Getter
public class IdentifierAndVersion {

    static final Pattern PATTERN = Pattern.compile("^((DIRECT:)?[A-Za-z\\d\\-.]+)(:(\\d+\\.\\d+))?$");

    private final String identifier;
    private final String version;

    public IdentifierAndVersion(final String identiferAndVersion) {
        if (identiferAndVersion.startsWith(DelegatingLoader.DIRECT_PREFIX)) {
            identifier = DelegatingLoader.DIRECT_PREFIX + BASE64.decode(identiferAndVersion.substring(DelegatingLoader.DIRECT_PREFIX.length()));
            version = DelegatingLoader.DIRECT_PREFIX + "NOVERSION";
        } else {
            var matcher = PATTERN.matcher(identiferAndVersion);
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
        return identifier.equals(other.identifier) && Objects.equals(version, other.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, version);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder(identifier);
        if (StringUtils.isNotBlank(version)) {
            sb.append(":").append(version);
        }
        return sb.toString();
    }
}
