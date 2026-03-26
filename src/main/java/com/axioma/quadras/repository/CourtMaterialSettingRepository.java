package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.CourtMaterialCode;
import com.axioma.quadras.domain.model.CourtMaterialSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtMaterialSettingRepository extends JpaRepository<CourtMaterialSetting, Long> {

	Optional<CourtMaterialSetting> findByCode(CourtMaterialCode code);
}
