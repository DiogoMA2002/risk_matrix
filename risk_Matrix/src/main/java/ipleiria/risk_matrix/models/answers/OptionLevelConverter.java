package ipleiria.risk_matrix.models.answers;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ipleiria.risk_matrix.models.questions.OptionLevel;

@Converter(autoApply = false)
public class OptionLevelConverter implements AttributeConverter<OptionLevel, String> {

    @Override
    public String convertToDatabaseColumn(OptionLevel attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public OptionLevel convertToEntityAttribute(String dbData) {
        return dbData == null ? null : OptionLevel.valueOf(dbData);
    }
}
