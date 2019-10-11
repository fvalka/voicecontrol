package com.vektorraum.voicecontrol.repository;

import com.vektorraum.voicecontrol.model.Call;
import org.springframework.data.repository.CrudRepository;

public interface CallRepository extends CrudRepository<Call, String> {
}
