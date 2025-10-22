package ru.yandex.practicum.catsgram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.dal.UserRepository;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.*;


@Service
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();
    //private final UserService userService;
    private final UserRepository userRepository;
    // private final Comparator<Post> postDateComparator = Comparator.comparing(Post::getPostDate);

    @Autowired
    public PostService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Collection<Post> findAll(int size, String sort, int from) {
        return switch (sort) {
            case "asc" -> posts.values().stream()
                    .sorted(Comparator.comparing(Post::getPostDate))
                    .skip(from)
                    .limit(size)
                    .toList();
            case "desc" -> posts.values().stream()
                    .sorted(Comparator.comparing(Post::getPostDate, Comparator.reverseOrder()))
                    .skip(from)
                    .limit(size)
                    .toList();
            default -> throw new IllegalArgumentException("Параметр sort задан неправильно");
        };
    }

    public Post create(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }

        userRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new ConditionsNotMetException("Автор с id = "
                        + post.getAuthorId() + " не найден"));

        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    public Optional<Post> findById(long postId) {
        return Optional.ofNullable(posts.get(postId));
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
