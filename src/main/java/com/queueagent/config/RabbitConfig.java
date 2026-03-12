package com.queueagent.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE_NAME = "inventory.exchange";
    public static final String QUEUE_NAME = "inventory.queue";
    public static final String ROUTING_KEY = "inventory.routing.key";
    public static final String DLQ_NAME = "inventory.dlq";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue inventoryQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", "")       // default exchange 사용
                .withArgument("x-dead-letter-routing-key", DLQ_NAME)
                .build();
    }

    // default exchange는 큐 이름으로 자동 라우팅 → 별도 Exchange/Binding 불필요
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding binding(Queue inventoryQueue, TopicExchange inventoryExchange) {
        return BindingBuilder.bind(inventoryQueue).to(inventoryExchange).with(ROUTING_KEY);
    }
}
