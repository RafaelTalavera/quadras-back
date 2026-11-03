package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.LaundryDocumentConfirmation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaundryDocumentConfirmationRepository
		extends JpaRepository<LaundryDocumentConfirmation, Long> {
	List<LaundryDocumentConfirmation> findByDocumentIdOrderByCreatedAtAscIdAsc(Long documentId);
}
