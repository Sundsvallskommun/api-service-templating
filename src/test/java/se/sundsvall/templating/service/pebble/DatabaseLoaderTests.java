package se.sundsvall.templating.service.pebble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import com.mitchellbosecke.pebble.error.LoaderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.templating.TemplateFlavor;
import se.sundsvall.templating.integration.db.DbIntegration;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;

@ExtendWith(MockitoExtension.class)
class DatabaseLoaderTests {

    @Mock
    private DbIntegration mockDbIntegration;
    @Mock
    private TemplateEntity mockTemplateEntity;
    @Mock
    private Map<TemplateFlavor, String> mockVariants;

    private DatabaseLoader loader;

    @BeforeEach
    void setUp() {
        loader = new DatabaseLoader(mockDbIntegration);
    }

    @Test
    void test_getReader() {
        when(mockVariants.containsKey(any(TemplateFlavor.class))).thenReturn(true);
        when(mockVariants.get(any(TemplateFlavor.class))).thenReturn("someContent");
        when(mockTemplateEntity.getVariants()).thenReturn(mockVariants);
        when(mockDbIntegration.getTemplate(any(String.class))).thenReturn(Optional.of(mockTemplateEntity));

        var reader = loader.getReader("someTemplateId:TEXT");
        assertThat(reader).isNotNull();

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class));
        verify(mockTemplateEntity, times(2)).getVariants();
        verify(mockVariants, times(1)).containsKey(any(TemplateFlavor.class));
        verify(mockVariants, times(1)).get(any(TemplateFlavor.class));
    }

    @Test
    void test_getReader_whenTemplateDoesNotExist() {
        var templateId = "someTemplateId:TEXT";

        when(mockDbIntegration.getTemplate(any(String.class))).thenReturn(Optional.empty());

        assertThatExceptionOfType(LoaderException.class)
            .isThrownBy(() -> loader.getReader(templateId))
            .withMessageStartingWith("Unable to find template 'someTemplateId'");

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class));
    }

    @Test
    void test_getReader_whenTemplateFlavorDoesNotExist() {
        var templateId = "someTemplateId:TEXT";

        when(mockVariants.containsKey(any(TemplateFlavor.class))).thenReturn(false);
        when(mockTemplateEntity.getVariants()).thenReturn(mockVariants);
        when(mockDbIntegration.getTemplate(any(String.class))).thenReturn(Optional.of(mockTemplateEntity));

        assertThatExceptionOfType(LoaderException.class)
            .isThrownBy(() -> loader.getReader(templateId))
            .withMessageStartingWith("Unable to find template variant 'TEXT' for template 'someTemplateId'");

        verify(mockDbIntegration, times(1)).getTemplate(any(String.class));
        verify(mockTemplateEntity, times(1)).getVariants();
        verify(mockVariants, times(1)).containsKey(any(TemplateFlavor.class));
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

        assertThat(loader.resolveRelativePath(relativePath, "someOtherTemplateId:SOME_FLAVOR"))
            .isEqualTo(relativePath + ":SOME_FLAVOR");
    }

    @Test
    void test_resolveRelativePath_withFlavor() {
        var relativePath = "someTemplateId:SOME_FLAVOR";

        assertThat(loader.resolveRelativePath(relativePath, "someOtherTemplateId"))
            .isEqualTo(relativePath);
    }

    @Test
    void test_createCacheKey() {
        assertThat(loader.createCacheKey("someTemplateId")).isEqualTo("someTemplateId");
    }

    @Test
    void test_resourceExists() {
        when(mockDbIntegration.templateExists(any(String.class))).thenReturn(true);
        assertThat(loader.resourceExists("someTemplateId")).isTrue();

        verify(mockDbIntegration, times(1)).templateExists(any(String.class));
    }

    @Test
    void test_resourceExists_whenResourceDoesNotExist() {
        when(mockDbIntegration.templateExists(any(String.class))).thenReturn(false);
        assertThat(loader.resourceExists("someTemplateId")).isFalse();

        verify(mockDbIntegration, times(1)).templateExists(any(String.class));
    }
}
