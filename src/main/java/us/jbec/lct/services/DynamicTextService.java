package us.jbec.lct.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import us.jbec.lct.models.DynamicTextType;
import us.jbec.lct.models.database.DynamicText;
import us.jbec.lct.repositories.DynamicTextRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for retrieving dynamic text
 */
@Service
public class DynamicTextService {
    private final DynamicTextRepository dynamicTextRepository;

    /**
     * Service for retrieving dynamic text
     * @param dynamicTextRepository autowired parameter
     */
    public DynamicTextService(DynamicTextRepository dynamicTextRepository) {
        this.dynamicTextRepository = dynamicTextRepository;
    }

    /**
     * Retrieve optional dynamic text if found by ID
     * @param id ID of dynamic text to retrieve
     * @return
     */
    @Cacheable("dynamicText")
    public Optional<String> retrieveDynamicText(String id) {
        var optionalDynamicText =  dynamicTextRepository.findById(id);
        Optional<String> optionalString = Optional.empty();
        if (optionalDynamicText.isPresent() && isEffective(optionalDynamicText.get())) {
                optionalString = optionalDynamicText.map(DynamicText::getText);
        }
        return optionalString;
    }

    /**
     * Retrieve dynamic text by type
     * @param dynamicTextType type to find dynamic text for
     * @return List of effective dynamic text by type
     */
    @Cacheable("dynamicText")
    public List<DynamicText> retrieveDynamicTextByType(DynamicTextType dynamicTextType) {
        return dynamicTextRepository
                .getDynamicTextByDynamicTextTypeOrderBySortOrderDesc(dynamicTextType)
                .stream()
                .filter(this::isEffective)
                .toList();
    }

    private boolean isEffective(DynamicText dynamicText) {
        boolean effective = true;
        if (dynamicText.getEffectiveDate() != null && dynamicText.getEffectiveDate().isAfter(LocalDateTime.now())) {
            effective = false;
        } else if (dynamicText.getExpiryDate() != null && dynamicText.getExpiryDate().isBefore(LocalDateTime.now())) {
            effective = false;
        }
        return effective;
    }

}
