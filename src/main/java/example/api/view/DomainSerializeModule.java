package example.api.view;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.util.StdConverter;
import example.domain.model.employee.*;

import java.lang.reflect.Field;
import java.util.stream.Stream;

public class DomainSerializeModule extends SimpleModule {

    // toStringでシリアライズするクラス
    private static final Class<?>[] toStringClasses = {
            MailAddress.class,
            PhoneNumber.class,
            Name.class
    };

    // valueフィールドでシリアライズするクラス
    private static final Class<?>[] valueFieldClasses = {
            EmployeeNumber.class
    };

    // listフィールドでシリアライズするクラス（first class collection)
    private static final Class<?>[] listFieldClasses = {
            ContractingEmployees.class
    };

    DomainSerializeModule() {
        // {"value":string} => string
        Stream.of(toStringClasses)
                .forEach(clz -> this.addSerializer(clz, new ToStringSerializer()));

        // {"value":value} => value
        Stream.of(valueFieldClasses)
                .forEach(clz -> {
                    try {
                        Field field = clz.getDeclaredField("value");
                        // valueの型のSerializerにdelegateする
                        addSerializer(clz, delegateSerializer(field));
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                });

        // {"list":array} => array
        Stream.of(listFieldClasses)
                .forEach(clz -> {
                    try {
                        Field field = clz.getDeclaredField("list");
                        // listの型のSerializerにdelegateする
                        addSerializer(clz, delegateSerializer(field));
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                });

    }

    private StdDelegatingSerializer delegateSerializer(Field field) {
        field.setAccessible(true);
        return new StdDelegatingSerializer(
                new StdConverter<>() {
                    @Override
                    public Object convert(Object value) {
                        try {
                            return field.get(value);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }
}
