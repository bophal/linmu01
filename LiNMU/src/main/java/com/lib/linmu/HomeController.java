package com.lib.linmu;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

public class HomeController {
	@GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }
    
	@GetMapping("/book-list")
    public String bookList() {
        return "book-list";
    }
    /*@GetMapping("/hello")
    public String hello() {
        return "Hello from LiNMU!";
    }*/

}
