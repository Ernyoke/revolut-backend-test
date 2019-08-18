package esz.dev.user.control;

import esz.dev.user.entity.User;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Data persistence for bank user objects. This implementation provides an in-memory solution for persistence, and
 * should not be used in real life scenarios.
 */
@Singleton
public class UserStore {
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Map<Long, User> users = new ConcurrentHashMap<>();

    public long addUser(User user) {
        long id = idGenerator.incrementAndGet();
        users.put(id, user.withId(id));
        return id;
    }

    public Optional<User> getUser(Long id) {
        return Optional.ofNullable(users.get(id));
    }
}
