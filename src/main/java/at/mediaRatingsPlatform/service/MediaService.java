package at.mediaRatingsPlatform.service;

import at.mediaRatingsPlatform.dao.MediaDao;
import at.mediaRatingsPlatform.exception.ForbiddenException;
import at.mediaRatingsPlatform.exception.NotFoundException;
import at.mediaRatingsPlatform.model.Genre;
import at.mediaRatingsPlatform.model.Media;
import at.mediaRatingsPlatform.model.MediaType;
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
        Media media = dao.getById(id);
        if (media == null)
            throw new NotFoundException("Media not found");
        return media;
    }

    public List<Media> list(){
        return dao.getAll();
    }

    public void update(Media m, User u){
        Media existing = dao.getById(m.getId());
        if (existing == null)
            throw new NotFoundException("Media not found");
        if (!existing.getUserId().equals(u.getId()))
            throw new ForbiddenException("Not allowed to edit this media");
        dao.update(existing.getId(), m);
    }

    public void delete(UUID id, User u){
        Media existing = dao.getById(id);
        if (existing == null)
            throw new NotFoundException("Media not found");
        if (!existing.getUserId().equals(u.getId()))
            throw new ForbiddenException("Not allowed to delete this media");
        dao.delete(id);
    }

    public List<Media> filterByGenre(Genre genre) {
        return dao.getAll().stream()
                .filter(m -> m.getGenreList() != null && m.getGenreList().stream()
                        .anyMatch(g -> g.getName().equalsIgnoreCase(genre.getName())))
                .collect(Collectors.toList());
    }


    public List<Media> filterByType(MediaType type) {
        return dao.getAll().stream()
                .filter(m -> m.getMediaType() != null && m.getMediaType().getName().equalsIgnoreCase(type.getName()))
                .collect(Collectors.toList());
    }

    public List<Media> sortByReleaseYear() {
        return dao.getAll().stream()
                .sorted(Comparator.comparingInt(Media::getReleaseYear))
                .collect(Collectors.toList());
    }

}
