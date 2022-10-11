package se.sundsvall.templating.service.pebble.loader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringReader;

import com.mitchellbosecke.pebble.loader.StringLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.templating.service.pebble.IdentifierAndVersion;

@ExtendWith(MockitoExtension.class)
class DelegatingLoaderTests {

    @Mock
    private DatabaseLoader mockDatabaseLoader;
    @Mock
    private StringLoader mockStringLoader;

    private DelegatingLoader loader;

    @BeforeEach
    void setUp() {
        loader = new DelegatingLoader(mockDatabaseLoader, mockStringLoader);
    }

    @Test
    void test_getReader() {
        when(mockDatabaseLoader.getReader(any(IdentifierAndVersion.class))).thenReturn(new StringReader(""));

        var reader = loader.getReader(new IdentifierAndVersion("someTemplateId"));
        assertThat(reader).isNotNull();

        verify(mockStringLoader, never()).getReader(any(String.class));
        verify(mockDatabaseLoader, times(1)).getReader(any(IdentifierAndVersion.class));
    }

    @Test
    void test_getReader_directRendering() {
        when(mockStringLoader.getReader(any(String.class))).thenReturn(new StringReader(""));

        var reader = loader.getReader(new IdentifierAndVersion("DIRECT:someTemplate"));
        assertThat(reader).isNotNull();

        verify(mockStringLoader, times(1)).getReader(any(String.class));
        verify(mockDatabaseLoader, never()).getReader(any(IdentifierAndVersion.class));
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
        when(mockDatabaseLoader.resolveRelativePath(any(String.class), any(String.class)))
            .thenReturn("someResolvedTemplateId");

        assertThat(loader.resolveRelativePath("someTemplateId", "someOtherTemplateId"))
            .isEqualTo("someResolvedTemplateId");

        verify(mockStringLoader, never()).resolveRelativePath(any(String.class), any(String.class));
        verify(mockDatabaseLoader, times(1)).resolveRelativePath(any(String.class), any(String.class));
    }

    @Test
    void test_resolveRelativePath_directRendering() {
        assertThat(loader.resolveRelativePath("someTemplateId", DelegatingLoader.DIRECT_PREFIX + ":" + "someOtherTemplateId"))
            .isNull();

        verify(mockStringLoader, times(1)).resolveRelativePath(any(String.class), any(String.class));
        verify(mockDatabaseLoader, never()).resolveRelativePath(any(String.class), any(String.class));
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
        when(mockDatabaseLoader.resourceExists(any(String.class))).thenReturn(true);

        assertThat(loader.resourceExists("someTemplateId")).isTrue();

        verify(mockStringLoader, never()).resourceExists(any(String.class));
        verify(mockDatabaseLoader, times(1)).resourceExists(any(String.class));
    }

    @Test
    void test_resourceExists_directRendering() {
        when(mockStringLoader.resourceExists(any(String.class))).thenReturn(true);

        assertThat(loader.resourceExists(DelegatingLoader.DIRECT_PREFIX + ":" + "someTemplateId")).isTrue();

        verify(mockStringLoader, times(1)).resourceExists(any(String.class));
        verify(mockDatabaseLoader, never()).resourceExists(any(String.class));
    }
}
