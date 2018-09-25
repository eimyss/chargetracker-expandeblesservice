package de.eimantas.eimantasbackend.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import feign.Logger;
import feign.Request;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class AccountsClientConfig {

  @Value("${service.feign.connectTimeout:60000}")
  private int connectTimeout;

  @Value("${service.feign.readTimeOut:60000}")
  private int readTimeout;


  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  @Bean
  public Decoder feignDecoder() {
    HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(customObjectMapper());
    ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(jacksonConverter);
    return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
  }

  public ObjectMapper customObjectMapper() {
    ObjectMapper customMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .registerModule(new ParameterNamesModule())
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule())
        .registerModule(new Jackson2HalModule());


    return customMapper;
  }


  @Bean
  public Request.Options options() {
    return new Request.Options(connectTimeout, readTimeout);
  }
}