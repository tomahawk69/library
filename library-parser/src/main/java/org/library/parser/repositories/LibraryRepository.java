package org.library.parser.repositories;

import org.library.common.entities.Library;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryRepository extends MongoRepository<Library, String> {
    Library findByPath(String path);
}
