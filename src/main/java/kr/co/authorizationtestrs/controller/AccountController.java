package kr.co.authorizationtestrs.controller;

import kr.co.authorizationtestrs.entity.UserAccount;
import kr.co.authorizationtestrs.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final UserAccountRepository repo;
    private final PasswordEncoder encoder;



    @PostMapping
    public UserAccount create(@RequestBody UserAccount req) {
        req.updatePassword(encoder.encode(req.getPassword()));
        return repo.save(req);
    }

    @GetMapping("/{username}")
    public UserAccount get(@PathVariable String username) {
        return repo.findByUsername(username).orElseThrow();
    }
}
