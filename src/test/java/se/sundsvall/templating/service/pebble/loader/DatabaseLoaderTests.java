package se.sundsvall.templating.service.pebble.loader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.mitchellbosecke.pebble.error.LoaderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.service.pebble.IdentifierAndVersion;

@ExtendWith(MockitoExtension.class)
class DatabaseLoaderTests {

    @Mock
    private DbIntegration mockDbIntegration;
    @Mock
    private TemplateEntity mockTemplateEntity;

    private DatabaseLoader loader;

    @BeforeEach
    void setUp() {
        loader = new DatabaseLoader(mockDbIntegration);
    }

    @Test
    void test_getReader() {
        when(mockTemplateEntity.getContent()).thenReturn("someContent");
        when(mockDbIntegration.getTemplate(any(String.class), any(String.class))).thenReturn(Optional.of(mockTemplateEntity));

        var reader = loader.getReader(new IdentifierAndVersion("someTemplateId:1.0"));
        assertThat(reader).isNotNull();

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class), any(String.class));
    }

    @Test
    void test_getReader_whenTemplateDoesNotExist() {
        var templateId = new IdentifierAndVersion("someTemplateId");

        when(mockDbIntegration.getTemplate(any(String.class), nullable(String.class))).thenReturn(Optional.empty());

        assertThatExceptionOfType(LoaderException.class)
            .isThrownBy(() -> loader.getReader(templateId))
            .withMessageStartingWith("Unable to find template 'someTemplateId'");

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class), nullable(String.class));
    }

    @Test
    void test_setCharset() {
        assertThatNoException().isThrownBy(() -> loader.setCharset("someCharset"));
    }

    @Test
    void test_setPrefix() {
        assertThatNoException().isThrownBy(() -> loader.setPrefix("somePrefix"));
    }

    @Test
    void test_setSuffix() {
        assertThatNoException().isThrownBy(() -> loader.setSuffix("someSuffix"));
    }

    @Test
    void test_resolveRelativePath() {
        var relativePath = "someTemplateId";

        assertThat(loader.resolveRelativePath(relativePath, relativePath)).isEqualTo(relativePath);
    }

    @Test
    void test_createCacheKey() {
        assertThat(loader.createCacheKey("someTemplateId")).satisfies(identifierAndVersion -> {
            assertThat(identifierAndVersion.getIdentifier()).isEqualTo("someTemplateId");
            assertThat(identifierAndVersion.getVersion()).isNull();
        });
    }

    @Test
    void test_resourceExists() {
        when(mockDbIntegration.getTemplate(any(String.class), eq(null)))
            .thenReturn(Optional.of(TemplateEntity.builder().build()));

        assertThat(loader.resourceExists("someTemplateId")).isTrue();

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class), eq(null));
    }

    @Test
    void test_resourceExists_whenResourceDoesNotExist() {
        when(mockDbIntegration.getTemplate(any(String.class), any(String.class)))
            .thenReturn(Optional.empty());

        assertThat(loader.resourceExists("someTemplateId:1.2")).isFalse();

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class), any(String.class));
    }
}
