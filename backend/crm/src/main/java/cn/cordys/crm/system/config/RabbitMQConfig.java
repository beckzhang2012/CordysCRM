package cn.cordys.crm.system.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXPORT_EXCHANGE = "export.exchange";
    public static final String EXPORT_QUEUE = "export.queue";
    public static final String EXPORT_ROUTING_KEY = "export.routing.key";

    public static final String EXPORT_DLX_EXCHANGE = "export.dlx.exchange";
    public static final String EXPORT_DLX_QUEUE = "export.dlx.queue";
    public static final String EXPORT_DLX_ROUTING_KEY = "export.dlx.routing.key";

    @Bean
    public DirectExchange exportExchange() {
        return new DirectExchange(EXPORT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange exportDlxExchange() {
        return new DirectExchange(EXPORT_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue exportQueue() {
        return QueueBuilder.durable(EXPORT_QUEUE)
                .deadLetterExchange(EXPORT_DLX_EXCHANGE)
                .deadLetterRoutingKey(EXPORT_DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue exportDlxQueue() {
        return new Queue(EXPORT_DLX_QUEUE, true);
    }

    @Bean
    public Binding exportBinding(Queue exportQueue, DirectExchange exportExchange) {
        return BindingBuilder.bind(exportQueue).to(exportExchange).with(EXPORT_ROUTING_KEY);
    }

    @Bean
    public Binding exportDlxBinding(Queue exportDlxQueue, DirectExchange exportDlxExchange) {
        return BindingBuilder.bind(exportDlxQueue).to(exportDlxExchange).with(EXPORT_DLX_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }
}
