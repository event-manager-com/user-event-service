package gregad.eventmanager.usereventservice.dao;

import gregad.eventmanager.usereventservice.model.EventEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author Greg Adler
 */
@Repository
public interface EventDao extends MongoRepository<EventEntity,Long> {
    
    List<EventEntity> findAllByOwnerIdAndTitleContaining(long ownerId, String title);

    List<EventEntity> findAllByOwnerId(long ownerId);

    List<EventEntity> findByOwnerIdAndEventDateBetween(long ownerId, LocalDate from, LocalDate to);

}
