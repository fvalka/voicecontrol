package com.vektorraum.voicecontrol.repository;

import com.vektorraum.voicecontrol.model.routing.AvailabilitySetting;
import org.springframework.data.repository.CrudRepository;

public interface AvailabilitySettingRepository extends CrudRepository<AvailabilitySetting, String> {
}
