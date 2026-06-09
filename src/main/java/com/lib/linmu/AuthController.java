package com.lib.linmu;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
	 @Autowired
	    private UserRepository userRepository;

	    // --- LOGIN ---
	    @PostMapping("/login")
	    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
	        // Find user by email or username
	        Optional<User> userOpt = userRepository.findByEmail(req.getUsername());

	        // Fallback: check against hardcoded admin for demo
	        if (userOpt.isEmpty()) {
	            if ("admin".equals(req.getUsername()) && "admin123".equals(req.getPassword())) {
	                Map<String, Object> resp = new HashMap<>();
	                resp.put("token", "demo-token-admin-" + System.currentTimeMillis());
	                resp.put("user", Map.of("name", "Admin", "email", "admin@linmu.edu.kh", "role", "ADMIN"));
	                return ResponseEntity.ok(resp);
	            }
	            return ResponseEntity.status(401).body(Map.of("message", "Username ឬ Password មិនត្រឹមត្រូវ"));
	        }

	        User user = userOpt.get();

	        // Simple password check (in production, use BCrypt)
	        if (!user.getPassword().equals(req.getPassword())) {
	            return ResponseEntity.status(401).body(Map.of("message", "Username ឬ Password មិនត្រឹមត្រូវ"));
	        }

	        // Return token + user info
	        Map<String, Object> resp = new HashMap<>();
	        resp.put("token", "token-" + user.getId() + "-" + System.currentTimeMillis());
	        resp.put("user", Map.of(
	            "id", user.getId(),
	            "name", user.getName(),
	            "email", user.getEmail(),
	            "role", user.getRole()
	        ));
	        return ResponseEntity.ok(resp);
	    }

	    // --- REGISTER ---
	    @PostMapping("/register")
	    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
	        if (userRepository.existsByEmail(req.getEmail())) {
	            return ResponseEntity.badRequest().body(Map.of("message", "អ៊ីមែលនេះត្រូវបានប្រើហើយ"));
	        }

	        User user = new User();
	        user.setName(req.getName());
	        user.setEmail(req.getEmail());
	        user.setPassword(req.getPassword()); // Hash in production!
	        user.setStudentId(req.getStudentId());
	        user.setRole("USER");

	        userRepository.save(user);
	        return ResponseEntity.ok(Map.of("message", "ចុះឈ្មោះបានជោគជ័យ"));
	    }

	    // --- DTOs ---
	    public static class LoginRequest {
	        private String username;
	        private String password;
	        public String getUsername() { return username; }
	        public void setUsername(String u) { this.username = u; }
	        public String getPassword() { return password; }
	        public void setPassword(String p) { this.password = p; }
	    }

	    public static class RegisterRequest {
	        private String name, email, password, studentId;
	        public String getName() { return name; }
	        public void setName(String n) { this.name = n; }
	        public String getEmail() { return email; }
	        public void setEmail(String e) { this.email = e; }
	        public String getPassword() { return password; }
	        public void setPassword(String p) { this.password = p; }
	        public String getStudentId() { return studentId; }
	        public void setStudentId(String s) { this.studentId = s; }
	    }



}
