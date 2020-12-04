package gregad.eventmanager.usereventservice.dao;

import gregad.eventmanager.usereventservice.model.EventEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Greg Adler
 */
@Repository
public interface EventDao extends MongoRepository<EventEntity,Long> {

    Optional<List<EventEntity>> findAllByOwnerIdAndTitleContaining(int ownerId, String title);

    Optional<List<EventEntity>> findAllByOwnerId(int ownerId);
    
    Optional<EventEntity> findByIdAndOwnerId(long eventId, int ownerId);
}
