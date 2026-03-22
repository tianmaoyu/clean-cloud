package org.clean.web;

import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import org.clean.CleanException;
import org.clean.Result;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Type;

//@Configuration
public class FeignDecoderConfig {

//    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public Decoder feignDecoder() {
        return new ResultAwareSpringDecoder(this.messageConverters);
    }

    public static class ResultAwareSpringDecoder implements Decoder {
        private final SpringDecoder springDecoder;

        public ResultAwareSpringDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
            this.springDecoder = new SpringDecoder(messageConverters);
        }

        @Override
        public Object decode(Response response, Type type) throws IOException, FeignException {
            Object result = springDecoder.decode(response, type);

            if (result instanceof Result) {
                Result<?> r = (Result<?>) result;
                if (!r.isSuccess()) {
                    throw new CleanException(r.getCode(), r.getMessage());
                }
            }

            return result;
        }

    }
}