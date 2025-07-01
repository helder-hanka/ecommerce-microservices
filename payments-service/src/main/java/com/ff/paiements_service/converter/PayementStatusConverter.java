package com.ff.paiements_service.converter;

import com.ff.paiements_service.entity.PaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PayementStatusConverter implements AttributeConverter<PaymentStatus, String> {

    @Override
    public String convertToDatabaseColumn(PaymentStatus status) {
        if (status == null) {
            return null;
        }
        return status.name(); // Stocke le nom de l'énumération comme une chaîne
    }

    @Override
    public PaymentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return PaymentStatus.valueOf(dbData); // Convertit la chaîne en énumération
    }
}
