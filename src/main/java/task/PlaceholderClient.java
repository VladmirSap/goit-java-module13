package task;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class PlaceholderClient {
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/users";
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {

        System.out.println(getAllUsers());
        System.out.println(getUserById(1));
        System.out.println(getUserByUsername("Bret"));

        User newUser = new User(null, "New User", "newuser@example.com");
        User createdUser = createUser(newUser);
        System.out.println("Created User: " + createdUser);

        createdUser.setName("Updated User");
        User updatedUser = updateUser(createdUser);
        System.out.println("Updated User: " + updatedUser);

        int deleted = deleteUser(createdUser.getId());
        System.out.println("Deleted User. Status Code: " + deleted);

        // 2 task
        int userId = 1;
        getLastPostCommentsByUser(userId);
        // 3 task
        getOpenTodosByUser(userId);
    }

    public static User createUser(User user) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonInputString = gson.toJson(user);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = reader.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                User createdUser = gson.fromJson(response.toString(), User.class);
                return createdUser;
            }
        }
        return null;
    }

    public static User updateUser(User user) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/" + user.getId()).openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String jsonInputString = gson.toJson(user);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = reader.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                User updatedUser = gson.fromJson(response.toString(), User.class);
                return updatedUser;
            }
        } else {
            System.out.println("Update. Status Code: " + responseCode);
        }

        return null;
    }

    public static int deleteUser(int id) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/" + id).openConnection();
        connection.setRequestMethod("DELETE");
        int responseCode = connection.getResponseCode();
        return responseCode;
    }

    public static String getAllUsers() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL).openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            List<User> users = gson.fromJson(response.toString(), new TypeToken<List<User>>() {}.getType());
            return users.toString();
        }
    }

    public static String getUserById(int id) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/" + id).openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            User user = gson.fromJson(response.toString(), User.class);
            return user.toString();
        }
    }

    public static String getUserByUsername(String username) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "?username=" + username).openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            List<User> users = gson.fromJson(response.toString(), new TypeToken<List<User>>() {}.getType());
            return users.toString();
        }
    }

    // 2 task
    public static void getLastPostCommentsByUser(int userId) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/" + userId + "/posts").openConnection();
        connection.setRequestMethod("GET");

        List<Post> posts;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            posts = gson.fromJson(response.toString(), new TypeToken<List<Post>>() {}.getType());
        }

        if (posts.isEmpty()) {
            System.out.println("No posts found for user ID: " + userId);
            return;
        }
        Post lastPost = posts.stream().max((p1, p2) -> p1.getId().compareTo(p2.getId())).orElse(null);

        connection = (HttpURLConnection) new URL("https://jsonplaceholder.typicode.com/posts/" + lastPost.getId() + "/comments").openConnection();
        connection.setRequestMethod("GET");

        List<Comment> comments;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            comments = gson.fromJson(response.toString(), new TypeToken<List<Comment>>() {}.getType());
        }

        String filename = String.format("user-%d-post-%d-comments.json", userId, lastPost.getId());
        try (FileWriter fileWriter = new FileWriter(filename)) {
            gson.toJson(comments, fileWriter);
        }

        System.out.println("Comments saved to " + filename);
    }

     // 3 task
    public static void getOpenTodosByUser(int userId) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/" + userId + "/todos").openConnection();
        connection.setRequestMethod("GET");

        List<Todo> todos;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            todos = gson.fromJson(response.toString(), new TypeToken<List<Todo>>() {}.getType());
        }

        List<Todo> openTodos = todos.stream()
                .filter(todo -> !todo.isCompleted()).toList();

        System.out.println("Open Todos for User ID " + userId + ":");
        for (Todo todo : openTodos) {
            System.out.println("- " + todo.getTitle());
        }
    }

    static class User {
        private Integer id;
        private String name;
        private String email;

        public User(Integer id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

    static class Post {
        private Integer id;
        private Integer userId;
        private String title;
        private String body;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }

    static class Comment {
        private Integer id;
        private Integer postId;
        private String name;
        private String email;
        private String body;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }
    static class Todo {
        private Integer id;
        private Integer userId;
        private String title;
        private boolean completed;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }
}
