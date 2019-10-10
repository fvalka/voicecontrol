package com.vektorraum.voicecontrol.phoneprovider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/status")
@Slf4j
public class CallStatusRest {
    /**
     * Handles the call status callback
     * @param body
     */
    @PostMapping("/")
    public void status(@RequestBody MultiValueMap<String, String> body) {

    }
}
