package example;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.util.StdConverter;
import example.domain.model.employee.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Field;
import java.util.stream.Stream;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> {
            // setter,getter,is-getterを使用しない
            jacksonObjectMapperBuilder.visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            // publicコンストラクタを使用する
            jacksonObjectMapperBuilder.visibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.PUBLIC_ONLY);
            // フィールドを使用する
            jacksonObjectMapperBuilder.visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        };
    }

    @Bean
    Module customModule() {
        // シリアライザを追加
        // デシリアライズはpublicコンストラクタを使用する
        SimpleModule simpleModule = new SimpleModule();

        // toStringでシリアライズするクラス
        Stream.of(
                MailAddress.class,
                PhoneNumber.class,
                Name.class
        ).forEach(clz -> simpleModule.addSerializer(clz, new ToStringSerializer()));

        // valueフィールドでシリアライズするクラス
        Stream.of(
                EmployeeNumber.class
        ).forEach(clz -> {
            try {
                Field valueField = clz.getDeclaredField("value");
                valueField.setAccessible(true);
                // valueの型のSerializerにdelegateする
                StdDelegatingSerializer serializer = new StdDelegatingSerializer(
                        new StdConverter<Object, Object>() {
                            @Override
                            public Object convert(Object value) {
                                try {
                                    return valueField.get(value);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                );
                simpleModule.addSerializer(clz, serializer);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        });

        // listフィールドでシリアライズするクラス（first class collection)
        Stream.of(
                ContractingEmployees.class
        ).forEach(clz -> {
            try {
                Field valueField = clz.getDeclaredField("list");
                valueField.setAccessible(true);
                // listの型のSerializerにdelegateする
                StdDelegatingSerializer serializer = new StdDelegatingSerializer(
                        new StdConverter<Object, Object>() {
                            @Override
                            public Object convert(Object value) {
                                try {
                                    return valueField.get(value);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                );
                simpleModule.addSerializer(clz, serializer);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        });

        return simpleModule;
    }
}