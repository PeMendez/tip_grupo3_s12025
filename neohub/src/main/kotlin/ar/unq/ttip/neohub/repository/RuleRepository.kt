package ar.unq.ttip.neohub.repository

import ar.unq.ttip.neohub.model.Rule
import org.springframework.data.jpa.repository.JpaRepository

interface RuleRepository : JpaRepository<Rule, Long> {

}