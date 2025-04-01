package ipleiria.risk_matrix.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import ipleiria.risk_matrix.models.questions.QuestionCategory;

import java.io.IOException;

public class QuestionCategoryDeserializer extends JsonDeserializer<QuestionCategory> {
    @Override
    public QuestionCategory deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String raw = p.getText().trim();  // remove whitespace
        for (QuestionCategory category : QuestionCategory.values()) {
            if (category.name().equalsIgnoreCase(raw)) {
                return category;
            }
        }
        throw new InvalidFormatException(p, "Invalid category: " + raw, raw, QuestionCategory.class);
    }
}
