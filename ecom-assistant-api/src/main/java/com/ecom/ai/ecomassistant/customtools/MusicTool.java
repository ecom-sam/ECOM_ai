package com.ecom.ai.ecomassistant.customtools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class MusicTool {
    private static final Map<String, String[]> GENRE_SONGS = new HashMap<>();
    private static final Random RANDOM = new Random();

    static {
        GENRE_SONGS.put("pop", new String[] {"Shape of You - Ed Sheeran", "Blinding Lights - The Weeknd", "Uptown Funk - Bruno Mars"});
        GENRE_SONGS.put("rock", new String[] {"Bohemian Rhapsody - Queen", "Stairway to Heaven - Led Zeppelin", "Hotel California - Eagles"});
        GENRE_SONGS.put("jazz", new String[] {"Take Five - Dave Brubeck", "So What - Miles Davis", "Feeling Good - Nina Simone"});
        GENRE_SONGS.put("bossa nova", new String[] {
                "The Girl from Ipanema - Antônio Carlos Jobim",
                "Corcovado (Quiet Nights of Quiet Stars) - Antônio Carlos Jobim",
                "Desafinado - João Gilberto",
                "Wave - Antônio Carlos Jobim"
        });
    }

    @Tool(description = "Get a random song recommendation by genre")
    public String recommendSong(String genre) {
        String[] songs = GENRE_SONGS.get(genre.toLowerCase());
        if (songs == null) {
            return "Sorry, I don't have recommendations for that genre.";
        }
        int idx = RANDOM.nextInt(songs.length);
        return songs[idx];
    }
}
