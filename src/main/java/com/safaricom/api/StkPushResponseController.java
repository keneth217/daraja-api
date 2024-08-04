package com.safaricom.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/responses")
public class StkPushResponseController {

    private final StkPushResponseRepository responseRepository;

    public StkPushResponseController(StkPushResponseRepository responseRepository) {
        this.responseRepository = responseRepository;
    }

    @GetMapping
    public List<StkPushResponse> getAllResponses() {
        return responseRepository.findAll();
    }
}
