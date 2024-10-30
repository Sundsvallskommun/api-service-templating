package se.sundsvall.templating.integration.db.entity;

import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode(of = {
	"major", "minor"
})
@Builder(setterPrefix = "with")
@NoArgsConstructor
public class Version implements Comparable<Version> {

	private static final Pattern PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)$");

	public enum IncrementMode {
		MAJOR,
		MINOR
	}

	@Column(name = "major", columnDefinition = "SMALLINT")
	private int major;
	@Column(name = "minor", columnDefinition = "SMALLINT")
	private int minor;

	public static Version parse(final String s) {
		var matcher = PATTERN.matcher(s);
		if (matcher.matches()) {
			return new Version(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
		}

		return null;
	}

	public Version(final int major, final int minor) {
		this.major = major;
		this.minor = minor;
	}

	public Version apply(final IncrementMode incrementMode) {
		return switch (incrementMode) {
			case MAJOR -> new Version(major + 1, 0);
			case MINOR -> new Version(major, minor + 1);
		};
	}

	@Override
	public String toString() {
		return String.format("%d.%s", major, minor);
	}

	@Override
	public int compareTo(final Version other) {
		var result = Integer.compare(major, other.major);

		return result != 0 ? result : Integer.compare(minor, other.minor);
	}
}
