package com.programmingtech.orderservice.controlller;

import com.programmingtech.orderservice.dto.OrderRequest;
import com.programmingtech.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
//@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    //we have to configure here to ensure circuit breaker features
    //circuit breaker is being  configure in application.properties
    //and also we have to configure our controllers *** with the single line anotation below
    @CircuitBreaker(name="inventory",fallbackMethod = "fallbackMethod")
    //fallback method is for call the emergency function when circuit brekaer is open (api don't reply)
    @TimeLimiter(name="inventory")
    //we should use the same key key with the app.properties file //which was inventory in there

    @Retry(name="inventory")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest orderRequest){
        return CompletableFuture.supplyAsync( ()-> orderService.placeOrder(orderRequest) );
//        return "Order Placed Successfully";
    }

    //changed the return type as CompletableFuture from the String after adding the TimeLimter anotation
    //for resilience4j circuit breaker

    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest,RuntimeException runtimeException){
        return CompletableFuture.supplyAsync( ()-> "Something happened while trying to call placeOrder method, please try again!" );
    }


}
