package com.ff.paiements_service.converter;

import com.ff.paiements_service.entity.PaymentMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentMethodConverter implements AttributeConverter<PaymentMethod, String> {
    @Override
    public String convertToDatabaseColumn(PaymentMethod method) {
        if (method == null) {
            return null;
        }
        return method.name(); // Stocke le nom de l'énumération comme une chaîne
    }

    @Override
    public PaymentMethod convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return PaymentMethod.valueOf(dbData); // Convertit la chaîne en énumération
    }
}
