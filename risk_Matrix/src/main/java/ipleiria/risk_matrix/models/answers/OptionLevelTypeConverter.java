package ipleiria.risk_matrix.models.answers;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ipleiria.risk_matrix.models.questions.OptionLevelType;

@Converter(autoApply = false)
public class OptionLevelTypeConverter implements AttributeConverter<OptionLevelType, String> {

    @Override
    public String convertToDatabaseColumn(OptionLevelType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public OptionLevelType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : OptionLevelType.valueOf(dbData);
    }
}
