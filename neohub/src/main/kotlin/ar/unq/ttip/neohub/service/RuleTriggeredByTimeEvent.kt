package ar.unq.ttip.neohub.service

import ar.unq.ttip.neohub.model.ruleEngine.Rule
import org.springframework.context.ApplicationEvent

class RuleTriggeredByTimeEvent(val rule: Rule) : ApplicationEvent(rule)