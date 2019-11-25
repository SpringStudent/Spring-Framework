package org.springframework.studymvc.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 周宁
 * @Date 2019-09-27 14:23
 */
@RestController
@RequestMapping("/path/variable")
public class PathVariableController {

    @GetMapping("/test/{version}")
    public String test(@PathVariable String version){
        System.out.println(version);
        return "helloworld";
    }
}
