package ipleiria.risk_matrix.models.feedback;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class FeedbackTypeConverter implements AttributeConverter<FeedbackType, String> {

    @Override
    public String convertToDatabaseColumn(FeedbackType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public FeedbackType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : FeedbackType.valueOf(dbData);
    }
}

