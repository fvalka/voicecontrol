package com.vektorraum.voicecontrol.repository;

import com.vektorraum.voicecontrol.model.Call;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CallRepository extends CrudRepository<Call, String> {
}
