package org.clean.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kafka")
public class KafkaProducerController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/send")
    public String sendMessage(@RequestParam String topic, @RequestParam String message) {
        kafkaTemplate.send(topic, message);
        return "消息已发送到主题: " + topic;
    }
}