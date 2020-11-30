package gregad.eventmanager.usereventservice.dao;

import gregad.eventmanager.usereventservice.model.DatabaseSequence;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Greg Adler
 */
@Repository
public interface SequenceDao extends MongoRepository<DatabaseSequence,Integer> {
}
