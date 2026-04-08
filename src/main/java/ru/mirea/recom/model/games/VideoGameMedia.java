package ru.mirea.recom.model.games;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "video_game_media")
public class VideoGameMedia {
    @Id
    private Integer steamAppId;
    private String headerImage;
    
    // Храним JSON-строку со скриншотами
    @Lob
    private String screenshotsJson;
    
    private String background;
    
    // Поле для возможного хранения данных по трейлерам или роликам (файл movies)
    @Lob
    private String movies;
}
