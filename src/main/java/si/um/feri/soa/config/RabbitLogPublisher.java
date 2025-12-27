package si.um.feri.soa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class RabbitLogPublisher {

    private final Logger logger = LoggerFactory.getLogger(RabbitLogPublisher.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${RABBITMQ_HOST:rabbitmq}")
    private String host;

    @Value("${RABBITMQ_PORT:5672}")
    private int port;

    @Value("${RABBITMQ_USER:guest}")
    private String username;

    @Value("${RABBITMQ_PASSWORD:guest}")
    private String password;

    @Value("${RABBITMQ_EXCHANGE:logs-exchange}")
    private String exchange;

    @Value("${RABBITMQ_QUEUE:logs-queue}")
    private String queue;

    @Value("${RABBITMQ_ROUTING_KEY:logs.route}")
    private String routingKey;

    @Value("${spring.application.name:soa-shared-expense}")
    private String serviceName;

    private Connection connection;
    private Channel channel;

    private synchronized Channel channel() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            return channel;
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(exchange, "direct", true);
        channel.queueDeclare(queue, true, false, false, null);
        channel.queueBind(queue, exchange, routingKey);
        return channel;
    }

    public void send(String level, String message, String url, String correlationId, String method, int statusCode, Long durationMs) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("timestamp", Instant.now().toString());
            payload.put("level", level);
            payload.put("message", message);
            payload.put("service", serviceName);
            payload.put("correlation_id", correlationId);
            payload.put("url", url);
            payload.put("method", method);
            payload.put("status_code", statusCode);
            payload.put("detail", durationMs != null ? durationMs + "ms" : null);
            payload.put("formatted", String.format("%s %s %s Correlation:%s [%s] - %s",
                    payload.get("timestamp"), level, url, correlationId == null ? "-" : correlationId, serviceName, message));

            byte[] body = objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
            channel().basicPublish(
                    exchange,
                    routingKey,
                    new AMQP.BasicProperties.Builder()
                            .contentType("application/json")
                            .deliveryMode(2)
                            .build(),
                    body
            );
        } catch (Exception e) {
            logger.warn("Failed to publish log to RabbitMQ: {}", e.getMessage());
        }
    }
}
