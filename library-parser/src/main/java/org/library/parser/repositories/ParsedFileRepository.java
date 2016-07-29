package org.library.parser.repositories;

import org.library.common.entities.ParsedFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParsedFileRepository extends MongoRepository<ParsedFile, String> {


}
