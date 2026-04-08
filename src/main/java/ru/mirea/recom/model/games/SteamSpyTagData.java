package ru.mirea.recom.model.games;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Map;

@Data
@Entity
@Table(name = "steamspy_tag_data")
public class SteamSpyTagData {
    @Id
    private Integer appId;

    // Хранение тегов в виде JSON-строки
    @Lob
    private String tagsJson;

    @Transient
    private Map<String, Integer> tags;

    public void setTags(Map<String, Integer> tags) {
        this.tags = tags;
        try {
            this.tagsJson = new ObjectMapper().writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            this.tagsJson = "{}";
        }
    }
}
