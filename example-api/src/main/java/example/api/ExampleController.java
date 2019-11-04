package example.api;

import model.GetUsersDto;
import model.Photo;
import model.User;
import model.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

@RestController
public class ExampleController {

    private static final Integer PAGE_SIZE = 3;

    private AtomicInteger usersCount = new AtomicInteger(7);

    private Map<Integer, User> users = new LinkedHashMap<Integer, User>() {{
        put(1, new User(1, "cartman@gmail.com", "Eric Cartman"));
        put(2, new User(2, "marsh@gmail.com", "Stan Marsh"));
        put(3, new User(3, "broflo@gmail.com", "Kyle Broflofski"));
        put(4, new User(4, "mccormick@gmail.com", "Kenny McCormick"));
        put(5, new User(5, "butters@gmail.com", "Butters Scotch"));
        put(6, new User(6, "chickenlover@gmail.com", "Chicken Lover"));
        put(7, new User(7, "officer@gmail.com", "Officer Barbrady"));
    }};

    private Map<String, Photo> photos = new HashMap<>();

    @GetMapping("/users")
    public GetUsersDto getUsers(@RequestParam(defaultValue = "1") Integer page) {
        List<User> data = this.users.values().stream()
                .skip((page - 1) * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .collect(toList());

        return GetUsersDto.builder()
                .data(data)
                .page(page)
                .perPage(PAGE_SIZE)
                .total(users.size())
                .totalPages(users.size() / PAGE_SIZE + 1)
                .build();
    }

    @GetMapping("/users/list")
    public List<User> getUsersAsList() {
        return new ArrayList<>(this.users.values());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity getUser(@PathVariable Integer id) {
        User user = users.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createUserFromJson(@RequestBody UserDto createUserDto) {
        int id = usersCount.incrementAndGet();
        User user = new User(id, createUserDto.getEmail(), createUserDto.getName());
        this.users.put(id, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity createUserFromFormUrlEncoded(@RequestParam Map<String, String> createUserMap) {
        int id = usersCount.incrementAndGet();
        User user = new User(id, createUserMap.get("email"), createUserMap.get("name"));
        this.users.put(id, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity updateUser(@PathVariable Integer id, @RequestBody UserDto updateUserDto) {
        User user = users.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setEmail(updateUserDto.getEmail());
        user.setName(updateUserDto.getName());

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity deleteUser(@PathVariable Integer id) {
        if (users.containsKey(id)) {
            users.remove(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/photos")
    public ResponseEntity uploadPhoto(@RequestParam MultipartFile photo, @RequestParam String title) throws IOException {
        photos.put(title, new Photo(photo.getContentType(), photo.getBytes()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/photos/{title}")
    public ResponseEntity getPhoto(@PathVariable String title) throws IOException {
        Photo photo = photos.get(title);
        if (photo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .body(photo.getContent());
    }
}
