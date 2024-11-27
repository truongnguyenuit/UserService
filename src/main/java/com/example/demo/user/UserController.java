package com.example.demo.user;

import com.example.demo.exception.ApiRequestException;
import com.example.demo.user.config.JwtProvider;
import com.example.demo.user.types.AuthResponse;
import com.example.demo.user.types.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("api/user")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtProvider jwtProvider,
                          UserDetailsService userDetailsService,
                          UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<User>> getUserById(@PathVariable Long id) throws ApiRequestException {
        return new ResponseEntity<>(userService.getUserById(id), HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> createUserHandler(@RequestBody User user) throws ApiRequestException {
        User isEmailExist = userRepository.findByEmail(user.getEmail());
        if (isEmailExist != null) {
            throw new ApiRequestException("Email is already used with another account");
        }
        userRepository.save(new User(
                user.getName(),
                user.getEmail(),
                passwordEncoder.encode(user.getPassword()),
                user.getRole())
        );
        return new ResponseEntity<>("Sign up successful", HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@RequestBody LoginRequest req) throws ApiRequestException {
        Authentication authentication = authenticate(req.email(), req.password());

        //Generate jwt from Authentication object
        String jwt = jwtProvider.generateToken(authentication);

        //Get authorities from Authentication Object
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String role = authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();
        return new ResponseEntity<>(new AuthResponse(jwt, "Login successful", USER_ROLE.valueOf(role)), HttpStatus.CREATED);
    }

    private Authentication authenticate(String username, String password) {

        //Load user from database
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails == null) {
            throw new ApiRequestException("Invalid username ...");
        }

        //Compare between req password & password from Spring security
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new ApiRequestException("Invalid password...");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
