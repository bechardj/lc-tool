package us.jbec.lct.services;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import us.jbec.lct.models.DynamicTextType;
import us.jbec.lct.models.database.DynamicText;
import us.jbec.lct.repositories.DynamicTextRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class DynamicTextServiceTest {

    @InjectMocks
    private DynamicTextService testee;

    @Mock
    private DynamicTextRepository dynamicTextRepository;

    @BeforeSuite
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRetrieveDynamicText_TextOnly() {
        DynamicText dynamicText = new DynamicText();
        dynamicText.setText("hello");
        when(dynamicTextRepository.findById("test")).thenReturn(Optional.of(dynamicText));

        Optional<String> result = testee.retrieveDynamicText("test");

        assertFalse(result.isEmpty());
        assertEquals(result.get(), "hello");
    }

    @Test
    public void testRetrieveDynamicText_NotFound() {
        DynamicText dynamicText = new DynamicText();
        dynamicText.setText("hello");
        when(dynamicTextRepository.findById("test")).thenReturn(Optional.empty());

        Optional<String> result = testee.retrieveDynamicText("test");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testRetrieveDynamicText_Effective() {
        DynamicText dynamicText = new DynamicText();
        dynamicText.setText("hello");
        dynamicText.setEffectiveDate(LocalDateTime.now().minusDays(1));
        when(dynamicTextRepository.findById("test")).thenReturn(Optional.of(dynamicText));

        Optional<String> result = testee.retrieveDynamicText("test");

        assertFalse(result.isEmpty());
        assertEquals(result.get(), "hello");
    }

    @Test
    public void testRetrieveDynamicText_NotEffective() {
        DynamicText dynamicText = new DynamicText();
        dynamicText.setText("hello");
        dynamicText.setEffectiveDate(LocalDateTime.now().plusDays(1));
        when(dynamicTextRepository.findById("test")).thenReturn(Optional.of(dynamicText));

        Optional<String> result = testee.retrieveDynamicText("test");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testRetrieveDynamicText_Expired() {
        DynamicText dynamicText = new DynamicText();
        dynamicText.setText("hello");
        dynamicText.setExpiryDate(LocalDateTime.now().minusDays(1));
        when(dynamicTextRepository.findById("test")).thenReturn(Optional.of(dynamicText));

        Optional<String> result = testee.retrieveDynamicText("test");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testRetrieveDynamicText_NotExpired() {
        DynamicText dynamicText = new DynamicText();
        dynamicText.setText("hello");
        dynamicText.setExpiryDate(LocalDateTime.now().plusDays(1));
        when(dynamicTextRepository.findById("test")).thenReturn(Optional.of(dynamicText));

        Optional<String> result = testee.retrieveDynamicText("test");

        assertFalse(result.isEmpty());
        assertEquals(result.get(), "hello");
    }

    @Test
    public void testRetrieveDynamicText_InRange() {
        DynamicText dynamicText = new DynamicText();
        dynamicText.setText("hello");
        dynamicText.setEffectiveDate(LocalDateTime.now().minusMinutes(1));
        dynamicText.setExpiryDate(LocalDateTime.now().plusDays(1));
        when(dynamicTextRepository.findById("test")).thenReturn(Optional.of(dynamicText));

        Optional<String> result = testee.retrieveDynamicText("test");

        assertFalse(result.isEmpty());
        assertEquals(result.get(), "hello");
    }

    @Test
    public void testRetrieveDynamicText_ExpireBeforeEffective() {
        DynamicText dynamicText = new DynamicText();
        dynamicText.setText("hello");
        dynamicText.setExpiryDate(LocalDateTime.now().minusDays(2));
        dynamicText.setEffectiveDate(LocalDateTime.now().minusDays(1));
        when(dynamicTextRepository.findById("test")).thenReturn(Optional.of(dynamicText));

        Optional<String> result = testee.retrieveDynamicText("test");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testRetrieveDynamicText_NotEffectiveWithExpiry() {
        DynamicText dynamicText = new DynamicText();
        dynamicText.setText("hello");
        dynamicText.setExpiryDate(LocalDateTime.now().plusHours(2));
        dynamicText.setEffectiveDate(LocalDateTime.now().plusHours(1));
        when(dynamicTextRepository.findById("test")).thenReturn(Optional.of(dynamicText));

        Optional<String> result = testee.retrieveDynamicText("test");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testRetrieveDynamicTextByType() {
        DynamicText dynamicText1 = new DynamicText();
        dynamicText1.setText("hello");
        dynamicText1.setExpiryDate(LocalDateTime.now().plusDays(1));
        dynamicText1.setEffectiveDate(LocalDateTime.now().minusDays(1));
        dynamicText1.setDynamicTextType(DynamicTextType.RELEASE_NOTES);

        DynamicText dynamicText2 = new DynamicText();
        dynamicText2.setText("hello");
        dynamicText2.setExpiryDate(LocalDateTime.now().minusDays(1));
        dynamicText2.setEffectiveDate(LocalDateTime.now().minusDays(1));
        dynamicText2.setDynamicTextType(DynamicTextType.RELEASE_NOTES);

        when(dynamicTextRepository.getDynamicTextByDynamicTextTypeOrderBySortOrderDesc(DynamicTextType.RELEASE_NOTES))
                .thenReturn(Arrays.asList(dynamicText1, dynamicText2));

        List<DynamicText> result = testee.retrieveDynamicTextByType(DynamicTextType.RELEASE_NOTES);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0), dynamicText1);

    }
}