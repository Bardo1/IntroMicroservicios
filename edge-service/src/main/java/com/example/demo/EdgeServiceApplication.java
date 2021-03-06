package com.example.demo;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.Resources;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableFeignClients
@EnableCircuitBreaker
@EnableDiscoveryClient
@EnableZuulProxy
@SpringBootApplication
public class EdgeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdgeServiceApplication.class, args);
	}
}

@Data
class Item {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

@Component
@FeignClient("item-catalog-service")
interface ItemClient {

	@GetMapping("/items")
	Resources<Item> readItems();
}

@RestController
class GoodItemApiAdapterRestController {

	private final ItemClient itemClient;

	public GoodItemApiAdapterRestController(ItemClient itemClient) {
		this.itemClient = itemClient;
	}

	public Collection<Item> fallback() {
		return new ArrayList<>();
	}

	 
	@HystrixCommand(fallbackMethod = "fallback")
	@GetMapping("/good-items")
	@CrossOrigin(origins = "*")
	public Collection<Item> goodItems() {
		return itemClient.readItems().getContent().stream().collect(Collectors.toList());
	}

	private boolean isGreat(Item item) {
		return !item.getName().equals("Yoga Mat") && !item.getName().equals("Pen") && !item.getName().equals("Light");
	}
}