package at.mediaRatingsPlatform.service;

import at.mediaRatingsPlatform.dao.MediaDao;
import at.mediaRatingsPlatform.model.GenreEnum;
import at.mediaRatingsPlatform.model.Media;
import at.mediaRatingsPlatform.model.MediaTypeEnum;
import at.mediaRatingsPlatform.model.User;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MediaService {
    private final MediaDao dao;

    public MediaService(MediaDao dao){
        this.dao=dao;
    }

    public Media create(Media m, User u){
        m.setUserId(u.getId());
        return dao.create(m);
    }

    public Media get(UUID id){
        return dao.getById(id);
    }

    public List<Media> list(){
        return dao.getAll();
    }

    public void update(Media m, User u){
        Media existing = dao.getById(m.getId());
        if (existing==null || !existing.getUserId().equals(u.getId()))
            throw new RuntimeException("forbidden");
        dao.update(existing.getId(), m);
    }

    public void delete(UUID id, User u){
        Media existing = dao.getById(id);
        // TODO: throw exception, 400
        if (existing == null || !existing.getUserId().equals(u.getId()))
            throw new RuntimeException("forbidden");
        dao.delete(id);
    }

    public List<Media> filterByGenre(GenreEnum genre) {
        return dao.getAll().stream()
                .filter(m -> m.getGenreList() != null && m.getGenreList().contains(genre))
                .collect(Collectors.toList());
    }


    public List<Media> filterByType(MediaTypeEnum type) {
        return dao.getAll().stream()
                .filter(m -> m.getMediaType() == type)
                .collect(Collectors.toList());
    }

    public List<Media> sortByReleaseYear() {
        return dao.getAll().stream()
                .sorted(Comparator.comparingInt(Media::getReleaseYear))
                .collect(Collectors.toList());
    }

}
