package com.ecom.ai.ecomassistant.customtools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MovieTool implements ChatToolMarker {
    private static final Map<String, String> MOVIE_DATABASE = new HashMap<>();

    static {
        MOVIE_DATABASE.put("inception", "Inception (2010) - A thief who steals corporate secrets through dream-sharing technology.");
        MOVIE_DATABASE.put("the matrix", "The Matrix (1999) - A computer hacker learns about the true nature of reality.");
        MOVIE_DATABASE.put("interstellar", "Interstellar (2014) - A team of explorers travel through a wormhole in space.");
        MOVIE_DATABASE.put("terminator", "The Terminator (1984) - A cyborg assassin is sent back in time to kill the mother of the future resistance leader.");
    }

    @Tool(description = "Get a brief summary of a movie by title")
    public String getMovieSummary(String title) {
        String key = title.toLowerCase();
        return MOVIE_DATABASE.getOrDefault(key, "Sorry, I don't have information about that movie.");
    }
}
