package io.github.violetpixel.bookstore.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Guarda booleanos como 0/1 em uma coluna numérica. Funciona igual no PostgreSQL (SMALLINT),
 * MySQL (SMALLINT), SQL Server (SMALLINT) e Oracle (NUMBER(1)), inclusive em versões do Oracle sem BOOLEAN.
 */
@Converter
public class Flag implements AttributeConverter<Boolean, Short> {

    @Override
    public Short convertToDatabaseColumn(Boolean value) {
        return value == null ? null : (short) (value ? 1 : 0);
    }

    @Override
    public Boolean convertToEntityAttribute(Short value) {
        return value == null ? null : value != 0;
    }
}
